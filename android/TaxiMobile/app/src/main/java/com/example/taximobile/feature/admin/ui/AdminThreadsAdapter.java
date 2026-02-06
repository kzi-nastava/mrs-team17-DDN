package com.example.taximobile.feature.admin.ui;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.feature.support.data.dto.response.ChatThreadResponseDto;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminThreadsAdapter extends RecyclerView.Adapter<AdminThreadsAdapter.VH> {

    public interface OnThreadClickListener {
        void onClick(ChatThreadResponseDto t);
    }

    private final List<ChatThreadResponseDto> items;
    private final OnThreadClickListener onClick;

    public AdminThreadsAdapter(List<ChatThreadResponseDto> items,
                               OnThreadClickListener onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_thread, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ChatThreadResponseDto t = items.get(position);

        String userName = t.getUserName();
        String userEmail = t.getUserEmail();

        String title = !isNullOrEmpty(userName) ? userName : userEmail;

        h.tvTitle.setText(title);
        h.tvSubtitle.setText(userEmail != null ? userEmail : "");

        // lastMessageAt je STRING na Android DTO (Gson)
        String lm = t.getLastMessageAt();
        if (!isNullOrEmpty(lm)) {
            h.tvTime.setText(formatIsoToShort(lm));
            h.tvTime.setVisibility(View.VISIBLE);
        } else {
            h.tvTime.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> {
            if (onClick != null) onClick.onClick(t);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        final TextView tvTitle, tvSubtitle, tvTime;

        public VH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvSubtitle = v.findViewById(R.id.tvSubtitle);
            tvTime = v.findViewById(R.id.tvTime);
        }
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    /**
     * Backend šalje ISO string (Spring OffsetDateTime), npr:
     * 2026-02-06T18:42:31.123+01:00
     * Prikaz: dd.MM HH:mm
     */
    private static String formatIsoToShort(String iso) {
        // Ako radi java.time (API 26+ i/ili desugaring), formatiraj normalno
        try {
            OffsetDateTime odt = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                odt = OffsetDateTime.parse(iso);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return odt.format(DateTimeFormatter.ofPattern("dd.MM HH:mm"));
            }
            // Ako iz nekog razloga nema O, fallback ispod
        } catch (Exception ignored) {
        }

        // Fallback bez parsiranja (radi svuda):
        // "2026-02-06T18:42:31..." -> "2026-02-06 18:42"
        try {
            String s = iso.replace("T", " ");
            if (s.length() >= 16) return s.substring(0, 16);
            return s;
        } catch (Exception e) {
            return iso;
        }
    }
}
