package com.example.taximobile.feature.support.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.feature.support.data.dto.response.ChatMessageResponseDto;

import java.util.List;

public class ChatMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VT_ADMIN = 1;
    private static final int VT_USER  = 2;

    private final List<ChatMessageResponseDto> items;

    public ChatMessagesAdapter(List<ChatMessageResponseDto> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessageResponseDto m = items.get(position);
        String role = m != null ? m.getSenderRole() : null;
        return "ADMIN".equalsIgnoreCase(role) ? VT_ADMIN : VT_USER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == VT_ADMIN)
                ? R.layout.item_chat_message_admin
                : R.layout.item_chat_message_user;

        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessageResponseDto m = items.get(position);
        VH vh = (VH) holder;

        vh.tvContent.setText(m != null && m.getContent() != null ? m.getContent() : "");
        vh.tvTime.setText(m != null && m.getSentAt() != null ? m.getSentAt() : "");
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;
        VH(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
