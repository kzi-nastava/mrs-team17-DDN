package com.example.taximobile.feature.user.data;

import com.example.taximobile.feature.user.data.dto.request.RideRatingRequestDto;
import com.example.taximobile.feature.user.data.dto.response.PendingRideRatingResponseDto;
import com.example.taximobile.feature.user.data.dto.response.RideRatingResponseDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RideRatingApi {

    @GET("api/rides/rate/pending")
    Call<PendingRideRatingResponseDto> getPendingRide();

    @POST("api/rides/{rideId}/rating")
    Call<RideRatingResponseDto> submitRating(
            @Path("rideId") long rideId,
            @Body RideRatingRequestDto request
    );
}
