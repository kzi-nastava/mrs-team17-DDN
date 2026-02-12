package com.example.taximobile.feature.admin.data;

import com.example.taximobile.feature.admin.data.dto.request.AdminSetUserBlockRequestDto;
import com.example.taximobile.feature.admin.data.dto.response.AdminUserOptionResponseDto;
import com.example.taximobile.feature.admin.data.dto.response.AdminUserStatusResponseDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AdminUsersApi {

    @GET("/api/admin/users/status")
    Call<List<AdminUserStatusResponseDto>> listUsersWithStatus(
            @Query("role") String role,
            @Query("query") String query,
            @Query("limit") int limit
    );

    @GET("/api/admin/users")
    Call<List<AdminUserOptionResponseDto>> listUserOptions(
            @Query("role") String role,
            @Query("query") String query,
            @Query("limit") int limit
    );

    @PUT("/api/admin/users/{userId}/block")
    Call<AdminUserStatusResponseDto> setBlockStatus(
            @Path("userId") long userId,
            @Body AdminSetUserBlockRequestDto body
    );
}
