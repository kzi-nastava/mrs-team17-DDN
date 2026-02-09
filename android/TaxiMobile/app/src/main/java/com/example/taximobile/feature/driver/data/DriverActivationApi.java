package com.example.taximobile.feature.driver.data;

import com.example.taximobile.feature.driver.data.dto.request.DriverActivateAccountRequestDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface DriverActivationApi {

    @POST("api/drivers/activation")
    Call<Void> activateDriver(@Body DriverActivateAccountRequestDto request);
}
