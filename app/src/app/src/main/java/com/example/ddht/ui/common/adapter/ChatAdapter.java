package com.example.ddht.ui.common.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ddht.R;
import com.example.ddht.data.remote.dto.ChatMessageDto;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private final List<ChatMessageDto> messages = new ArrayList<>();

    public void addMessage(ChatMessageDto message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessageDto message = messages.get(position);
        if (message.isUser()) {
            holder.tvUser.setVisibility(View.VISIBLE);
            holder.tvBot.setVisibility(View.GONE);
            holder.tvUser.setText(message.getText());
        } else {
            holder.tvBot.setVisibility(View.VISIBLE);
            holder.tvUser.setVisibility(View.GONE);
            holder.tvBot.setText(message.getText());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvBot, tvUser;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBot = itemView.findViewById(R.id.tvMessageBot);
            tvUser = itemView.findViewById(R.id.tvMessageUser);
        }
    }
}
