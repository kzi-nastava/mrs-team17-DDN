package com.example.taximobile.feature.admin.ui;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.feature.admin.data.dto.response.AdminUserStatusResponseDto;

import java.util.List;

public class AdminUserStatusAdapter extends RecyclerView.Adapter<AdminUserStatusAdapter.VH> {

    public interface Actions {
        void onOpenDialog(AdminUserStatusResponseDto user, boolean initialBlocked);
        void onQuickUnblock(AdminUserStatusResponseDto user);
    }

    private final List<AdminUserStatusResponseDto> items;
    private final Actions actions;

    public AdminUserStatusAdapter(List<AdminUserStatusResponseDto> items, Actions actions) {
        this.items = items;
        this.actions = actions;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user_status, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        AdminUserStatusResponseDto u = items.get(position);

        String name = u != null ? safe(u.displayName()) : "";
        String email = u != null ? safe(u.getEmail()) : "";
        boolean blocked = u != null && u.isBlocked();
        String reason = u != null ? safe(u.getBlockReason()) : "";

        h.tvName.setText(name);
        h.tvEmail.setText(email);

        if (blocked) {
            h.tvStatus.setText("BLOCKED");
            h.tvStatus.setBackgroundResource(R.drawable.bg_chip_error);
            h.tvStatus.setTextColor(Color.parseColor("#C62828"));
        } else {
            h.tvStatus.setText("ACTIVE");
            h.tvStatus.setBackgroundResource(R.drawable.bg_chip_success);
            h.tvStatus.setTextColor(ContextCompat.getColor(h.tvStatus.getContext(), R.color.green));
        }

        if (blocked) {
            h.tvNote.setVisibility(View.VISIBLE);
            h.tvNote.setText(!reason.trim().isEmpty() ? ("Note: " + reason.trim()) : "Note: (not provided)");
        } else {
            h.tvNote.setVisibility(View.GONE);
        }

        if (blocked) {
            h.btnPrimary.setText("Unblock");
            h.btnPrimary.setBackgroundResource(R.drawable.bg_button_primary_user_green);
            h.btnSecondary.setVisibility(View.VISIBLE);
            h.btnSecondary.setText("Edit note");
        } else {
            h.btnPrimary.setText("Block");
            h.btnPrimary.setBackgroundResource(R.drawable.bg_button_primary_user_red);
            h.btnSecondary.setVisibility(View.GONE);
        }

        h.btnPrimary.setOnClickListener(v -> {
            if (actions == null || u == null) return;
            if (blocked) actions.onQuickUnblock(u);
            else actions.onOpenDialog(u, true);
        });

        h.btnSecondary.setOnClickListener(v -> {
            if (actions == null || u == null) return;
            actions.onOpenDialog(u, true);
        });

        h.itemView.setOnClickListener(v -> {
            if (actions == null || u == null) return;
            actions.onOpenDialog(u, blocked);
        });
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvStatus, tvNote;
        Button btnPrimary, btnSecondary;

        public VH(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.tvName);
            tvEmail = v.findViewById(R.id.tvEmail);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvNote = v.findViewById(R.id.tvNote);
            btnPrimary = v.findViewById(R.id.btnPrimary);
            btnSecondary = v.findViewById(R.id.btnSecondary);
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
