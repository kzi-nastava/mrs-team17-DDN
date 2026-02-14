package com.example.taximobile.core.push;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PushTokenApi {

    @POST("api/notifications/me/device-token")
    Call<Void> register(@Body RegisterPushTokenRequest body);
}
