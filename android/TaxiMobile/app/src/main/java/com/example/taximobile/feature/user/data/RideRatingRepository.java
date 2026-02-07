package com.example.taximobile.feature.user.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.user.data.dto.request.RideRatingRequestDto;
import com.example.taximobile.feature.user.data.dto.response.PendingRideRatingResponseDto;
import com.example.taximobile.feature.user.data.dto.response.RideRatingResponseDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideRatingRepository {

    private final RideRatingApi api;

    public RideRatingRepository(Context ctx) {
        this.api = ApiClient.get(ctx).create(RideRatingApi.class);
    }

    public interface PendingCb {
        void onSuccess(long rideId);
        void onEmpty();
        void onError(String msg);
    }

    public interface SubmitCb {
        void onSuccess(RideRatingResponseDto dto);
        void onError(String msg);
    }

    public void getPending(PendingCb cb) {
        api.getPendingRide().enqueue(new Callback<PendingRideRatingResponseDto>() {
            @Override
            public void onResponse(
                    Call<PendingRideRatingResponseDto> call,
                    Response<PendingRideRatingResponseDto> res
            ) {
                if (res.isSuccessful()) {
                    PendingRideRatingResponseDto body = res.body();
                    if (body != null && body.getRideId() != null) {
                        cb.onSuccess(body.getRideId());
                    } else {
                        cb.onEmpty();
                    }
                    return;
                }

                if (res.code() == 404) {
                    cb.onEmpty();
                    return;
                }

                cb.onError("HTTP " + res.code());
            }

            @Override
            public void onFailure(Call<PendingRideRatingResponseDto> call, Throwable t) {
                cb.onError(t != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void submitRating(long rideId, int driverRating, int vehicleRating, String comment, SubmitCb cb) {
        RideRatingRequestDto req = new RideRatingRequestDto(driverRating, vehicleRating, comment);

        api.submitRating(rideId, req).enqueue(new Callback<RideRatingResponseDto>() {
            @Override
            public void onResponse(Call<RideRatingResponseDto> call, Response<RideRatingResponseDto> res) {
                if (res.isSuccessful() && res.body() != null) {
                    cb.onSuccess(res.body());
                    return;
                }

                cb.onError("Failed to submit rating (" + res.code() + ")");
            }

            @Override
            public void onFailure(Call<RideRatingResponseDto> call, Throwable t) {
                cb.onError(t != null ? t.getMessage() : "Network error");
            }
        });
    }
}
