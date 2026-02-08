package com.example.taximobile.feature.user.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.user.data.dto.request.CreateRideRequestDto;
import com.example.taximobile.feature.user.data.dto.request.RoutePreviewRequestDto;
import com.example.taximobile.feature.user.data.dto.response.CreateRideResponseDto;
import com.example.taximobile.feature.user.data.dto.response.RoutePreviewResponseDto;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideOrderRepository {

    private final RideOrderApi rideApi;
    private final RoutingApi routingApi;

    public RideOrderRepository(Context ctx) {
        Context app = ctx.getApplicationContext();
        rideApi = ApiClient.get(app).create(RideOrderApi.class);
        routingApi = ApiClient.get(app).create(RoutingApi.class);
    }

    public static class ApiError {
        public final String message;
        public final String blockReason;
        public final int httpCode;

        public ApiError(String message, String blockReason, int httpCode) {
            this.message = message;
            this.blockReason = blockReason;
            this.httpCode = httpCode;
        }
    }

    public interface CreateCb {
        void onSuccess(CreateRideResponseDto dto);
        void onError(ApiError err);
    }

    public interface PreviewCb {
        void onSuccess(RoutePreviewResponseDto dto);
        void onError(String msg, int httpCode);
    }

    public void createRide(CreateRideRequestDto req, CreateCb cb) {
        rideApi.createRide(req).enqueue(new Callback<CreateRideResponseDto>() {
            @Override
            public void onResponse(Call<CreateRideResponseDto> call, Response<CreateRideResponseDto> res) {
                if (!res.isSuccessful()) {
                    ApiError err = parseError(res.errorBody(), res.code());
                    cb.onError(err);
                    return;
                }
                CreateRideResponseDto body = res.body();
                if (body == null) {
                    cb.onError(new ApiError("Empty response", null, res.code()));
                    return;
                }
                cb.onSuccess(body);
            }

            @Override
            public void onFailure(Call<CreateRideResponseDto> call, Throwable t) {
                cb.onError(new ApiError(t != null && t.getMessage() != null ? t.getMessage() : "Network error", null, -1));
            }
        });
    }

    public void previewRoute(RoutePreviewRequestDto req, PreviewCb cb) {
        routingApi.previewRoute(req).enqueue(new Callback<RoutePreviewResponseDto>() {
            @Override
            public void onResponse(Call<RoutePreviewResponseDto> call, Response<RoutePreviewResponseDto> res) {
                if (!res.isSuccessful()) {
                    cb.onError("HTTP " + res.code(), res.code());
                    return;
                }
                RoutePreviewResponseDto body = res.body();
                if (body == null) {
                    cb.onError("Empty response", res.code());
                    return;
                }
                cb.onSuccess(body);
            }

            @Override
            public void onFailure(Call<RoutePreviewResponseDto> call, Throwable t) {
                cb.onError(t != null && t.getMessage() != null ? t.getMessage() : "Network error", -1);
            }
        });
    }

    private ApiError parseError(ResponseBody body, int http) {
        String fallback = "Request failed (HTTP " + http + ")";
        if (body == null) return new ApiError(fallback, null, http);

        try {
            String s = body.string();
            if (s == null || s.trim().isEmpty()) return new ApiError(fallback, null, http);

            JSONObject obj = new JSONObject(s);
            String msg = obj.optString("message", fallback);
            String reason = obj.optString("blockReason", null);
            if (reason != null && reason.trim().isEmpty()) reason = null;
            return new ApiError(msg, reason, http);

        } catch (Exception e) {
            return new ApiError(fallback, null, http);
        } finally {
            try { body.close(); } catch (Exception ignore) {}
        }
    }
}
