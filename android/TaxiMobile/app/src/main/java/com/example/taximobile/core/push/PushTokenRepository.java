package com.example.taximobile.core.push;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PushTokenRepository {

    public interface VoidCb {
        void onSuccess();
        void onError(String msg);
    }

    private final PushTokenApi api;

    public PushTokenRepository(Context context) {
        this.api = ApiClient.get(context.getApplicationContext()).create(PushTokenApi.class);
    }

    public void registerToken(String token, String platform, VoidCb cb) {
        if (token == null || token.trim().isEmpty()) {
            if (cb != null) cb.onError("Missing token");
            return;
        }

        RegisterPushTokenRequest req = new RegisterPushTokenRequest(
                token.trim(),
                (platform == null || platform.trim().isEmpty()) ? "ANDROID" : platform.trim().toUpperCase()
        );

        api.register(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    if (cb != null) cb.onSuccess();
                    return;
                }
                if (cb != null) cb.onError("HTTP " + response.code());
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (cb != null) {
                    cb.onError(t != null && t.getMessage() != null ? t.getMessage() : "Network error");
                }
            }
        });
    }
}
