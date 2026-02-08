package com.example.taximobile.feature.user.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.feature.user.data.dto.response.FavoriteRoutePointResponseDto;
import com.example.taximobile.feature.user.data.dto.response.FavoriteRouteResponseDto;

import java.util.List;

public class FavoriteRoutesAdapter extends RecyclerView.Adapter<FavoriteRoutesAdapter.Holder> {

    public interface OnOrderAgainListener {
        void onOrderAgain(FavoriteRouteResponseDto item);
    }

    public interface OnRemoveListener {
        void onRemove(FavoriteRouteResponseDto item);
    }

    private final List<FavoriteRouteResponseDto> items;
    private final OnOrderAgainListener orderAgainListener;
    private final OnRemoveListener removeListener;

    public FavoriteRoutesAdapter(
            List<FavoriteRouteResponseDto> items,
            OnOrderAgainListener orderAgainListener,
            OnRemoveListener removeListener
    ) {
        this.items = items;
        this.orderAgainListener = orderAgainListener;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_route, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        FavoriteRouteResponseDto d = items.get(position);

        String title = safe(d != null ? d.getName() : null);
        if (title.isEmpty()) {
            long id = d != null && d.getId() != null ? d.getId() : 0L;
            title = h.itemView.getContext().getString(R.string.favourite_route_title_fallback, id);
        }
        h.tvTitle.setText(title);

        String from = safe(d != null && d.getStart() != null ? d.getStart().getAddress() : null);
        String to = safe(d != null && d.getDestination() != null ? d.getDestination().getAddress() : null);
        if (from.isEmpty()) from = "-";
        if (to.isEmpty()) to = "-";
        h.tvRoute.setText(from + " → " + to);

        String stops = joinStops(d != null ? d.getStops() : null);
        if (stops.isEmpty()) {
            h.tvStops.setVisibility(View.GONE);
        } else {
            h.tvStops.setVisibility(View.VISIBLE);
            h.tvStops.setText(h.itemView.getContext().getString(R.string.favourite_route_stops, stops));
        }

        h.btnRemove.setOnClickListener(v -> {
            if (removeListener != null && d != null) removeListener.onRemove(d);
        });

        h.btnOrderAgain.setOnClickListener(v -> {
            if (orderAgainListener != null && d != null) orderAgainListener.onOrderAgain(d);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvRoute;
        TextView tvStops;
        Button btnRemove;
        Button btnOrderAgain;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvFavTitle);
            tvRoute = itemView.findViewById(R.id.tvFavRoute);
            tvStops = itemView.findViewById(R.id.tvFavStops);
            btnRemove = itemView.findViewById(R.id.btnFavRemove);
            btnOrderAgain = itemView.findViewById(R.id.btnFavOrderAgain);
        }
    }

    private static String safe(String s) {
        return s != null ? s.trim() : "";
    }

    private static String joinStops(List<FavoriteRoutePointResponseDto> stops) {
        if (stops == null || stops.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < stops.size(); i++) {
            FavoriteRoutePointResponseDto p = stops.get(i);
            String a = p != null ? safe(p.getAddress()) : "";
            if (a.isEmpty()) continue;
            if (sb.length() > 0) sb.append(" • ");
            sb.append(a);
        }
        return sb.toString();
    }
}
