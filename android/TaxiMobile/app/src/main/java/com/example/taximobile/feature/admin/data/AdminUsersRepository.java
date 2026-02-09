package com.example.taximobile.feature.admin.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.admin.data.dto.request.AdminSetUserBlockRequestDto;
import com.example.taximobile.feature.admin.data.dto.response.AdminUserStatusResponseDto;

import org.json.JSONObject;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminUsersRepository {

    private final AdminUsersApi api;

    public AdminUsersRepository(Context ctx) {
        Context app = ctx.getApplicationContext();
        api = ApiClient.get(app).create(AdminUsersApi.class);
    }

    public interface ListCb {
        void onSuccess(List<AdminUserStatusResponseDto> list);
        void onError(String msg, int httpCode);
    }

    public interface SaveCb {
        void onSuccess(AdminUserStatusResponseDto updated);
        void onError(String msg, int httpCode);
    }

    public void listUsersWithStatus(String role, String query, int limit, ListCb cb) {
        String q = query != null && !query.trim().isEmpty() ? query.trim() : null;
        int lim = limit <= 0 ? 200 : limit;

        api.listUsersWithStatus(role, q, lim).enqueue(new Callback<List<AdminUserStatusResponseDto>>() {
            @Override
            public void onResponse(Call<List<AdminUserStatusResponseDto>> call, Response<List<AdminUserStatusResponseDto>> res) {
                if (!res.isSuccessful()) {
                    String msg = parseErrorMessage(res.errorBody(), res.code());
                    cb.onError(msg, res.code());
                    return;
                }
                cb.onSuccess(res.body());
            }

            @Override
            public void onFailure(Call<List<AdminUserStatusResponseDto>> call, Throwable t) {
                cb.onError(t != null && t.getMessage() != null ? t.getMessage() : "Network error", -1);
            }
        });
    }

    public void setBlockStatus(long userId, boolean blocked, String reason, SaveCb cb) {
        String r = null;
        if (blocked) {
            if (reason != null && !reason.trim().isEmpty()) r = reason.trim();
        }

        AdminSetUserBlockRequestDto body = new AdminSetUserBlockRequestDto(blocked, r);

        api.setBlockStatus(userId, body).enqueue(new Callback<AdminUserStatusResponseDto>() {
            @Override
            public void onResponse(Call<AdminUserStatusResponseDto> call, Response<AdminUserStatusResponseDto> res) {
                if (!res.isSuccessful()) {
                    String msg = parseErrorMessage(res.errorBody(), res.code());
                    cb.onError(msg, res.code());
                    return;
                }
                AdminUserStatusResponseDto dto = res.body();
                if (dto == null) {
                    cb.onError("Empty response", res.code());
                    return;
                }
                cb.onSuccess(dto);
            }

            @Override
            public void onFailure(Call<AdminUserStatusResponseDto> call, Throwable t) {
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
