package com.example.taximobile.feature.driver.data;

import com.example.taximobile.feature.user.data.dto.response.RideTrackingResponseDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface DriverRideTrackingApi {

    @GET("api/rides/{rideId}/tracking")
    Call<RideTrackingResponseDto> getRideTracking(@Path("rideId") long rideId);
}
