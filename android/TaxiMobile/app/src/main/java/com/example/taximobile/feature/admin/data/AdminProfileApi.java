package com.example.taximobile.feature.admin.data;

import com.example.taximobile.feature.admin.data.dto.request.UpdateAdminProfileRequestDto;
import com.example.taximobile.feature.admin.data.dto.response.AdminProfileResponseDto;
import com.example.taximobile.feature.common.data.dto.response.ProfileImageUploadResponseDto;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface AdminProfileApi {

    @GET("api/admins/{adminId}/profile")
    Call<AdminProfileResponseDto> getAdminProfile(@Path("adminId") long adminId);

    @PUT("api/admins/{adminId}/profile")
    Call<Void> updateAdminProfile(
            @Path("adminId") long adminId,
            @Body UpdateAdminProfileRequestDto request
    );

    @Multipart
    @POST("api/admins/{adminId}/profile-image")
    Call<ProfileImageUploadResponseDto> uploadProfileImage(
            @Path("adminId") long adminId,
            @Part MultipartBody.Part file
    );
}
