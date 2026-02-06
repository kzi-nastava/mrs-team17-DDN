// app/src/main/java/com/example/taximobile/feature/driver/data/DriverRideRepository.java
package com.example.taximobile.feature.driver.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.driver.data.dto.response.DriverRideDetailsResponseDto;
import com.example.taximobile.feature.driver.data.dto.response.DriverRideHistoryResponseDto;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverRideRepository {

    private final DriverApi api;

    public DriverRideRepository(Context ctx) {
        this.api = ApiClient.get(ctx).create(DriverApi.class);
    }

    public interface ListCb {
        void onSuccess(List<DriverRideHistoryResponseDto> items);
        void onError(String msg);
    }

    public interface DetailsCb {
        void onSuccess(DriverRideDetailsResponseDto dto);
        void onError(String msg);
    }

    public void getRides(String fromIso, String toIso, ListCb cb) {
        api.getDriverRides(fromIso, toIso).enqueue(new Callback<List<DriverRideHistoryResponseDto>>() {
            @Override
            public void onResponse(
                    Call<List<DriverRideHistoryResponseDto>> call,
                    Response<List<DriverRideHistoryResponseDto>> res
            ) {
                if (!res.isSuccessful()) {
                    cb.onError("HTTP " + res.code());
                    return;
                }
                List<DriverRideHistoryResponseDto> body = res.body();
                cb.onSuccess(body != null ? body : Collections.emptyList());
            }

            @Override
            public void onFailure(Call<List<DriverRideHistoryResponseDto>> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void getRideDetails(long rideId, DetailsCb cb) {
        api.getDriverRideDetails(rideId).enqueue(new Callback<DriverRideDetailsResponseDto>() {
            @Override
            public void onResponse(
                    Call<DriverRideDetailsResponseDto> call,
                    Response<DriverRideDetailsResponseDto> res
            ) {
                if (!res.isSuccessful()) {
                    cb.onError("HTTP " + res.code());
                    return;
                }
                DriverRideDetailsResponseDto body = res.body();
                if (body == null) {
                    cb.onError("Empty body");
                    return;
                }
                cb.onSuccess(body);
            }

            @Override
            public void onFailure(Call<DriverRideDetailsResponseDto> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }
}
