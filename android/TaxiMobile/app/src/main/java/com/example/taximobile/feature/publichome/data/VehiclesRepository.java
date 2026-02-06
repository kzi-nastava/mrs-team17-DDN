package com.example.taximobile.feature.publichome.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.publichome.data.dto.response.ActiveVehicleResponseDto;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehiclesRepository {

    private final VehiclesApi api;

    public VehiclesRepository(Context ctx) {
        this.api = ApiClient.get(ctx).create(VehiclesApi.class);
    }

    public interface ListCb {
        void onSuccess(List<ActiveVehicleResponseDto> items);
        void onError(String msg);
    }

    public void getActiveVehicles(Double minLat, Double maxLat, Double minLng, Double maxLng, ListCb cb) {
        api.getActiveVehicles(minLat, maxLat, minLng, maxLng).enqueue(new Callback<List<ActiveVehicleResponseDto>>() {
            @Override
            public void onResponse(Call<List<ActiveVehicleResponseDto>> call, Response<List<ActiveVehicleResponseDto>> res) {
                if (!res.isSuccessful()) { cb.onError("HTTP " + res.code()); return; }
                List<ActiveVehicleResponseDto> body = res.body();
                cb.onSuccess(body != null ? body : Collections.emptyList());
            }

            @Override
            public void onFailure(Call<List<ActiveVehicleResponseDto>> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }
}
