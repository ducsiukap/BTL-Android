package com.example.ddht.ui.common;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.TextUtils;
import android.graphics.Rect;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddht.data.remote.SimpleStompClient;

import com.example.ddht.R;
import com.example.ddht.data.manager.CartManager;
import com.example.ddht.data.model.CartItem;
import com.example.ddht.data.model.Product;
import com.example.ddht.data.remote.dto.ApiResponse;
import com.example.ddht.data.remote.dto.CatalogDto;
import com.example.ddht.data.remote.dto.ChatCartItemDto;
import com.example.ddht.data.remote.dto.ChatMessageDto;
import com.example.ddht.data.remote.dto.ChatResponse;
import com.example.ddht.data.remote.dto.OrderResponse;
import com.example.ddht.data.remote.dto.OrderStatus;
import com.example.ddht.data.remote.dto.ProductDto;
import com.example.ddht.data.remote.dto.SpeechToTextResponse;
import com.example.ddht.data.repository.CatalogRepository;
import com.example.ddht.data.repository.ChatRepository;
import com.example.ddht.data.repository.OrderRepository;
import com.example.ddht.data.repository.ProductRepository;
import com.example.ddht.ui.common.adapter.ChatAdapter;
import com.example.ddht.ui.common.adapter.ProductAdapter;
import com.example.ddht.ui.common.adapter.StaffOrderAdapter;
import com.example.ddht.ui.common.fragment.AccountFragment;
import com.example.ddht.ui.manager.ProductDetailActivity;
import com.example.ddht.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1201;
    private static final int VOICE_SAMPLE_RATE = 16000;
    private static final int VOICE_CHANNEL_COUNT = 1;
    private static final int VOICE_BITS_PER_SAMPLE = 16;
    private SessionManager sessionManager;
    private CatalogRepository catalogRepository;
    private ProductRepository productRepository;
    private ChatRepository chatRepository;
    private ProductAdapter productAdapter;
    private EditText edtProductSearch;
    private TextView tvProductsError;
    private LinearLayout layoutCatalogFilters;
    private final List<CatalogDto> catalogsCache = new ArrayList<>();
    private Long selectedCatalogId = null;
    private String currentQuery = "";
    private String chatSessionId = null; // Duy trì session_id từ AI server
    private boolean isVoiceRecording = false;
    private AudioRecord voiceRecorder;
    private Thread voiceRecordThread;
    private volatile boolean keepVoiceRecording;
    private int voiceBufferSize;
    private File voiceTempFile;
    private ChatAdapter activeChatAdapter;
    private RecyclerView activeChatRecycler;
    private EditText activeChatInput;
    private ImageButton activeVoiceButton;

    private OrderRepository orderRepository;
    private StaffOrderAdapter staffOrderAdapter;
    private SimpleStompClient stompClient;
    private Button btnOrderFilter;
    private final List<String> selectedStatuses = new ArrayList<>();
    private final String[] statusLabels = {"CHỜ THANH TOÁN", "ĐANG CHẾ BIẾN", "HOÀN THÀNH", "ĐÃ HỦY"};
    private final String[] statusValues = {"PENDING", "PREPARING", "COMPLETED", "CANCELLED"};
    private final boolean[] checkedItems = {false, false, false, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);
        catalogRepository = new CatalogRepository();
        productRepository = new ProductRepository();
        orderRepository = new OrderRepository();
        chatRepository = new ChatRepository();

        RecyclerView rvProducts = findViewById(R.id.rvProducts);
        edtProductSearch = findViewById(R.id.edtProductSearch);
        ImageButton btnSearchProducts = findViewById(R.id.btnSearchProducts);
        Button btnOpenCart = findViewById(R.id.btnOpenCart);
        Button btnCreateOrder = findViewById(R.id.btnCreateOrder);
        TextView tvWelcome = findViewById(R.id.tvWelcome);
        tvProductsError = findViewById(R.id.tvProductsError);
        layoutCatalogFilters = findViewById(R.id.layoutCatalogFilters);
        LinearLayout layoutProducts = findViewById(R.id.layoutHomeProducts);
        android.widget.FrameLayout layoutAccount = findViewById(R.id.layoutHomeAccount);
        LinearLayout layoutOrders = findViewById(R.id.layoutHomeOrders);
        btnOrderFilter = findViewById(R.id.btnOrderFilter);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        FloatingActionButton fabChatBot = findViewById(R.id.fabChatBot);

        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));
        int spacingPx = dpToPx(8);
        if (rvProducts.getItemDecorationCount() == 0) {
            rvProducts.addItemDecoration(new GridSpacingItemDecoration(2, spacingPx));
        }
        productAdapter = new ProductAdapter(new ArrayList<>(), product -> {
            Intent intent = new Intent(HomeActivity.this, ProductDetailActivity.class);
            if (product.getId() != null) {
                intent.putExtra(ProductDetailActivity.EXTRA_PRODUCT_ID, product.getId().longValue());
            }
            startActivity(intent);
        });
        rvProducts.setAdapter(productAdapter);

        RecyclerView rvStaffOrders = findViewById(R.id.rvStaffOrders);
        if (rvStaffOrders != null) {
            rvStaffOrders.setLayoutManager(new LinearLayoutManager(this));
            staffOrderAdapter = new StaffOrderAdapter(new StaffOrderAdapter.OnOrderActionListener() {
                @Override
                public void onUpdateStatus(OrderResponse order, OrderStatus nextStatus) {
                    updateStaffOrderStatus(order.getId(), nextStatus.name());
                }

                @Override
                public void onMarkAsPaid(OrderResponse order) {
                    markStaffOrderAsPaid(order.getId());
                }

                @Override
                public void onCancelOrder(OrderResponse order) {
                    cancelStaffOrder(order.getId());
                }

                @Override
                public void onItemClick(OrderResponse order) {
                }
            });
            rvStaffOrders.setAdapter(staffOrderAdapter);
        }

        btnSearchProducts.setOnClickListener(v -> searchProducts());
        edtProductSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                searchProducts();
                return true;
            }
            return false;
        });

        if (sessionManager.isLoggedIn()) {
            String userName = sessionManager.getUserName();
            tvWelcome.setText(getString(R.string.home_staff_welcome, userName == null ? "Nhân viên" : userName));
        } else {
            tvWelcome.setText(R.string.home_guest_welcome);
        }

        String role = sessionManager.getUserRole();
        boolean isStaff = role != null && role.equalsIgnoreCase("STAFF");
        if (bottomNavigationView.getMenu().findItem(R.id.nav_orders) != null) {
            bottomNavigationView.getMenu().findItem(R.id.nav_orders).setVisible(isStaff);
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.layoutHomeAccount, new AccountFragment()).commit();

        bottomNavigationView.setSelectedItemId(R.id.nav_products);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_products) {
                layoutProducts.setVisibility(View.VISIBLE);
                layoutOrders.setVisibility(View.GONE);
                layoutAccount.setVisibility(View.GONE);
                fabChatBot.show();
                return true;
            }
            if (item.getItemId() == R.id.nav_orders && isStaff) {
                layoutProducts.setVisibility(View.GONE);
                layoutOrders.setVisibility(View.VISIBLE);
                layoutAccount.setVisibility(View.GONE);
                fabChatBot.hide();
                loadStaffOrders();
                return true;
            }
            if (item.getItemId() == R.id.nav_account) {
                layoutProducts.setVisibility(View.GONE);
                layoutOrders.setVisibility(View.GONE);
                layoutAccount.setVisibility(View.VISIBLE);
                fabChatBot.hide();
                return true;
            }
            return false;
        });

        btnOpenCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));
        btnCreateOrder.setText(R.string.order_lookup_title);
        btnCreateOrder.setOnClickListener(v -> startActivity(new Intent(this, OrderLookupActivity.class)));

        fabChatBot.setOnClickListener(v -> showChatBotDialog());

        loadCatalogs();
        loadProducts(currentQuery);
        if (btnOrderFilter != null) {
            btnOrderFilter.setOnClickListener(v -> showFilterBottomSheet());
        }

        if (isStaff) {
            initStaffWebSocket();
        }
    }

    private void initStaffWebSocket() {
        String wsUrl = "ws://10.0.2.2:3333/ws-order";
        stompClient = new SimpleStompClient(wsUrl);
        stompClient.connect();
        stompClient.subscribe("/topic/staff/new-order", payload -> {
            Toast.makeText(HomeActivity.this, "CÓ ĐƠN HÀNG MỚI!", Toast.LENGTH_LONG).show();
            loadStaffOrders();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (stompClient != null) {
            stompClient.disconnect();
        }
    }

    private void showChatBotDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_chat_bot, null);
        RecyclerView rvChatHistory = dialogView.findViewById(R.id.rvChatHistory);
        EditText edtChatMessage = dialogView.findViewById(R.id.edtChatMessage);
        ImageButton btnVoiceChat = dialogView.findViewById(R.id.btnVoiceChat);
        ImageButton btnSendChat = dialogView.findViewById(R.id.btnSendChat);
        ImageButton btnGoToCart = dialogView.findViewById(R.id.btnChatGoToCart);

        ChatAdapter chatAdapter = new ChatAdapter();
        rvChatHistory.setLayoutManager(new LinearLayoutManager(this));
        rvChatHistory.setAdapter(chatAdapter);
        activeChatInput = edtChatMessage;

        chatAdapter.addMessage(new ChatMessageDto(
                "Xin chào! Tôi là trợ lý ảo. Bạn hãy chọn gợi ý hoặc nhập nội dung để tôi hỗ trợ nhé!", false));

        btnGoToCart.setOnClickListener(v -> startActivity(new Intent(this, CartActivity.class)));

        View.OnClickListener sendListener = v -> {
            String msg = edtChatMessage.getText().toString().trim();
            if (TextUtils.isEmpty(msg))
                return;
            chatAdapter.addMessage(new ChatMessageDto(msg, true));
            edtChatMessage.setText("");
            rvChatHistory.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            sendChatMessage(msg, chatAdapter, rvChatHistory);
        };
        btnSendChat.setOnClickListener(sendListener);
        btnVoiceChat.setOnClickListener(v -> handleVoiceChatButton(chatAdapter, rvChatHistory, btnVoiceChat));

        View.OnClickListener suggestListener = v -> {
            String msg = ((Button) v).getText().toString();
            chatAdapter.addMessage(new ChatMessageDto(msg, true));
            rvChatHistory.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
            sendChatMessage(msg, chatAdapter, rvChatHistory);
        };

        dialogView.findViewById(R.id.btnChatSuggestSearch).setOnClickListener(suggestListener);
        dialogView.findViewById(R.id.btnChatSuggestBestSeller).setOnClickListener(suggestListener);
        dialogView.findViewById(R.id.btnChatSuggestPrice).setOnClickListener(suggestListener);
        dialogView.findViewById(R.id.btnChatSuggestAddToCart).setOnClickListener(suggestListener);

        AlertDialog dialog = new AlertDialog.Builder(this).setView(dialogView).setPositiveButton("Đóng", null).create();
        dialog.setOnDismissListener(d -> {
            if (isVoiceRecording) {
                stopVoiceRecordingInternal();
            }
            resetVoiceRecorder();
            cleanupVoiceTempFile();
            isVoiceRecording = false;
            activeChatAdapter = null;
            activeChatRecycler = null;
            activeChatInput = null;
            activeVoiceButton = null;
        });
        dialog.show();
    }

    private void sendChatMessage(String msg, ChatAdapter adapter, RecyclerView rv) {
        List<ChatCartItemDto> currentCartPayload = shouldIncludeCurrentCart(msg)
                ? buildCurrentCartPayload()
                : null;

        chatRepository.sendMessage(msg, chatSessionId, currentCartPayload).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                Log.d(TAG, "Chat HTTP Status: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse chatData = response.body();
                    // Lưu lại session_id cho các lần chat kế tiếp
                    chatSessionId = chatData.getSessionId();

                    Log.d(TAG, "Bot Response: " + chatData.getResponse());
                    adapter.addMessage(new ChatMessageDto(chatData.getResponse(), false));

                    if (chatData.getActionData() != null) {
                        syncCartFromChatAction(chatData.getActionData());
                        if (!chatData.getActionData().isEmpty()) {
                            Toast.makeText(HomeActivity.this, "Da cap nhat gio hang tu tro ly ao", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    rv.smoothScrollToPosition(adapter.getItemCount() - 1);
                } else {
                    Log.e(TAG, "Chat response failed: " + response.message());
                    adapter.addMessage(new ChatMessageDto("Bot không phản hồi. Vui lòng thử lại sau.", false));
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Log.e(TAG, "Network Error to AI Server: ", t);
                adapter.addMessage(new ChatMessageDto("Lỗi kết nối tới trợ lý ảo.", false));
                rv.smoothScrollToPosition(adapter.getItemCount() - 1);
            }
        });
    }

    private boolean shouldIncludeCurrentCart(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return false;
        }

        String lower = msg.toLowerCase();
        boolean hasAddKeyword = lower.contains("thêm") || lower.contains("them") || lower.contains("add");
        boolean hasCartKeyword = lower.contains("giỏ hàng") || lower.contains("gio hang") || lower.contains("cart");
        return hasAddKeyword && hasCartKeyword;
    }

    private List<ChatCartItemDto> buildCurrentCartPayload() {
        List<CartItem> items = CartManager.getInstance().getCartItems();
        List<ChatCartItemDto> payload = new ArrayList<>();

        for (CartItem item : items) {
            if (item == null || item.getProduct() == null || item.getProduct().getId() == null) {
                continue;
            }

            Product product = item.getProduct();
            payload.add(new ChatCartItemDto(
                    "product_" + product.getId(),
                    product.getName(),
                    product.getDisplayPrice(),
                    item.getQuantity(),
                    product.getImageUrl()));
        }

        return payload;
    }

    private void syncCartFromChatAction(List<ChatCartItemDto> actionItems) {
        CartManager cartManager = CartManager.getInstance();
        cartManager.clearCart();

        for (ChatCartItemDto actionItem : actionItems) {
            if (actionItem == null || TextUtils.isEmpty(actionItem.getName()) || actionItem.getQuantity() <= 0) {
                continue;
            }

            Long mappedProductId = mapActionItemIdToProductId(actionItem.getItemId());
            if (mappedProductId == null) {
                continue;
            }

            Product product = new Product(
                    mappedProductId,
                    actionItem.getName(),
                    "",
                    actionItem.getPrice(),
                    actionItem.getPrice(),
                    false,
                    actionItem.getUrl());
            cartManager.addProduct(product, actionItem.getQuantity());
        }
    }

    private Long mapActionItemIdToProductId(String itemId) {
        if (TextUtils.isEmpty(itemId)) {
            return null;
        }

        int numberStart = -1;
        for (int i = itemId.length() - 1; i >= 0; i--) {
            if (Character.isDigit(itemId.charAt(i))) {
                numberStart = i;
            } else if (numberStart != -1) {
                break;
            }
        }

        if (numberStart != -1) {
            String numericTail = itemId.substring(numberStart);
            try {
                return Long.parseLong(numericTail);
            } catch (NumberFormatException ignored) {
                // Fallback to hash-based id below.
            }
        }

        return 1_000_000_000L + (long) Math.abs(itemId.hashCode());
    }

    private void handleVoiceChatButton(ChatAdapter adapter, RecyclerView rv, ImageButton button) {
        activeChatAdapter = adapter;
        activeChatRecycler = rv;
        activeVoiceButton = button;

        if (!isVoiceRecording) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[] { Manifest.permission.RECORD_AUDIO },
                        REQUEST_RECORD_AUDIO_PERMISSION);
                return;
            }
            startVoiceRecording();
            return;
        }

        stopVoiceRecordingAndSend();
    }

    private void startVoiceRecording() {
        try {
            voiceTempFile = File.createTempFile("voice_chat_", ".wav", getCacheDir());
            voiceBufferSize = AudioRecord.getMinBufferSize(
                    VOICE_SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            if (voiceBufferSize <= 0) {
                throw new IllegalStateException("Invalid audio buffer size: " + voiceBufferSize);
            }

            voiceBufferSize = Math.max(voiceBufferSize, VOICE_SAMPLE_RATE);
            voiceRecorder = new AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    VOICE_SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    voiceBufferSize);

            if (voiceRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
                throw new IllegalStateException("AudioRecord initialization failed");
            }

            keepVoiceRecording = true;
            voiceRecorder.startRecording();
            voiceRecordThread = new Thread(this::recordPcmToWavFile, "voice-wav-recorder");
            voiceRecordThread.start();

            isVoiceRecording = true;
            if (activeVoiceButton != null) {
                activeVoiceButton.setColorFilter(getColor(android.R.color.holo_red_dark));
            }
            Toast.makeText(this, "Đang ghi âm WAV... Bấm lại để gửi", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Failed to start voice recording", e);
            resetVoiceRecorder();
            cleanupVoiceTempFile();
            isVoiceRecording = false;
            Toast.makeText(this, "Không thể bắt đầu ghi âm", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopVoiceRecordingAndSend() {
        stopVoiceRecordingInternal();
        resetVoiceRecorder();

        if (activeVoiceButton != null) {
            activeVoiceButton.clearColorFilter();
        }

        if (voiceTempFile == null || !voiceTempFile.exists() || voiceTempFile.length() <= 44) {
            cleanupVoiceTempFile();
            Toast.makeText(this, "Không có dữ liệu ghi âm", Toast.LENGTH_SHORT).show();
            return;
        }

        final File uploadFile = voiceTempFile;

        if (activeChatAdapter != null) {
            activeChatAdapter.addMessage(new ChatMessageDto("Đang chuyển giọng nói thành văn bản...", false));
            if (activeChatRecycler != null) {
                activeChatRecycler.smoothScrollToPosition(activeChatAdapter.getItemCount() - 1);
            }
        }

        chatRepository.speechToText(uploadFile).enqueue(new Callback<SpeechToTextResponse>() {
            @Override
            public void onResponse(Call<SpeechToTextResponse> call, Response<SpeechToTextResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SpeechToTextResponse speechData = response.body();
                    String transcript = !TextUtils.isEmpty(speechData.getCorrectedText())
                            ? speechData.getCorrectedText()
                            : speechData.getOriginalText();

                    if (!TextUtils.isEmpty(transcript) && activeChatInput != null) {
                        activeChatInput.setText(transcript);
                        activeChatInput.setSelection(transcript.length());
                    }

                    if (activeChatAdapter != null) {
                        String previewMessage = TextUtils.isEmpty(transcript)
                                ? "Không nhận được nội dung từ giọng nói."
                                : "Đã nhận diện: " + transcript;
                        activeChatAdapter.addMessage(new ChatMessageDto(previewMessage, false));
                        if (activeChatRecycler != null) {
                            activeChatRecycler.smoothScrollToPosition(activeChatAdapter.getItemCount() - 1);
                        }
                    }
                } else if (activeChatAdapter != null) {
                    activeChatAdapter.addMessage(new ChatMessageDto("Không thể nhận diện giọng nói lúc này.", false));
                }

                cleanupVoiceTempFile();
            }

            @Override
            public void onFailure(Call<SpeechToTextResponse> call, Throwable t) {
                Log.e(TAG, "Speech-to-text failed", t);
                if (activeChatAdapter != null) {
                    activeChatAdapter.addMessage(new ChatMessageDto("Lỗi kết nối speech-to-text.", false));
                    if (activeChatRecycler != null) {
                        activeChatRecycler.smoothScrollToPosition(activeChatAdapter.getItemCount() - 1);
                    }
                }
                cleanupVoiceTempFile();
            }
        });
    }

    private void recordPcmToWavFile() {
        if (voiceTempFile == null || voiceRecorder == null) {
            return;
        }

        long totalPcmBytes = 0;
        byte[] buffer = new byte[voiceBufferSize];

        try (FileOutputStream outputStream = new FileOutputStream(voiceTempFile)) {
            // Reserve 44-byte WAV header and patch it after recording stops.
            outputStream.write(new byte[44]);

            while (keepVoiceRecording) {
                int read = voiceRecorder.read(buffer, 0, buffer.length);
                if (read > 0) {
                    outputStream.write(buffer, 0, read);
                    totalPcmBytes += read;
                }
            }

            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "Failed to write WAV recording", e);
            return;
        }

        try {
            writeWavHeader(voiceTempFile, totalPcmBytes);
        } catch (IOException e) {
            Log.e(TAG, "Failed to finalize WAV header", e);
        }
    }

    private void writeWavHeader(File wavFile, long totalPcmBytes) throws IOException {
        long totalDataLen = totalPcmBytes + 36;
        long byteRate = (long) VOICE_SAMPLE_RATE * VOICE_CHANNEL_COUNT * VOICE_BITS_PER_SAMPLE / 8;
        short blockAlign = (short) (VOICE_CHANNEL_COUNT * VOICE_BITS_PER_SAMPLE / 8);

        ByteBuffer header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN);
        header.put(new byte[] { 'R', 'I', 'F', 'F' });
        header.putInt((int) totalDataLen);
        header.put(new byte[] { 'W', 'A', 'V', 'E' });
        header.put(new byte[] { 'f', 'm', 't', ' ' });
        header.putInt(16); // PCM chunk size
        header.putShort((short) 1); // PCM format
        header.putShort((short) VOICE_CHANNEL_COUNT);
        header.putInt(VOICE_SAMPLE_RATE);
        header.putInt((int) byteRate);
        header.putShort(blockAlign);
        header.putShort((short) VOICE_BITS_PER_SAMPLE);
        header.put(new byte[] { 'd', 'a', 't', 'a' });
        header.putInt((int) totalPcmBytes);

        try (RandomAccessFile wavAccess = new RandomAccessFile(wavFile, "rw")) {
            wavAccess.seek(0);
            wavAccess.write(header.array(), 0, 44);
        }
    }

    private void stopVoiceRecordingInternal() {
        keepVoiceRecording = false;
        if (voiceRecorder != null) {
            try {
                voiceRecorder.stop();
            } catch (RuntimeException e) {
                Log.e(TAG, "Failed to stop recording cleanly", e);
            }
        }
        waitVoiceRecordingThread();
        isVoiceRecording = false;
    }

    private void waitVoiceRecordingThread() {
        if (voiceRecordThread == null) {
            return;
        }
        try {
            voiceRecordThread.join(1200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e(TAG, "Interrupted while waiting voice thread", e);
        }
        voiceRecordThread = null;
    }

    private void resetVoiceRecorder() {
        if (voiceRecorder == null) {
            return;
        }
        voiceRecorder.release();
        voiceRecorder = null;
    }

    private void cleanupVoiceTempFile() {
        if (voiceTempFile == null) {
            return;
        }
        if (voiceTempFile.exists()) {
            // noinspection ResultOfMethodCallIgnored
            voiceTempFile.delete();
        }
        voiceTempFile = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_RECORD_AUDIO_PERMISSION) {
            return;
        }

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startVoiceRecording();
            return;
        }

        Toast.makeText(this, "Bạn cần cấp quyền micro để dùng voice chat", Toast.LENGTH_SHORT).show();
    }

    private Product mapDtoToProduct(ProductDto dto) {
        double originalPrice = dto.getOriginalPrice() == null ? 0 : dto.getOriginalPrice();
        double displayPrice = dto.getDiscountedPrice() == null ? originalPrice : dto.getDiscountedPrice();
        String imageUrl = (dto.getImages() != null && !dto.getImages().isEmpty()) ? dto.getImages().get(0).getUrl()
                : null;
        return new Product(dto.getId(), dto.getName(), dto.getDescription(), displayPrice, originalPrice,
                Boolean.TRUE.equals(dto.getSaleOff()), imageUrl);
    }

    private void loadStaffOrders() {
        if (staffOrderAdapter == null)
            return;
        String token = "Bearer " + sessionManager.getAccessToken();
        List<String> statuses = selectedStatuses.isEmpty() ? null : selectedStatuses;
        orderRepository.getStaffQueueOrders(token, statuses)
                .enqueue(new Callback<ApiResponse<List<OrderResponse>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<OrderResponse>>> call,
                            @NonNull Response<ApiResponse<List<OrderResponse>>> response) {
                        if (response.isSuccessful() && response.body() != null)
                            staffOrderAdapter.submitList(response.body().getData());
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<OrderResponse>>> call, @NonNull Throwable t) {
                        Toast.makeText(HomeActivity.this, "Lỗi tải đơn hàng: " + t.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void updateStaffOrderStatus(Long orderId, String status) {
        String token = "Bearer " + sessionManager.getAccessToken();
        orderRepository.updateOrderStatus(orderId, status, token).enqueue(new Callback<ApiResponse<OrderResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<OrderResponse>> call,
                    @NonNull Response<ApiResponse<OrderResponse>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    loadStaffOrders();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<OrderResponse>> call, @NonNull Throwable t) {
                Toast.makeText(HomeActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markStaffOrderAsPaid(Long orderId) {
        String token = "Bearer " + sessionManager.getAccessToken();
        orderRepository.markAsPaid(orderId, token).enqueue(new Callback<ApiResponse<OrderResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<OrderResponse>> call,
                    @NonNull Response<ApiResponse<OrderResponse>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "Đã xác nhận thanh toán", Toast.LENGTH_SHORT).show();
                    loadStaffOrders();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<OrderResponse>> call, @NonNull Throwable t) {
                Toast.makeText(HomeActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelStaffOrder(Long orderId) {
        String token = "Bearer " + sessionManager.getAccessToken();
        orderRepository.cancelOrder(orderId, token).enqueue(new Callback<ApiResponse<OrderResponse>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<OrderResponse>> call,
                    @NonNull Response<ApiResponse<OrderResponse>> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "Đã hủy đơn hàng", Toast.LENGTH_SHORT).show();
                    loadStaffOrders();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<OrderResponse>> call, @NonNull Throwable t) {
                Toast.makeText(HomeActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchProducts() {
        currentQuery = edtProductSearch.getText() == null ? "" : edtProductSearch.getText().toString().trim();
        loadProducts(currentQuery);
    }

    private void loadProducts(String query) {
        tvProductsError.setVisibility(View.GONE);
        productRepository.searchProducts(query, selectedCatalogId, 0, 50)
                .enqueue(new Callback<ApiResponse<List<ProductDto>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<ProductDto>>> call,
                            @NonNull Response<ApiResponse<List<ProductDto>>> response) {
                        if (!response.isSuccessful() || response.body() == null || response.body().getData() == null) {
                            tvProductsError.setText(R.string.home_products_load_failed);
                            tvProductsError.setVisibility(View.VISIBLE);
                            productAdapter.submit(new ArrayList<>());
                            return;
                        }
                        List<Product> mapped = new ArrayList<>();
                        for (ProductDto dto : response.body().getData())
                            mapped.add(mapDtoToProduct(dto));
                        productAdapter.submit(mapped);
                        if (mapped.isEmpty()) {
                            tvProductsError.setText(R.string.home_products_empty);
                            tvProductsError.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<ProductDto>>> call, @NonNull Throwable t) {
                        tvProductsError.setText(getString(R.string.network_error, t.getMessage()));
                        tvProductsError.setVisibility(View.VISIBLE);
                        productAdapter.submit(new ArrayList<>());
                    }
                });
    }

    private void loadCatalogs() {
        renderCatalogChips();
        catalogRepository.getAllCatalogs().enqueue(new Callback<ApiResponse<List<CatalogDto>>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<List<CatalogDto>>> call,
                    @NonNull Response<ApiResponse<List<CatalogDto>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    catalogsCache.clear();
                    catalogsCache.addAll(response.body().getData());
                    renderCatalogChips();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<List<CatalogDto>>> call, @NonNull Throwable t) {
            }
        });
    }

    private void renderCatalogChips() {
        layoutCatalogFilters.removeAllViews();
        addCatalogChip(getString(R.string.home_catalog_all), null, selectedCatalogId == null);
        for (CatalogDto c : catalogsCache)
            if (c != null && c.getId() != null)
                addCatalogChip(c.getName(), c.getId(), c.getId().equals(selectedCatalogId));
    }

    private void addCatalogChip(String label, Long id, boolean selected) {
        TextView chip = new TextView(this);
        chip.setText(label);
        chip.setSelected(selected);
        chip.setBackgroundResource(R.drawable.bg_catalog_chip);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        chip.setTextColor(getColor(selected ? android.R.color.white : R.color.brand_primary));
        chip.setPadding(dpToPx(12), dpToPx(8), dpToPx(12), dpToPx(8));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, -2);
        p.setMarginEnd(dpToPx(8));
        chip.setLayoutParams(p);
        chip.setOnClickListener(v -> {
            selectedCatalogId = id;
            renderCatalogChips();
            loadProducts(currentQuery);
        });
        layoutCatalogFilters.addView(chip);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private void showFilterBottomSheet() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn trạng thái đơn hàng");
        builder.setMultiChoiceItems(statusLabels, checkedItems, (dialog, which, isChecked) -> {
            checkedItems[which] = isChecked;
            if (isChecked) {
                if (!selectedStatuses.contains(statusValues[which])) {
                    selectedStatuses.add(statusValues[which]);
                }
            } else {
                selectedStatuses.remove(statusValues[which]);
            }
        });
        builder.setPositiveButton("Lọc", (dialog, which) -> {
            if (selectedStatuses.isEmpty()) {
                btnOrderFilter.setText("Lọc: Tất cả");
            } else if (selectedStatuses.size() == 1) {
                btnOrderFilter.setText("Lọc: " + mapOrderStatusToString(OrderStatus.valueOf(selectedStatuses.get(0))));
            } else {
                btnOrderFilter.setText("Lọc: " + selectedStatuses.size() + " mục");
            }
            loadStaffOrders();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private String mapOrderStatusToString(OrderStatus status) {
        switch (status) {
            case PENDING: return "CHỜ THANH TOÁN";
            case PREPARING: return "ĐANG CHẾ BIẾN";
            case COMPLETED: return "HOÀN THÀNH";
            case CANCELLED: return "ĐÃ HỦY";
            default: return status.name();
        }
    }

    private static class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int span, space;

        GridSpacingItemDecoration(int span, int space) {
            this.span = span;
            this.space = space;
        }

        @Override
        public void getItemOffsets(@NonNull Rect out, @NonNull View v, @NonNull RecyclerView p,
                @NonNull RecyclerView.State s) {
            int pos = p.getChildAdapterPosition(v);
            int col = pos % span;
            out.left = space - col * space / span;
            out.right = (col + 1) * space / span;
            if (pos < span)
                out.top = space;
            out.bottom = space;
        }
    }
}
