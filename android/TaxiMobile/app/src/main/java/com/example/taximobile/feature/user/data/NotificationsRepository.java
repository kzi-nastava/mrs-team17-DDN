package com.example.taximobile.feature.user.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.user.data.dto.response.NotificationResponseDto;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsRepository {

    private final NotificationsApi api;

    public NotificationsRepository(Context ctx) {
        this.api = ApiClient.get(ctx).create(NotificationsApi.class);
    }

    public interface ListCb {
        void onSuccess(List<NotificationResponseDto> items);
        void onError(String msg);
    }

    public interface CountCb {
        void onSuccess(long count);
        void onError(String msg);
    }

    public interface VoidCb {
        void onSuccess();
        void onError(String msg);
    }

    public void list(int limit, ListCb cb) {
        api.myNotifications(limit).enqueue(new Callback<List<NotificationResponseDto>>() {
            @Override
            public void onResponse(
                    Call<List<NotificationResponseDto>> call,
                    Response<List<NotificationResponseDto>> res
            ) {
                if (!res.isSuccessful()) {
                    cb.onError("HTTP " + res.code());
                    return;
                }
                List<NotificationResponseDto> body = res.body();
                cb.onSuccess(body != null ? body : Collections.emptyList());
            }

            @Override
            public void onFailure(Call<List<NotificationResponseDto>> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void unreadCount(CountCb cb) {
        api.myUnreadCount().enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> res) {
                if (!res.isSuccessful()) {
                    cb.onError("HTTP " + res.code());
                    return;
                }
                Long body = res.body();
                cb.onSuccess(body != null ? body : 0L);
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void markRead(long id, VoidCb cb) {
        api.markRead(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> res) {
                if (!res.isSuccessful()) {
                    cb.onError("HTTP " + res.code());
                    return;
                }
                cb.onSuccess();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }
}
