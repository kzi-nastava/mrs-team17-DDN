package com.example.taximobile.feature.user.data;

import com.example.taximobile.feature.user.data.dto.request.CreateRideRequestDto;
import com.example.taximobile.feature.user.data.dto.response.CreateRideResponseDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RideOrderApi {

    @POST("api/rides")
    Call<CreateRideResponseDto> createRide(@Body CreateRideRequestDto request);
}
