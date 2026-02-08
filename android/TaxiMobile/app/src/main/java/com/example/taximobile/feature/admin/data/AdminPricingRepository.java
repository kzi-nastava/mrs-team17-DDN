package com.example.taximobile.feature.admin.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.admin.data.dto.response.AdminPricingResponseDto;
import com.example.taximobile.feature.admin.data.dto.request.AdminPricingUpdateRequestDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminPricingRepository {

    private final AdminPricingApi api;

    public AdminPricingRepository(Context ctx) {
        api = ApiClient.get(ctx).create(AdminPricingApi.class);
    }

    public interface GetCb {
        void onSuccess(AdminPricingResponseDto dto);
        void onError(String msg);
    }

    public interface SaveCb {
        void onSuccess();
        void onError(String msg);
    }

    public void get(GetCb cb) {
        api.get().enqueue(new Callback<AdminPricingResponseDto>() {
            @Override
            public void onResponse(
                    Call<AdminPricingResponseDto> call,
                    Response<AdminPricingResponseDto> res
            ) {
                if (res.isSuccessful() && res.body() != null) {
                    cb.onSuccess(res.body());
                } else {
                    cb.onError("Failed to load pricing");
                }
            }

            @Override
            public void onFailure(
                    Call<AdminPricingResponseDto> call,
                    Throwable t
            ) {
                cb.onError(t != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void update(AdminPricingUpdateRequestDto req, SaveCb cb) {
        api.update(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(
                    Call<Void> call,
                    Response<Void> res
            ) {
                if (res.isSuccessful()) {
                    cb.onSuccess();
                } else {
                    cb.onError("Save failed");
                }
            }

            @Override
            public void onFailure(
                    Call<Void> call,
                    Throwable t
            ) {
                cb.onError(t != null ? t.getMessage() : "Network error");
            }
        });
    }
}
