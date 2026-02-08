package com.example.taximobile.feature.user.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.user.data.dto.response.UserLookupStatusResponseDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserLookupRepository {

    private final UserLookupApi api;

    public UserLookupRepository(Context ctx) {
        Context app = ctx.getApplicationContext();
        api = ApiClient.get(app).create(UserLookupApi.class);
    }

    public interface LookupCb {
        void onSuccess(UserLookupStatusResponseDto dto);
        void onError(String msg, int httpCode);
    }

    public void lookupByEmail(String email, LookupCb cb) {
        api.lookupByEmail(email).enqueue(new Callback<UserLookupStatusResponseDto>() {
            @Override
            public void onResponse(Call<UserLookupStatusResponseDto> call, Response<UserLookupStatusResponseDto> res) {
                if (!res.isSuccessful()) {
                    cb.onError("HTTP " + res.code(), res.code());
                    return;
                }
                UserLookupStatusResponseDto body = res.body();
                if (body == null) {
                    cb.onError("Empty response", res.code());
                    return;
                }
                cb.onSuccess(body);
            }

            @Override
            public void onFailure(Call<UserLookupStatusResponseDto> call, Throwable t) {
                cb.onError(t != null && t.getMessage() != null ? t.getMessage() : "Network error", -1);
            }
        });
    }
}
