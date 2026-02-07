package com.example.taximobile.feature.user.data;

import android.content.Context;

import com.example.taximobile.core.auth.JwtUtils;
import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.core.network.TokenStorage;
import com.example.taximobile.feature.user.data.dto.response.AddFavoriteFromRideResponseDto;
import com.example.taximobile.feature.user.data.dto.response.FavoriteRouteResponseDto;

import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteRouteRepository {

    private final FavoriteRouteApi api;
    private final TokenStorage tokenStorage;

    public FavoriteRouteRepository(Context ctx) {
        this.api = ApiClient.get(ctx).create(FavoriteRouteApi.class);
        this.tokenStorage = new TokenStorage(ctx.getApplicationContext());
    }

    public interface AddCb {
        void onSuccess(AddFavoriteFromRideResponseDto dto);
        void onError(String msg, int httpCode);
    }

    public interface ListCb {
        void onSuccess(List<FavoriteRouteResponseDto> items);
        void onError(String msg, int httpCode);
    }

    public void listFavorites(ListCb cb) {
        Long userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            cb.onError("Session expired. Please login again.", 401);
            return;
        }

        api.listFavorites(userId).enqueue(new Callback<List<FavoriteRouteResponseDto>>() {
            @Override
            public void onResponse(
                    Call<List<FavoriteRouteResponseDto>> call,
                    Response<List<FavoriteRouteResponseDto>> res
            ) {
                if (res.isSuccessful()) {
                    List<FavoriteRouteResponseDto> body = res.body();
                    cb.onSuccess(body != null ? body : Collections.emptyList());
                    return;
                }

                cb.onError(readErrorMessage(res), res.code());
            }

            @Override
            public void onFailure(Call<List<FavoriteRouteResponseDto>> call, Throwable t) {
                cb.onError(t != null && t.getMessage() != null ? t.getMessage() : "Network error", 0);
            }
        });
    }

    public void addFromRide(long rideId, AddCb cb) {
        Long userId = getCurrentUserId();
        if (userId == null || userId <= 0) {
            cb.onError("Session expired. Please login again.", 401);
            return;
        }

        api.addFromRide(userId, rideId).enqueue(new Callback<AddFavoriteFromRideResponseDto>() {
            @Override
            public void onResponse(
                    Call<AddFavoriteFromRideResponseDto> call,
                    Response<AddFavoriteFromRideResponseDto> res
            ) {
                if (res.isSuccessful() && res.body() != null) {
                    cb.onSuccess(res.body());
                    return;
                }

                cb.onError(readErrorMessage(res), res.code());
            }

            @Override
            public void onFailure(Call<AddFavoriteFromRideResponseDto> call, Throwable t) {
                cb.onError(t != null && t.getMessage() != null ? t.getMessage() : "Network error", 0);
            }
        });
    }

    private Long getCurrentUserId() {
        String token = tokenStorage.getToken();
        if (token == null || token.isBlank()) return null;
        return JwtUtils.getUserIdFromSub(token);
    }

    private String readErrorMessage(Response<?> res) {
        if (res == null) return "Unknown error";

        String fallback = "Request failed (" + res.code() + ")";

        try {
            if (res.errorBody() == null) return fallback;
            String raw = res.errorBody().string();
            if (raw == null || raw.isBlank()) return fallback;

            JSONObject json = new JSONObject(raw);
            String msg = json.optString("message", null);
            if (msg == null || msg.isBlank()) return fallback;
            return msg;
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
