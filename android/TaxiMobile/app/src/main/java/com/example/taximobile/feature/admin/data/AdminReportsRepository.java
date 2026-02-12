package com.example.taximobile.feature.admin.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.user.data.dto.response.RideStatsReportResponseDto;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminReportsRepository {

    private final AdminReportsApi api;

    public AdminReportsRepository(Context ctx) {
        api = ApiClient.get(ctx.getApplicationContext()).create(AdminReportsApi.class);
    }

    public interface ReportCb {
        void onSuccess(RideStatsReportResponseDto res);
        void onError(String msg, int httpCode);
    }

    public void getAdminRideReport(String role, Long userId, String fromIso, String toIso, ReportCb cb) {
        api.getAdminRideReport(role, userId, fromIso, toIso).enqueue(new Callback<RideStatsReportResponseDto>() {
            @Override
            public void onResponse(Call<RideStatsReportResponseDto> call, Response<RideStatsReportResponseDto> res) {
                if (!res.isSuccessful()) {
                    cb.onError(parseErrorMessage(res.errorBody(), res.code()), res.code());
                    return;
                }
                RideStatsReportResponseDto body = res.body();
                if (body == null) {
                    cb.onError("Empty response", res.code());
                    return;
                }
                cb.onSuccess(body);
            }

            @Override
            public void onFailure(Call<RideStatsReportResponseDto> call, Throwable t) {
                cb.onError(t != null && t.getMessage() != null ? t.getMessage() : "Network error", -1);
            }
        });
    }

    private static String parseErrorMessage(ResponseBody body, int http) {
        String fallback = "Request failed (HTTP " + http + ")";
        if (body == null) return fallback;

        try {
            String s = body.string();
            if (s == null || s.trim().isEmpty()) return fallback;

            JSONObject obj = new JSONObject(s);
            String msg = obj.optString("message", fallback);
            if (msg == null || msg.trim().isEmpty()) return fallback;
            return msg;

        } catch (Exception ignored) {
            return fallback;
        } finally {
            try { body.close(); } catch (Exception ignore) {}
        }
    }
}
