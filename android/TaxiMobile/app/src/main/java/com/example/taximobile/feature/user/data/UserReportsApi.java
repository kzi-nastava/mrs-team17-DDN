package com.example.taximobile.feature.user.data;

import com.example.taximobile.feature.user.data.dto.response.RideStatsReportResponseDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UserReportsApi {

    @GET("api/reports/rides")
    Call<RideStatsReportResponseDto> getMyRideReport(
            @Query("from") String fromIsoDate,
            @Query("to") String toIsoDate
    );
}
