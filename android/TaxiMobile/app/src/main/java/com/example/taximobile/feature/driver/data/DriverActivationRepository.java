package com.example.taximobile.feature.driver.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.driver.data.dto.request.DriverActivateAccountRequestDto;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverActivationRepository {

    private final DriverActivationApi api;

    public DriverActivationRepository(Context ctx) {
        this.api = ApiClient.get(ctx.getApplicationContext()).create(DriverActivationApi.class);
    }

    public interface Cb {
        void onSuccess();
        void onError(String msg, int httpCode);
    }

    public void activate(DriverActivateAccountRequestDto req, Cb cb) {
        api.activateDriver(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> res) {
                if (res.isSuccessful()) {
                    cb.onSuccess();
                    return;
                }

                String msg = extractMessage(res);
                if (msg == null || msg.trim().isEmpty()) {
                    msg = "HTTP " + res.code();
                }
                cb.onError(msg, res.code());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
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
