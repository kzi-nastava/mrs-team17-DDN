package com.example.taximobile.feature.admin.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.driver.data.dto.response.ProfileChangeRequestResponseDto;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUpdateRequestsRepository {

    private final AdminUpdateRequestsApi api;

    public AdminUpdateRequestsRepository(Context ctx) {
        api = ApiClient.get(ctx.getApplicationContext()).create(AdminUpdateRequestsApi.class);
    }

    public interface ListCb {
        void onSuccess(List<ProfileChangeRequestResponseDto> items);
        void onError(String msg, int httpCode);
    }

    public interface ActionCb {
        void onSuccess(ProfileChangeRequestResponseDto updated);
        void onError(String msg, int httpCode);
    }

    public void list(String status, ListCb cb) {
        api.listRequests(status).enqueue(new Callback<List<ProfileChangeRequestResponseDto>>() {
            @Override
            public void onResponse(Call<List<ProfileChangeRequestResponseDto>> call,
                                   Response<List<ProfileChangeRequestResponseDto>> res) {
                if (!res.isSuccessful()) {
                    cb.onError("HTTP " + res.code(), res.code());
                    return;
                }
                List<ProfileChangeRequestResponseDto> body = res.body();
                if (body == null) body = Collections.emptyList();
                cb.onSuccess(body);
            }

            @Override
            public void onFailure(Call<List<ProfileChangeRequestResponseDto>> call, Throwable t) {
                cb.onError(t != null && t.getMessage() != null ? t.getMessage() : "Network error", -1);
            }
        });
    }

    public void approve(long requestId, String note, ActionCb cb) {
        api.approve(requestId, note).enqueue(new Callback<ProfileChangeRequestResponseDto>() {
            @Override
            public void onResponse(Call<ProfileChangeRequestResponseDto> call,
                                   Response<ProfileChangeRequestResponseDto> res) {
                if (!res.isSuccessful()) {
                    cb.onError("HTTP " + res.code(), res.code());
                    return;
                }
                ProfileChangeRequestResponseDto body = res.body();
                if (body == null) body = new ProfileChangeRequestResponseDto();
                cb.onSuccess(body);
            }

            @Override
            public void onFailure(Call<ProfileChangeRequestResponseDto> call, Throwable t) {
                cb.onError(t != null && t.getMessage() != null ? t.getMessage() : "Network error", -1);
            }
        });
    }

    public void reject(long requestId, String reason, ActionCb cb) {
        api.reject(requestId, reason).enqueue(new Callback<ProfileChangeRequestResponseDto>() {
            @Override
            public void onResponse(Call<ProfileChangeRequestResponseDto> call,
                                   Response<ProfileChangeRequestResponseDto> res) {
                if (!res.isSuccessful()) {
                    cb.onError("HTTP " + res.code(), res.code());
                    return;
                }
                ProfileChangeRequestResponseDto body = res.body();
                if (body == null) body = new ProfileChangeRequestResponseDto();
                cb.onSuccess(body);
            }

            @Override
            public void onFailure(Call<ProfileChangeRequestResponseDto> call, Throwable t) {
                cb.onError(t != null && t.getMessage() != null ? t.getMessage() : "Network error", -1);
            }
        });
    }
}
