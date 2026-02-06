package com.example.taximobile.feature.driver.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.feature.driver.model.Ride;

import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {

    private final List<Ride> rides;
    private final OnRideClickListener listener;

    public interface OnRideClickListener {
        void onRideClick(Ride ride);
    }

    public RideAdapter(List<Ride> rides, OnRideClickListener listener) {
        this.rides = rides;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ride, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        Ride ride = rides.get(position);

        // SUMMARY info for list
        holder.tvDate.setText(ride.getDateStart());
        holder.tvRoute.setText(ride.getRoute());
        holder.tvPrice.setText(ride.getPrice() + " RSD");
        holder.tvStatus.setText(ride.getStatus());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRideClick(ride);
            }
        });
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    static class RideViewHolder extends RecyclerView.ViewHolder {

        TextView tvDate;
        TextView tvRoute;
        TextView tvPrice;
        TextView tvStatus;

        RideViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvRoute = itemView.findViewById(R.id.tvRoute);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
