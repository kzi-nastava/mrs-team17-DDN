package com.example.taximobile.feature.admin.data;

import com.example.taximobile.feature.user.data.dto.response.RideStatsReportResponseDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AdminReportsApi {

    @GET("/api/admin/reports/rides")
    Call<RideStatsReportResponseDto> getAdminRideReport(
            @Query("role") String role,
            @Query("userId") Long userId,
            @Query("from") String fromIsoDate,
            @Query("to") String toIsoDate
    );
}
