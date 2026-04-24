"""Menu agent for handling menu-related queries."""

from __future__ import annotations

import asyncio
import logging
import re
import time
import unicodedata

from langchain_core.messages import AIMessage
from langchain_core.language_models import BaseChatModel
from langgraph.prebuilt import create_react_agent

from src.agents.state import AgentState
from src.tools import menu_tools

logger = logging.getLogger(__name__)

# -----------------------------------------------------------------------
# Deterministic add-to-cart intent detection
# -----------------------------------------------------------------------
_ADD_KEYWORDS = [
    "thêm", "them", "thêm vào", "them vao",
    "order", "đặt món", "dat mon", "đặt", "dat",
    "cho tôi", "cho toi", "cho mình", "cho minh",
    "mua", "lấy cho", "lay cho", "gọi món", "goi mon",
    "muốn thêm", "muon them", "muốn mua", "muon mua", "muốn order", "muon order",
    "add", "muốn đặt", "muon dat",
]
_QUANTITY_PATTERN = re.compile(r"(\d+)\s*(?:phần|ly|tô|bát|đĩa|cái|suất|con|cái)?", re.IGNORECASE)
_CART_SUFFIX_PATTERN = re.compile(
    r"\b(vào giỏ hàng|vao gio hang|vào giỏ|vao gio|vào cart|vao cart|to cart)\b",
    re.IGNORECASE,
)
_FILLER_PATTERN = re.compile(
    r"\b(cho tôi|cho toi|cho mình|cho minh|cho em|cho anh|cho chị|cho chi|nữa|nua|với|voi|nhé|nhe|nha|đi|di)\b",
    re.IGNORECASE,
)


def _normalize_text(text: str) -> str:
    """Lowercase and strip Vietnamese accents for robust keyword matching."""
    normalized = unicodedata.normalize("NFD", text)
    without_marks = "".join(ch for ch in normalized if unicodedata.category(ch) != "Mn")
    return without_marks.lower().replace("đ", "d")


def _is_expected_provider_error(exc: Exception) -> bool:
    """Identify transient upstream/provider errors to keep logs concise."""
    name = type(exc).__name__
    if name in {"ResponseError", "ConnectError", "ReadTimeout", "TimeoutException"}:
        return True

    text = str(exc).lower()
    return any(token in text for token in ["status code: 500", "internal server error", "timeout"])


def _detect_add_intent(user_text: str) -> bool:
    """Return True if user message clearly requests adding to cart."""
    text_norm = _normalize_text(user_text)
    return any(_normalize_text(k) in text_norm for k in _ADD_KEYWORDS)


def _extract_multiple_items(user_text: str) -> list[tuple[str, int]]:
    """Extract multiple items from user text, handling separators like 'và', ','."""
    text = user_text.lower()

    # Remove common filler prefixes
    for kw in _ADD_KEYWORDS:
        text = text.replace(kw, " ")

    # Remove trailing add-to-cart suffixes and filler words
    text = _CART_SUFFIX_PATTERN.sub(" ", text)
    text = _FILLER_PATTERN.sub(" ", text)
    text = re.sub(r"\s+", " ", text).strip()

    # Split by separators (và, cùng, dấu phẩy)
    parts = re.split(r",|\b(và|cùng)\b", text)

    items: list[tuple[str, int]] = []
    for part in parts:
        if not part or part.strip() in ("", "và", "cùng", "với"):
            continue

        part = part.strip()
        m = _QUANTITY_PATTERN.search(part)
        qty = max(1, int(m.group(1))) if m else 1
        dish_name = _QUANTITY_PATTERN.sub(" ", part).strip()
        if dish_name:
            items.append((dish_name, qty))

    return items


MENU_AGENT_PROMPT = """You are the restaurant menu specialist assistant. Your responsibilities are to help users:

- Browse menu categories
- Search dishes by name or ingredients
- View dish details (price, description, ingredients)
- Recommend suitable dishes

Use available tools to fetch menu information.
Respond in Vietnamese, with a friendly and clear tone.
If a requested dish does not exist, suggest similar alternatives.
Only answer within menu and dish information scope.
"""


def create_menu_agent(llm: BaseChatModel):
    """Create the menu agent using ReAct pattern."""
    tools = [
        menu_tools.get_menu_categories,
        menu_tools.search_menu,
        menu_tools.get_dish_details,
        menu_tools.get_dishes_by_category,
    ]
    logger.info("FLOW menu_agent.init tool_count=%s", len(tools))
    agent = create_react_agent(llm, tools, prompt=MENU_AGENT_PROMPT)
    return agent


def create_menu_agent_node(llm: BaseChatModel):
    """Create the menu agent node for the graph."""
    from src.repositories.menu_repo import MenuRepository
    from src.tools.order_tools import _carts

    _menu_repo = MenuRepository()
    agent = create_menu_agent(llm)

    async def menu_agent_node(state: AgentState) -> dict:
        """Process menu-related queries.

        If the user clearly wants to add a dish to the cart:
        1. This node directly queries the DB to find the product.
        2. Adds it to the in-memory cart without relying on the LLM to call tools.
        3. Returns action=UPDATE_CART immediately.

        For pure information queries, the LLM handles it normally via ReAct.
        """
        session_id = state.get("session_id", "")
        logger.info("FLOW menu_agent.start session_id=%s", session_id)

        # Get original user message
        user_text = ""
        for msg in reversed(state.get("messages", [])):
            if getattr(msg, "type", "") == "human":
                user_text = msg.content
                break

        is_add_intent = _detect_add_intent(user_text)

        # ------------------------------------------------------------------
        # FAST PATH: Deterministic add-to-cart
        # Don't rely on LLM to call search + transfer tools. 
        # We query the DB directly and add to cart in code.
        # ------------------------------------------------------------------
        if is_add_intent:
            items_to_add = _extract_multiple_items(user_text)
            if not items_to_add:
                items_to_add = [(user_text, 1)]  # fallback

            logger.info(
                "FLOW menu_agent.add_intent_detected session_id=%s items=%s",
                session_id, items_to_add,
            )

            # Sync current_cart from App into _carts once
            current_app_cart = list(state.get("current_cart") or [])
            _carts[session_id] = {
                "items": current_app_cart,
                "last_access": time.time(),
            }
            cart = _carts[session_id]["items"]

            added_products = []

            for dish_query, quantity in items_to_add:
                try:
                    product = await _menu_repo.get_product_by_id_or_name(dish_query)
                except Exception:
                    logger.exception("FLOW menu_agent.db_lookup_failed session_id=%s", session_id)
                    product = None

                if product:
                    try:
                        image_url = await _menu_repo.get_product_image_url(product.id) or ""
                    except Exception:
                        image_url = ""

                    price = int(product.price)

                # Merge: update quantity if same product already in cart
                found = False
                for item in cart:
                    existing_id = item.get("item_id") or item.get("id")
                    if existing_id == str(product.id):
                        item["quantity"] += quantity
                        # Keep canonical key for downstream consistency.
                        if "item_id" not in item:
                            item["item_id"] = str(product.id)
                        found = True
                        break
                if not found:
                    cart.append({
                        "item_id": str(product.id),
                        "name": product.name,
                        "price": price,
                        "quantity": quantity,
                        "url": image_url,
                    })

                    added_products.append((product, quantity))
                    logger.info(
                        "FLOW menu_agent.cart_updated session_id=%s product=%s quantity=%s cart_size=%s",
                        session_id, product.name, quantity, len(cart),
                    )
                else:
                    logger.info(
                        "FLOW menu_agent.product_not_found session_id=%s query=%s",
                        session_id, dish_query,
                    )

            if added_products:
                # Successfully added at least 1 item
                parts = [f"{q} {p.name}" for p, q in added_products]
                added_str = " và ".join(parts)
                
                return {
                    "messages": [AIMessage(
                        content=f"Tôi đã thêm {added_str} vào giỏ hàng cho bạn rồi nhé! 🛒",
                        name="menu_agent",
                    )],
                    "next_agent": "FINISH",
                    "last_topic": "action",
                    "action": "UPDATE_CART",
                    "action_data": list(cart),
                }
            else:
                # Dish not found: return deterministic response instead of falling through to LLM.
                logger.info(
                    "FLOW menu_agent.product_not_found session_id=%s query=%s",
                    session_id, dish_query,
                )
                return {
                    "messages": [
                        AIMessage(
                            content=f"Mình chưa tìm thấy món '{dish_query}'. Bạn thử nhập lại tên món gần đúng hơn nhé.",
                            name="menu_agent",
                        )
                    ],
                    "next_agent": "FINISH",
                    "last_topic": "action",
                    "action": "None",
                    "action_data": None,
                }

        # ------------------------------------------------------------------
        # SLOW PATH: Pure information query — let LLM handle via ReAct
        # ------------------------------------------------------------------
        result = None
        last_error: Exception | None = None
        for attempt in range(1, 3):
            try:
                result = await agent.ainvoke({"messages": state["messages"]})
                break
            except Exception as exc:
                last_error = exc
                logger.warning(
                    "FLOW menu_agent.llm_failed session_id=%s attempt=%s error_type=%s error=%s",
                    session_id,
                    attempt,
                    type(exc).__name__,
                    str(exc)[:240],
                )
                if not _is_expected_provider_error(exc):
                    logger.debug(
                        "FLOW menu_agent.llm_failed_details session_id=%s attempt=%s",
                        session_id,
                        attempt,
                        exc_info=True,
                    )
                if attempt < 2:
                    await asyncio.sleep(0.4 * attempt)

        if result is not None:
            last_message = result["messages"][-1]
            logger.info("FLOW menu_agent.done session_id=%s next_agent=FINISH", session_id)
            return {
                "messages": [AIMessage(content=last_message.content, name="menu_agent")],
                "next_agent": "FINISH",
                "last_topic": "menu",
            }

        if last_error is not None and _is_expected_provider_error(last_error):
            logger.info(
                "FLOW menu_agent.provider_unavailable session_id=%s fallback=graceful",
                session_id,
            )
        else:
            logger.error("FLOW menu_agent.failed session_id=%s", session_id, exc_info=True)

        return {
            "messages": [
                AIMessage(
                    content="Xin lỗi, mình chưa thể xử lý yêu cầu thực đơn lúc này. Bạn vui lòng thử lại sau nhé.",
                    name="menu_agent",
                )
            ],
            "next_agent": "FINISH",
            "last_topic": "menu",
        }

    return menu_agent_node
