package com.example.taximobile.feature.user.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.feature.user.data.FavoriteRouteKeyUtil;
import com.example.taximobile.feature.user.data.dto.response.PassengerRideHistoryResponseDto;

import java.util.List;
import java.util.Set;

public class UserRideHistoryAdapter extends RecyclerView.Adapter<UserRideHistoryAdapter.Holder> {

    public interface OnRateClickListener {
        void onRateClick(long rideId);
    }

    public interface OnItemClickListener {
        void onItemClick(PassengerRideHistoryResponseDto item);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(long rideId, int position);
    }

    private final List<PassengerRideHistoryResponseDto> items;
    private final OnRateClickListener rateListener;
    private final OnItemClickListener itemListener;
    private final OnFavoriteClickListener favoriteListener;
    private final Set<String> favoriteRouteKeys;

    public UserRideHistoryAdapter(
            List<PassengerRideHistoryResponseDto> items,
            OnRateClickListener rateListener,
            OnItemClickListener itemListener,
            OnFavoriteClickListener favoriteListener,
            Set<String> favoriteRouteKeys
    ) {
        this.items = items;
        this.rateListener = rateListener;
        this.itemListener = itemListener;
        this.favoriteListener = favoriteListener;
        this.favoriteRouteKeys = favoriteRouteKeys;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_ride_history, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        PassengerRideHistoryResponseDto d = items.get(position);

        long rideId = d.getRideId() != null ? d.getRideId() : 0L;
        h.tvTitle.setText(h.itemView.getContext().getString(R.string.ride_label, rideId));

        String date = formatDate(d.getStartedAt());
        h.tvDate.setText(date.isEmpty() ? "-" : date);

        String route = safe(d.getStartAddress()) + " → " + safe(d.getDestinationAddress());
        h.tvRoute.setText(route);

        String stops = joinStops(d.getStops());
        if (stops.isEmpty()) {
            h.tvStops.setVisibility(View.GONE);
        } else {
            h.tvStops.setVisibility(View.VISIBLE);
            h.tvStops.setText(h.itemView.getContext().getString(R.string.ride_stops_label, stops));
        }

        boolean canRate = rideId > 0;
        h.btnRate.setEnabled(canRate);
        h.btnRate.setOnClickListener(v -> {
            if (rateListener != null && canRate) rateListener.onRateClick(rideId);
        });

        String rideKey = FavoriteRouteKeyUtil.fromRideDto(d);
        boolean alreadyFavorite = rideId > 0
                && favoriteRouteKeys != null
                && favoriteRouteKeys.contains(rideKey);
        h.btnFavorite.setImageResource(alreadyFavorite
                ? R.drawable.ic_star_filled_user
                : R.drawable.ic_star_outline_user);
        h.btnFavorite.setEnabled(!alreadyFavorite && rideId > 0);
        h.btnFavorite.setContentDescription(
                h.itemView.getContext().getString(
                        alreadyFavorite ? R.string.favorite_added_cd : R.string.favorite_add_cd
                )
        );
        h.btnFavorite.setOnClickListener(v -> {
            if (favoriteListener != null && !alreadyFavorite && rideId > 0) {
                favoriteListener.onFavoriteClick(rideId, h.getBindingAdapterPosition());
            }
        });

        h.itemView.setOnClickListener(v -> {
            if (itemListener != null) itemListener.onItemClick(d);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvDate;
        TextView tvRoute;
        TextView tvStops;
        ImageButton btnFavorite;
        Button btnRate;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvRideTitle);
            tvDate = itemView.findViewById(R.id.tvRideDate);
            tvRoute = itemView.findViewById(R.id.tvRideRoute);
            tvStops = itemView.findViewById(R.id.tvRideStops);
            btnFavorite = itemView.findViewById(R.id.btnFavoriteRide);
            btnRate = itemView.findViewById(R.id.btnRateRide);
        }
    }

    private static String safe(String s) {
        return s != null ? s : "-";
    }

    private static String joinStops(List<String> stops) {
        if (stops == null || stops.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stops.size(); i++) {
            if (i > 0) sb.append(" • ");
            sb.append(stops.get(i));
        }
        return sb.toString();
    }

    private static String formatDate(String iso) {
        if (iso == null || iso.length() < 10) return "";
        String date = iso.substring(0, 10); // yyyy-MM-dd
        String[] p = date.split("-");
        if (p.length == 3) {
            return p[2] + "." + p[1] + "." + p[0];
        }
        return date;
    }
}
