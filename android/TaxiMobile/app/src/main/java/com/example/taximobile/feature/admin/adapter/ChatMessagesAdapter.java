package com.example.taximobile.feature.admin.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.feature.support.data.dto.response.ChatMessageResponseDto;

import java.util.List;

public class ChatMessagesAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ADMIN = 1;
    private static final int TYPE_USER = 2;

    private final List<ChatMessageResponseDto> items;

    public ChatMessagesAdapter(List<ChatMessageResponseDto> items) {
        this.items = items;
    }

    @Override
    public int getItemViewType(int position) {
        return "ADMIN".equals(items.get(position).getSenderRole())
                ? TYPE_ADMIN : TYPE_USER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        int layout = viewType == TYPE_ADMIN
                ? R.layout.item_chat_admin
                : R.layout.item_chat_user;

        View v = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);

        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
        ChatMessageResponseDto m = items.get(pos);
        VH vh = (VH) h;
        vh.tvContent.setText(m.getContent());
        vh.tvTime.setText(m.getSentAt().toString());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;
        VH(View v) {
            super(v);
            tvContent = v.findViewById(R.id.tvContent);
            tvTime = v.findViewById(R.id.tvTime);
        }
    }
}
