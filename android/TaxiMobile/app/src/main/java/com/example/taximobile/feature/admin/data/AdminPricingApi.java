package com.example.taximobile.feature.admin.data;

import com.example.taximobile.feature.admin.data.dto.response.AdminPricingResponseDto;
import com.example.taximobile.feature.admin.data.dto.request.AdminPricingUpdateRequestDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;

public interface AdminPricingApi {

    @GET("/api/admin/pricing")
    Call<AdminPricingResponseDto> get();

    @PUT("/api/admin/pricing")
    Call<Void> update(@Body AdminPricingUpdateRequestDto body);
}
