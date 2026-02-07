package com.example.taximobile.feature.user.data;

import com.example.taximobile.feature.common.data.dto.response.ProfileImageUploadResponseDto;
import com.example.taximobile.feature.user.data.dto.request.UpdateUserProfileRequestDto;
import com.example.taximobile.feature.user.data.dto.response.UserProfileResponseDto;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface UserProfileApi {

    @GET("api/users/{userId}/profile")
    Call<UserProfileResponseDto> getUserProfile(@Path("userId") long userId);

    @PUT("api/users/{userId}/profile")
    Call<Void> updateUserProfile(
            @Path("userId") long userId,
            @Body UpdateUserProfileRequestDto request
    );

    @Multipart
    @POST("api/users/{userId}/profile-image")
    Call<ProfileImageUploadResponseDto> uploadUserProfileImage(
            @Path("userId") long userId,
            @Part MultipartBody.Part file
    );
}
