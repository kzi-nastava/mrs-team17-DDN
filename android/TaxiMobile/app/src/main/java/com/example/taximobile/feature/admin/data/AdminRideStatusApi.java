package com.example.taximobile.feature.admin.data;

import com.example.taximobile.feature.admin.data.dto.response.AdminRideStatusRowDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AdminRideStatusApi {

    @GET("api/admin/ride-status")
    Call<List<AdminRideStatusRowDto>> list(
            @Query("q") String q,
            @Query("limit") int limit
    );
}
