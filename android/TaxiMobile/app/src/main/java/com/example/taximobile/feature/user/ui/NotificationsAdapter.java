package com.example.taximobile.feature.user.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.feature.user.data.dto.response.NotificationResponseDto;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.List;
import java.util.TimeZone;

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
        Context ctx = h.itemView.getContext();

        String title = safeTitle(n.getTitle());
        String message = n.getMessage() != null ? n.getMessage().trim() : "";
        String createdAtLabel = formatCreatedAt(ctx, n.getCreatedAt());

        h.title.setText(title);
        h.msg.setText(message);
        h.meta.setText(createdAtLabel);

        TypeBadge badge = typeBadge(ctx, n.getType());
        h.typeChip.setText(badge.text);
        h.typeChip.setBackgroundResource(badge.backgroundResId);

        boolean unread = (n.getReadAt() == null);
        h.title.setTypeface(null, unread ? Typeface.BOLD : Typeface.NORMAL);
        h.msg.setAlpha(unread ? 1f : 0.84f);
        h.unreadIndicator.setVisibility(unread ? View.VISIBLE : View.INVISIBLE);
        h.card.setStrokeWidth(unread ? dp(h.itemView, 2) : dp(h.itemView, 1));
        h.card.setStrokeColor(Color.parseColor(unread ? "#6CC29D" : "#464143"));

        h.itemView.setOnClickListener(x -> {
            if (cb != null) cb.onClick(n, position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        MaterialCardView card;
        View unreadIndicator;
        TextView title;
        TextView typeChip;
        TextView msg;
        TextView meta;

        VH(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.nCard);
            unreadIndicator = itemView.findViewById(R.id.nUnreadIndicator);
            title = itemView.findViewById(R.id.nTitle);
            typeChip = itemView.findViewById(R.id.nTypeChip);
            msg = itemView.findViewById(R.id.nMsg);
            meta = itemView.findViewById(R.id.nMeta);
        }
    }

    private static int dp(View view, int dp) {
        return Math.round(dp * view.getResources().getDisplayMetrics().density);
    }

    private static String safeTitle(String value) {
        if (value == null || value.trim().isEmpty()) return "(no title)";
        return value.trim();
    }

    private static TypeBadge typeBadge(Context context, String typeRaw) {
        String type = normalize(typeRaw);

        if (type.contains("PANIC") || type.contains("CANCEL") || type.contains("ERROR")) {
            return new TypeBadge(
                    context.getString(R.string.notifications_type_alert),
                    R.drawable.bg_notification_chip_alert
            );
        }
        if (type.contains("FINISH") || type.contains("DONE") || type.contains("COMPLET")) {
            return new TypeBadge(
                    context.getString(R.string.notifications_type_done),
                    R.drawable.bg_notification_chip_success
            );
        }
        if (type.contains("RIDE")
                || type.contains("DRIVER")
                || type.contains("ETA")
                || type.contains("ACCEPT")
                || type.contains("START")
                || type.contains("ARRIVE")) {
            return new TypeBadge(
                    context.getString(R.string.notifications_type_ride),
                    R.drawable.bg_notification_chip_neutral
            );
        }

        return new TypeBadge(
                context.getString(R.string.notifications_type_update),
                R.drawable.bg_notification_chip_neutral
        );
    }

    private static String normalize(String value) {
        if (value == null) return "";
        return value.trim().toUpperCase(Locale.US);
    }

    private static String formatCreatedAt(Context context, String createdAtRaw) {
        Date createdAt = parseIsoDate(createdAtRaw);
        if (createdAt == null) {
            return context.getString(R.string.notifications_time_fallback);
        }

        long nowMs = System.currentTimeMillis();
        long diffMs = Math.max(0L, nowMs - createdAt.getTime());
        long minuteMs = 60_000L;
        long hourMs = 3_600_000L;

        if (diffMs < minuteMs) {
            return context.getString(R.string.notifications_time_just_now);
        }
        if (diffMs < hourMs) {
            long mins = Math.max(1L, diffMs / minuteMs);
            return context.getString(R.string.notifications_time_min_ago, mins);
        }
        if (diffMs < 24L * hourMs) {
            long hours = Math.max(1L, diffMs / hourMs);
            return context.getString(R.string.notifications_time_hour_ago, hours);
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
        String today = dayFormat.format(new Date(nowMs));
        String yesterday = dayFormat.format(new Date(nowMs - 24L * hourMs));
        String createdDay = dayFormat.format(createdAt);

        if (TextUtils.equals(createdDay, yesterday) && !TextUtils.equals(createdDay, today)) {
            return context.getString(
                    R.string.notifications_time_yesterday,
                    timeFormat.format(createdAt)
            );
        }

        SimpleDateFormat full = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return full.format(createdAt);
    }

    private static Date parseIsoDate(String raw) {
        if (raw == null || raw.trim().isEmpty()) return null;
        String candidate = normalizeFraction(raw.trim());

        String[] patterns = new String[]{
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss"
        };

        for (String pattern : patterns) {
            Date parsed = tryParse(candidate, pattern);
            if (parsed != null) return parsed;
        }
        return null;
    }

    private static Date tryParse(String value, String pattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
            format.setLenient(false);
            if (pattern.endsWith("'Z'")) {
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
            }
            return format.parse(value);
        } catch (Exception ignore) {
            return null;
        }
    }

    private static String normalizeFraction(String value) {
        int tIndex = value.indexOf('T');
        int dotIndex = value.indexOf('.', tIndex >= 0 ? tIndex : 0);
        if (dotIndex < 0) return value;

        int zIndex = value.indexOf('Z', dotIndex);
        int plusIndex = value.indexOf('+', dotIndex);
        int minusIndex = value.indexOf('-', dotIndex);

        int zoneIndex = -1;
        if (zIndex >= 0) zoneIndex = zIndex;
        if (plusIndex >= 0 && (zoneIndex < 0 || plusIndex < zoneIndex)) zoneIndex = plusIndex;
        if (minusIndex >= 0 && (zoneIndex < 0 || minusIndex < zoneIndex)) zoneIndex = minusIndex;
        if (zoneIndex < 0) zoneIndex = value.length();

        String fraction = value.substring(dotIndex + 1, zoneIndex);
        if (fraction.length() <= 3) return value;

        return value.substring(0, dotIndex + 1)
                + fraction.substring(0, 3)
                + value.substring(zoneIndex);
    }

    private static final class TypeBadge {
        final String text;
        final int backgroundResId;

        TypeBadge(String text, int backgroundResId) {
            this.text = text;
            this.backgroundResId = backgroundResId;
        }
    }
}
