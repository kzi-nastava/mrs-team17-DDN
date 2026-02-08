package com.example.taximobile.feature.user.data;

import com.example.taximobile.feature.user.data.dto.request.RoutePreviewRequestDto;
import com.example.taximobile.feature.user.data.dto.response.RoutePreviewResponseDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RoutingApi {

    @POST("api/routing/route")
    Call<RoutePreviewResponseDto> previewRoute(@Body RoutePreviewRequestDto request);
}
