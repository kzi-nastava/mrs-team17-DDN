package com.example.taximobile.feature.publichome.data;

import com.example.taximobile.feature.publichome.data.dto.response.ActiveVehicleResponseDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface VehiclesApi {

    @GET("api/vehicles/active")
    Call<List<ActiveVehicleResponseDto>> getActiveVehicles(
            @Query("minLat") Double minLat,
            @Query("maxLat") Double maxLat,
            @Query("minLng") Double minLng,
            @Query("maxLng") Double maxLng
    );
}
