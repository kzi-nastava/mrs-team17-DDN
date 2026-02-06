package com.example.taximobile.feature.admin.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.feature.admin.data.dto.response.AdminRideStatusRowDto;

import java.util.List;
import java.util.Locale;

public class AdminRideStatusAdapter extends RecyclerView.Adapter<AdminRideStatusAdapter.VH> {

    public interface OnClick {
        void onClick(AdminRideStatusRowDto row);
    }

    private final List<AdminRideStatusRowDto> items;
    private final OnClick onClick;

    public AdminRideStatusAdapter(List<AdminRideStatusRowDto> items, OnClick onClick) {
        this.items = items;
        this.onClick = onClick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_ride_status, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AdminRideStatusRowDto r = items.get(position);

        h.tvTitle.setText("Ride #" + r.getRideId());
        h.tvDriver.setText("Driver: " + r.driverDisplayName());

        String status = safe(r.getStatus());
        String startedAt = safe(r.getStartedAt());

        String line1 = "Status: " + (status.isEmpty() ? "-" : status);
        String line2 = "Start: " + (startedAt.isEmpty() ? "-" : shortIso(startedAt));
        String line3 = String.format(Locale.US, "Car: %.5f, %.5f", r.getCarLat(), r.getCarLng());

        h.tvMeta.setText(line1 + "\n" + line2 + "\n" + line3);

        h.itemView.setOnClickListener(v -> {
            if (onClick != null) onClick.onClick(r);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDriver, tvMeta;
        public VH(View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvTitle);
            tvDriver = v.findViewById(R.id.tvDriver);
            tvMeta = v.findViewById(R.id.tvMeta);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static String shortIso(String iso) {
        try {
            String s = iso.replace("T", " ");
            if (s.length() >= 16) return s.substring(0, 16);
            return s;
        } catch (Exception e) {
            return iso;
        }
    }
}
