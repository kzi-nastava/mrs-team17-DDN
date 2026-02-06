package com.example.taximobile.feature.auth.data;

import com.example.taximobile.feature.auth.data.dto.request.LoginRequest;
import com.example.taximobile.feature.auth.data.dto.response.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest req);
}
