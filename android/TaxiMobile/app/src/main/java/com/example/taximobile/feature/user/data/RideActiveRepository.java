package com.example.taximobile.feature.user.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.user.data.dto.request.RideReportRequestDto;
import com.example.taximobile.feature.user.data.dto.response.RideReportResponseDto;
import com.example.taximobile.feature.user.data.dto.response.RideTrackingResponseDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideActiveRepository {

    private final RideActiveApi api;

    public RideActiveRepository(Context ctx) {
        api = ApiClient.get(ctx).create(RideActiveApi.class);
    }

    public interface TrackingCb {
        void onSuccess(RideTrackingResponseDto dto);
        void onNoActiveRide();          // <-- novo
        void onError(String msg);
    }

    public interface ReportCb {
        void onSuccess(RideReportResponseDto dto);
        void onError(String msg);
    }

    public void getTracking(TrackingCb cb) {
        api.getMyActiveTracking().enqueue(new Callback<RideTrackingResponseDto>() {
            @Override
            public void onResponse(Call<RideTrackingResponseDto> call, Response<RideTrackingResponseDto> res) {
                if (res.isSuccessful() && res.body() != null) {
                    cb.onSuccess(res.body());
                    return;
                }

                // tipično: 404 / 400 kad nema aktivne vožnje
                if (res.code() == 404 || res.code() == 400) {
                    cb.onNoActiveRide();
                    return;
                }

                cb.onError("Failed to load tracking (" + res.code() + ")");
            }

            @Override
            public void onFailure(Call<RideTrackingResponseDto> call, Throwable t) {
                cb.onError(t != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void report(String text, ReportCb cb) {
        api.reportMyActiveRide(new RideReportRequestDto(text)).enqueue(new Callback<RideReportResponseDto>() {
            @Override
            public void onResponse(Call<RideReportResponseDto> call, Response<RideReportResponseDto> res) {
                if (res.isSuccessful() && res.body() != null) cb.onSuccess(res.body());
                else cb.onError("Failed to submit report (" + res.code() + ")");
            }

            @Override
            public void onFailure(Call<RideReportResponseDto> call, Throwable t) {
                cb.onError(t != null ? t.getMessage() : "Network error");
            }
        });
    }
}
