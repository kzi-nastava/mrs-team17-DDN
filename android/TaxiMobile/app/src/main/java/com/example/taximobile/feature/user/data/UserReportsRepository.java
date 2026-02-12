package com.example.taximobile.feature.user.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.user.data.dto.response.RideStatsReportResponseDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserReportsRepository {

    private final UserReportsApi api;

    public UserReportsRepository(Context ctx) {
        this.api = ApiClient.get(ctx).create(UserReportsApi.class);
    }

    public interface ReportCb {
        void onSuccess(RideStatsReportResponseDto res);
        void onError(String msg);
    }

    public void getMyRideReport(String fromIso, String toIso, ReportCb cb) {
        api.getMyRideReport(fromIso, toIso).enqueue(new Callback<RideStatsReportResponseDto>() {
            @Override
            public void onResponse(Call<RideStatsReportResponseDto> call, Response<RideStatsReportResponseDto> res) {
                if (!res.isSuccessful()) {
                    cb.onError("HTTP " + res.code());
                    return;
                }
                RideStatsReportResponseDto body = res.body();
                if (body == null) {
                    cb.onError("Empty response");
                    return;
                }
                cb.onSuccess(body);
            }

            @Override
            public void onFailure(Call<RideStatsReportResponseDto> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }
}
