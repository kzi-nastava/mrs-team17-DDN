package com.example.taximobile.feature.user.data;

import com.example.taximobile.feature.user.data.dto.request.RideReportRequestDto;
import com.example.taximobile.feature.user.data.dto.response.RideReportResponseDto;
import com.example.taximobile.feature.user.data.dto.response.RideTrackingResponseDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.POST;

public interface RideActiveApi {

    @GET("api/rides/active/tracking")
    Call<RideTrackingResponseDto> getMyActiveTracking();

    @GET("api/rides/{rideId}/tracking")
    Call<RideTrackingResponseDto> getRideTrackingById(@Path("rideId") long rideId);

    @POST("api/rides/active/reports")
    Call<RideReportResponseDto> reportMyActiveRide(@Body RideReportRequestDto req);
}
