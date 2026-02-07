package com.example.taximobile.feature.auth.data;

import com.example.taximobile.feature.auth.data.dto.request.ChangePasswordRequestDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AccountApi {

    @POST("api/account/change-password")
    Call<Void> changePassword(@Body ChangePasswordRequestDto request);
}
