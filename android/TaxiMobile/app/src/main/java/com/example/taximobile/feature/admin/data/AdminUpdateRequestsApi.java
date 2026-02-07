package com.example.taximobile.feature.admin.data;

import com.example.taximobile.feature.driver.data.dto.response.ProfileChangeRequestResponseDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AdminUpdateRequestsApi {

    @GET("api/admin/profile-change-requests")
    Call<List<ProfileChangeRequestResponseDto>> listRequests(
            @Query("status") String status
    );

    @POST("api/admin/profile-change-requests/{requestId}/approve")
    Call<ProfileChangeRequestResponseDto> approve(
            @Path("requestId") long requestId,
            @Query("note") String note
    );

    @POST("api/admin/profile-change-requests/{requestId}/reject")
    Call<ProfileChangeRequestResponseDto> reject(
            @Path("requestId") long requestId,
            @Query("reason") String reason
    );
}
