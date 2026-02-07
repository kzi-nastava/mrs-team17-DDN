package com.example.taximobile.feature.user.ui;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.feature.user.data.dto.response.NotificationResponseDto;

import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.VH> {

    public interface OnNotificationClick {
        void onClick(NotificationResponseDto n, int position);
    }

    private final OnNotificationClick cb;
    private final List<NotificationResponseDto> items;

    public NotificationsAdapter(List<NotificationResponseDto> items, OnNotificationClick cb) {
        this.items = (items != null) ? items : new ArrayList<>();
        this.cb = cb;
    }

    public void setItems(List<NotificationResponseDto> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void markReadAt(int position) {
        if (position < 0 || position >= items.size()) return;
        NotificationResponseDto old = items.get(position);

        NotificationResponseDto n = new NotificationResponseDto();
        n.setId(old.getId());
        n.setType(old.getType());
        n.setTitle(old.getTitle());
        n.setMessage(old.getMessage());
        n.setLinkUrl(old.getLinkUrl());
        n.setCreatedAt(old.getCreatedAt());
        n.setReadAt("now"); // samo indikator za UI

        items.set(position, n);
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        NotificationResponseDto n = items.get(position);

        h.title.setText(n.getTitle() != null ? n.getTitle() : "(no title)");
        h.msg.setText(n.getMessage() != null ? n.getMessage() : "");
        h.meta.setText(n.getCreatedAt() != null ? n.getCreatedAt() : "");

        boolean unread = (n.getReadAt() == null);
        h.title.setTypeface(null, unread ? Typeface.BOLD : Typeface.NORMAL);

        h.itemView.setOnClickListener(x -> {
            if (cb != null) cb.onClick(n, position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView title;
        TextView msg;
        TextView meta;

        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.nTitle);
            msg = itemView.findViewById(R.id.nMsg);
            meta = itemView.findViewById(R.id.nMeta);
        }
    }
}
