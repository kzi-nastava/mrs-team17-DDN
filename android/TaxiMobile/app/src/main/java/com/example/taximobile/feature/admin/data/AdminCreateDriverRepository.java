package com.example.taximobile.feature.admin.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.admin.data.dto.request.AdminCreateDriverRequestDto;
import com.example.taximobile.feature.admin.data.dto.response.AdminCreateDriverResponseDto;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminCreateDriverRepository {

    private final AdminCreateDriverApi api;

    public AdminCreateDriverRepository(Context ctx) {
        this.api = ApiClient.get(ctx.getApplicationContext()).create(AdminCreateDriverApi.class);
    }

    public interface Cb {
        void onSuccess(AdminCreateDriverResponseDto dto);
        void onError(String msg, int httpCode);
    }

    public void createDriver(AdminCreateDriverRequestDto req, Cb cb) {
        api.createDriver(req).enqueue(new Callback<AdminCreateDriverResponseDto>() {
            @Override
            public void onResponse(Call<AdminCreateDriverResponseDto> call, Response<AdminCreateDriverResponseDto> res) {
                if (res.isSuccessful() && res.body() != null) {
                    cb.onSuccess(res.body());
                    return;
                }

                String msg = extractMessage(res);
                if (msg == null || msg.trim().isEmpty()) {
                    msg = "HTTP " + res.code();
                }
                cb.onError(msg, res.code());
            }

            @Override
            public void onFailure(Call<AdminCreateDriverResponseDto> call, Throwable t) {
                cb.onError(t != null && t.getMessage() != null ? t.getMessage() : "Network error", -1);
            }
        });
    }

    private static String extractMessage(Response<?> res) {
        try {
            if (res.errorBody() == null) return null;
            String raw = res.errorBody().string();
            if (raw == null || raw.trim().isEmpty()) return null;

            JSONObject obj = new JSONObject(raw);
            return obj.optString("message", null);
        } catch (Exception ignored) {
            return null;
        }
    }
}
