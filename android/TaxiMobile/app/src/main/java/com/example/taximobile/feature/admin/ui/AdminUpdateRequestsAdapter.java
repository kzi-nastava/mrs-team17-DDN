package com.example.taximobile.feature.admin.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.feature.driver.data.dto.response.DriverProfileResponseDto;
import com.example.taximobile.feature.driver.data.dto.response.ProfileChangeRequestResponseDto;
import com.example.taximobile.feature.driver.data.dto.response.UserProfileResponseDto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AdminUpdateRequestsAdapter extends RecyclerView.Adapter<AdminUpdateRequestsAdapter.VH> {

    public interface Listener {
        void onApproveClicked(ProfileChangeRequestResponseDto item);
        void onRejectClicked(ProfileChangeRequestResponseDto item);
    }

    private final Listener listener;

    private final List<ProfileChangeRequestResponseDto> items = new ArrayList<>();
    private final Map<Long, DriverProfileResponseDto> currentProfilesByDriverId = new HashMap<>();
    private final Set<Long> actingRequestIds = new HashSet<>();

    public AdminUpdateRequestsAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<ProfileChangeRequestResponseDto> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setCurrentProfile(long driverId, DriverProfileResponseDto profile) {
        currentProfilesByDriverId.put(driverId, profile);
        notifyDataSetChanged();
    }

    public void setActing(long requestId, boolean acting) {
        if (acting) actingRequestIds.add(requestId);
        else actingRequestIds.remove(requestId);
        notifyDataSetChanged();
    }

    public void replaceItem(ProfileChangeRequestResponseDto updated) {
        if (updated == null || updated.getRequestId() == null) return;
        long id = updated.getRequestId();
        for (int i = 0; i < items.size(); i++) {
            ProfileChangeRequestResponseDto it = items.get(i);
            if (it != null && it.getRequestId() != null && it.getRequestId() == id) {
                items.set(i, updated);
                notifyItemChanged(i);
                return;
            }
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_update_request, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        ProfileChangeRequestResponseDto r = items.get(position);
        if (r == null) return;

        Long requestId = r.getRequestId();
        Long driverId = r.getDriverId();

        h.tvMain.setText(
                String.format(Locale.US, "Driver #%s · Request #%s",
                        driverId != null ? driverId : "?",
                        requestId != null ? requestId : "?"
                )
        );

        String created = formatCreatedAt(r.getCreatedAt());
        String status = safe(r.getStatus());
        h.tvMeta.setText("Created: " + created + " • Status: " + statusLabel(status));
        h.tvStatus.setText(statusLabel(status));

        DriverProfileResponseDto curProfile = null;
        if (driverId != null) {
            curProfile = currentProfilesByDriverId.get(driverId);
        }

        String changesText = buildChangesText(curProfile, r);
        h.tvChanges.setText(changesText);

        boolean pending = "PENDING".equalsIgnoreCase(status);
        boolean acting = requestId != null && actingRequestIds.contains(requestId);

        h.btnApprove.setEnabled(pending && !acting);
        h.btnReject.setEnabled(pending && !acting);

        h.btnApprove.setText(acting
                ? h.itemView.getContext().getString(R.string.update_requests_working)
                : h.itemView.getContext().getString(R.string.update_requests_approve));

        h.btnReject.setText(acting
                ? h.itemView.getContext().getString(R.string.update_requests_working)
                : h.itemView.getContext().getString(R.string.update_requests_reject));

        h.btnApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApproveClicked(r);
        });
        h.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onRejectClicked(r);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tvMain;
        TextView tvMeta;
        TextView tvStatus;
        TextView tvChanges;
        Button btnApprove;
        Button btnReject;

        VH(@NonNull View itemView) {
            super(itemView);
            tvMain = itemView.findViewById(R.id.tvMain);
            tvMeta = itemView.findViewById(R.id.tvMeta);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvChanges = itemView.findViewById(R.id.tvChanges);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }

    private String buildChangesText(DriverProfileResponseDto current, ProfileChangeRequestResponseDto req) {
        if (req == null) return "";

        if (current == null || current.getDriver() == null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Requested values:\n");
            sb.append("• First name: ").append(orDash(req.getFirstName())).append("\n");
            sb.append("• Last name: ").append(orDash(req.getLastName())).append("\n");
            sb.append("• Address: ").append(orDash(req.getAddress())).append("\n");
            sb.append("• Phone: ").append(orDash(req.getPhoneNumber())).append("\n");
            sb.append("• Image: ").append(orDash(req.getProfileImageUrl()));
            return sb.toString();
        }

        UserProfileResponseDto cur = current.getDriver();

        List<String> lines = new ArrayList<>();
        addDiff(lines, "First name", cur.getFirstName(), req.getFirstName());
        addDiff(lines, "Last name", cur.getLastName(), req.getLastName());
        addDiff(lines, "Address", cur.getAddress(), req.getAddress());
        addDiff(lines, "Phone", cur.getPhoneNumber(), req.getPhoneNumber());
        addDiff(lines, "Image", cur.getProfileImageUrl(), req.getProfileImageUrl());

        if (lines.isEmpty()) {
            return "No actual changes compared to current profile.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Requested changes:\n");
        for (int i = 0; i < lines.size(); i++) {
            sb.append("• ").append(lines.get(i));
            if (i < lines.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    private void addDiff(List<String> out, String field, String cur, String req) {
        String c = safe(cur);
        String r = safe(req);

        if (c.equals(r)) return;
        if (c.trim().isEmpty() && r.trim().isEmpty()) return;

        out.add(field + ": " + orDash(c) + " → " + orDash(r));
    }

    private String formatCreatedAt(String createdAt) {
        String s = safe(createdAt).trim();
        if (s.isEmpty()) return "—";

        int t = s.indexOf('T');
        if (t > 0 && t < s.length() - 1) {
            String date = s.substring(0, t);
            String time = s.substring(t + 1);
            if (time.length() >= 5) time = time.substring(0, 5);
            return date + " " + time;
        }
        return s;
    }

    private String statusLabel(String status) {
        String s = safe(status).trim().toUpperCase(Locale.ROOT);
        if (s.isEmpty()) return "UNKNOWN";
        if (s.equals("PENDING")) return "PENDING";
        if (s.equals("APPROVED")) return "APPROVED";
        if (s.equals("REJECTED")) return "REJECTED";
        return s;
    }

    private String orDash(String s) {
        String v = safe(s).trim();
        return v.isEmpty() ? "—" : v;
    }

    private String safe(String s) {
        return s != null ? s : "";
    }
}
