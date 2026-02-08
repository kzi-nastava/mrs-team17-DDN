package com.example.taximobile.feature.admin.data;

import com.example.taximobile.feature.admin.data.dto.request.AdminCreateDriverRequestDto;
import com.example.taximobile.feature.admin.data.dto.response.AdminCreateDriverResponseDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AdminCreateDriverApi {

    @POST("/api/admin/drivers")
    Call<AdminCreateDriverResponseDto> createDriver(@Body AdminCreateDriverRequestDto body);
}
