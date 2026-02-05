package com.example.taximobile.feature.auth.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.core.network.TokenStorage;
import com.example.taximobile.feature.auth.data.dto.request.LoginRequest;
import com.example.taximobile.feature.auth.data.dto.response.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private final AuthApi api;
    private final TokenStorage tokenStorage;

    public interface LoginCb {
        void onSuccess();
        void onError(String msg);
    }

    public AuthRepository(Context ctx) {
        this.api = ApiClient.get(ctx).create(AuthApi.class);
        this.tokenStorage = new TokenStorage(ctx.getApplicationContext());
    }

    public void login(String email, String password, LoginCb cb) {
        api.login(new LoginRequest(email, password)).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> res) {
                if (!res.isSuccessful()) {
                    cb.onError("HTTP " + res.code());
                    return;
                }

                LoginResponse body = res.body();
                if (body == null || body.getToken() == null || body.getToken().isBlank()) {
                    cb.onError("Missing token");
                    return;
                }

                tokenStorage.saveToken(body.getToken());
                cb.onSuccess();
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }
}
