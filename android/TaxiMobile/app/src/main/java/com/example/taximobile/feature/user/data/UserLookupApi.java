package com.example.taximobile.feature.user.data;

import com.example.taximobile.feature.user.data.dto.response.UserLookupStatusResponseDto;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UserLookupApi {

    @GET("api/users/lookup")
    Call<UserLookupStatusResponseDto> lookupByEmail(@Query("email") String email);
}
