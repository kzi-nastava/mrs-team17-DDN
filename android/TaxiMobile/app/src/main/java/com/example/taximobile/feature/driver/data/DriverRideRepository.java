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
    public interface ActiveRideCb {
        void onSuccess(DriverRideDetailsResponseDto dto);
        void onEmpty();
        void onError(String msg);
    }

    public interface FinishCb {
        void onSuccess();
        void onError(String msg);
    }

    public void getActiveRide(ActiveRideCb cb) {
        api.getActiveRide().enqueue(new Callback<DriverRideDetailsResponseDto>() {
            @Override
            public void onResponse(Call<DriverRideDetailsResponseDto> call, Response<DriverRideDetailsResponseDto> res) {
                if (res.code() == 404 || res.code() == 204) { cb.onEmpty(); return; }
                if (!res.isSuccessful()) { cb.onError("HTTP " + res.code()); return; }
                DriverRideDetailsResponseDto body = res.body();
                if (body == null || body.getRideId() == null) { cb.onEmpty(); return; }
                cb.onSuccess(body);
            }

            @Override
            public void onFailure(Call<DriverRideDetailsResponseDto> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void finishRide(long rideId, FinishCb cb) {
        api.finishRide(rideId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> res) {
                if (!res.isSuccessful()) { cb.onError("HTTP " + res.code()); return; }
                cb.onSuccess();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
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
