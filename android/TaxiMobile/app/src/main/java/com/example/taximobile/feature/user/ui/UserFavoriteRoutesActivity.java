package com.example.taximobile.feature.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.feature.user.adapter.FavoriteRoutesAdapter;
import com.example.taximobile.feature.user.data.FavoriteRouteRepository;
import com.example.taximobile.feature.user.data.dto.response.FavoriteRoutePointResponseDto;
import com.example.taximobile.feature.user.data.dto.response.FavoriteRouteResponseDto;

import java.util.ArrayList;
import java.util.List;

public class UserFavoriteRoutesActivity extends UserBaseActivity {

    private final List<FavoriteRouteResponseDto> items = new ArrayList<>();
    private FavoriteRoutesAdapter adapter;
    private FavoriteRouteRepository repo;

    private ProgressBar progress;
    private TextView empty;
    private RecyclerView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_user_favorite_routes);
        toolbar.setTitle(getString(R.string.title_favorite_routes));

        repo = new FavoriteRouteRepository(this);

        progress = v.findViewById(R.id.frProgress);
        empty = v.findViewById(R.id.frEmpty);
        list = v.findViewById(R.id.frList);

        adapter = new FavoriteRoutesAdapter(
                items,
                this::orderAgain,
                this::removeFavorite
        );

        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        loadFavorites();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        setLoading(true);
        repo.listFavorites(new FavoriteRouteRepository.ListCb() {
            @Override
            public void onSuccess(List<FavoriteRouteResponseDto> listItems) {
                runOnUiThread(() -> {
                    items.clear();
                    if (listItems != null) items.addAll(listItems);
                    adapter.notifyDataSetChanged();
                    setLoading(false);
                    empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onError(String msg, int httpCode) {
                runOnUiThread(() -> {
                    setLoading(false);
                    empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                    Toast.makeText(UserFavoriteRoutesActivity.this,
                            msg != null ? msg : getString(R.string.error_generic),
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    private void removeFavorite(FavoriteRouteResponseDto item) {
        if (item == null || item.getId() == null || item.getId() <= 0) return;

        long favId = item.getId();
        repo.deleteFavorite(favId, new FavoriteRouteRepository.DeleteCb() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    int idx = indexOfFavoriteId(favId);
                    if (idx >= 0) {
                        items.remove(idx);
                        adapter.notifyItemRemoved(idx);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                    Toast.makeText(UserFavoriteRoutesActivity.this,
                            getString(R.string.favourite_route_removed_msg),
                            Toast.LENGTH_SHORT
                    ).show();
                });
            }

            @Override
            public void onError(String msg, int httpCode) {
                runOnUiThread(() -> Toast.makeText(
                        UserFavoriteRoutesActivity.this,
                        msg != null ? msg : getString(R.string.error_generic),
                        Toast.LENGTH_LONG
                ).show());
            }
        });
    }

    private int indexOfFavoriteId(long favId) {
        for (int i = 0; i < items.size(); i++) {
            FavoriteRouteResponseDto d = items.get(i);
            if (d != null && d.getId() != null && d.getId() == favId) return i;
        }
        return -1;
    }

    private void orderAgain(FavoriteRouteResponseDto item) {
        if (item == null) return;

        FavoriteRoutePointResponseDto s = item.getStart();
        FavoriteRoutePointResponseDto d = item.getDestination();
        if (s == null || d == null) {
            Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_SHORT).show();
            return;
        }

        Intent i = new Intent(this, UserOrderRideActivity.class);

        i.putExtra(UserOrderRideActivity.EXTRA_PREFILL_START_ADDRESS, safe(s.getAddress()));
        i.putExtra(UserOrderRideActivity.EXTRA_PREFILL_START_LAT, safeDouble(s.getLat()));
        i.putExtra(UserOrderRideActivity.EXTRA_PREFILL_START_LNG, safeDouble(s.getLng()));

        i.putExtra(UserOrderRideActivity.EXTRA_PREFILL_DEST_ADDRESS, safe(d.getAddress()));
        i.putExtra(UserOrderRideActivity.EXTRA_PREFILL_DEST_LAT, safeDouble(d.getLat()));
        i.putExtra(UserOrderRideActivity.EXTRA_PREFILL_DEST_LNG, safeDouble(d.getLng()));

        ArrayList<String> stopAddrs = new ArrayList<>();
        double[] stopLats = new double[item.getStops() != null ? item.getStops().size() : 0];
        double[] stopLngs = new double[item.getStops() != null ? item.getStops().size() : 0];

        if (item.getStops() != null) {
            for (int idx = 0; idx < item.getStops().size(); idx++) {
                FavoriteRoutePointResponseDto p = item.getStops().get(idx);
                stopAddrs.add(p != null ? safe(p.getAddress()) : "");
                stopLats[idx] = p != null ? safeDouble(p.getLat()) : 0.0;
                stopLngs[idx] = p != null ? safeDouble(p.getLng()) : 0.0;
            }
        }

        if (!stopAddrs.isEmpty()) {
            i.putStringArrayListExtra(UserOrderRideActivity.EXTRA_PREFILL_STOP_ADDRESSES, stopAddrs);
            i.putExtra(UserOrderRideActivity.EXTRA_PREFILL_STOP_LATS, stopLats);
            i.putExtra(UserOrderRideActivity.EXTRA_PREFILL_STOP_LNGS, stopLngs);
        }

        startActivity(i);
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        list.setVisibility(loading ? View.GONE : View.VISIBLE);
        if (loading) empty.setVisibility(View.GONE);
    }

    private String safe(String s) {
        if (s == null) return "";
        String t = s.trim();
        return t.isEmpty() ? "" : t;
    }

    private double safeDouble(Double d) {
        return d != null ? d : 0.0;
    }
}
