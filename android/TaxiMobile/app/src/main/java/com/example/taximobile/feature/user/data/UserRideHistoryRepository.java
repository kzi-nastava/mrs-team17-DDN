package com.example.taximobile.feature.user.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.user.data.dto.response.PassengerRideHistoryResponseDto;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRideHistoryRepository {

    private final UserRideHistoryApi api;

    public UserRideHistoryRepository(Context ctx) {
        this.api = ApiClient.get(ctx).create(UserRideHistoryApi.class);
    }

    public interface ListCb {
        void onSuccess(List<PassengerRideHistoryResponseDto> items);
        void onError(String msg);
    }

    public void getRides(String fromIso, String toIso, ListCb cb) {
        api.getMyRideHistory(fromIso, toIso).enqueue(new Callback<List<PassengerRideHistoryResponseDto>>() {
            @Override
            public void onResponse(
                    Call<List<PassengerRideHistoryResponseDto>> call,
                    Response<List<PassengerRideHistoryResponseDto>> res
            ) {
                if (!res.isSuccessful()) {
                    cb.onError("HTTP " + res.code());
                    return;
                }
                List<PassengerRideHistoryResponseDto> body = res.body();
                cb.onSuccess(body != null ? body : Collections.emptyList());
            }

            @Override
            public void onFailure(Call<List<PassengerRideHistoryResponseDto>> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }
}
