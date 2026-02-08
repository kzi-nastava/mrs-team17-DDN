package com.example.taximobile.feature.driver.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.user.data.dto.response.RideTrackingResponseDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverRideTrackingRepository {

    private final DriverRideTrackingApi api;

    public DriverRideTrackingRepository(Context ctx) {
        api = ApiClient.get(ctx).create(DriverRideTrackingApi.class);
    }

    public interface TrackingCb {
        void onSuccess(RideTrackingResponseDto dto);
        void onError(String msg);
    }

    public void getTracking(long rideId, TrackingCb cb) {
        api.getRideTracking(rideId).enqueue(new Callback<RideTrackingResponseDto>() {
            @Override
            public void onResponse(Call<RideTrackingResponseDto> call, Response<RideTrackingResponseDto> res) {
                if (!res.isSuccessful() || res.body() == null) {
                    cb.onError("Failed to load tracking (" + res.code() + ")");
                    return;
                }
                cb.onSuccess(res.body());
            }

            @Override
            public void onFailure(Call<RideTrackingResponseDto> call, Throwable t) {
                cb.onError(t != null && t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }
}
