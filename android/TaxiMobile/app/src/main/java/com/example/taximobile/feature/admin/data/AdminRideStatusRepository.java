package com.example.taximobile.feature.admin.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.admin.data.dto.response.AdminRideStatusRowDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminRideStatusRepository {

    private final AdminRideStatusApi api;

    public AdminRideStatusRepository(Context ctx) {
        api = ApiClient.get(ctx).create(AdminRideStatusApi.class);
    }

    public interface ListCb {
        void onSuccess(List<AdminRideStatusRowDto> list);
        void onError(String msg);
    }

    public void list(String q, int limit, ListCb cb) {
        api.list(q, limit).enqueue(new Callback<List<AdminRideStatusRowDto>>() {
            @Override
            public void onResponse(
                    Call<List<AdminRideStatusRowDto>> call,
                    Response<List<AdminRideStatusRowDto>> res
            ) {
                if (res.isSuccessful() && res.body() != null) cb.onSuccess(res.body());
                else cb.onError("Failed to load ride status");
            }

            @Override
            public void onFailure(Call<List<AdminRideStatusRowDto>> call, Throwable t) {
                cb.onError(t != null ? t.getMessage() : "Network error");
            }
        });
    }
}
