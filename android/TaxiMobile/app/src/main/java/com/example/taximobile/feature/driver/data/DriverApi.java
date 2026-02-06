package com.example.taximobile.feature.driver.data;

import com.example.taximobile.feature.driver.data.dto.response.DriverRideDetailsResponseDto;
import com.example.taximobile.feature.driver.data.dto.response.DriverRideHistoryResponseDto;
import com.example.taximobile.feature.driver.data.dto.request.UpdateDriverProfileRequestDto;
import com.example.taximobile.feature.driver.data.dto.response.DriverProfileResponseDto;
import com.example.taximobile.feature.driver.data.dto.response.ProfileChangeRequestResponseDto;
import com.example.taximobile.feature.common.data.dto.response.ProfileImageUploadResponseDto;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DriverApi {

    @GET("api/driver/rides")
    Call<List<DriverRideHistoryResponseDto>> getDriverRides(
            @Query("from") String fromIsoDate,
            @Query("to") String toIsoDate
    );

    @GET("api/driver/rides/{rideId}")
    Call<DriverRideDetailsResponseDto> getDriverRideDetails(@Path("rideId") long rideId);

    // Driver profile (Student1 - 2.3)
    @GET("api/drivers/{driverId}/profile")
    Call<DriverProfileResponseDto> getDriverProfile(@Path("driverId") long driverId);

    @POST("api/drivers/{driverId}/profile-change-requests")
    Call<ProfileChangeRequestResponseDto> requestProfileChange(
            @Path("driverId") long driverId,
            @Body UpdateDriverProfileRequestDto request
    );

    @Multipart
    @POST("api/drivers/{driverId}/profile-image")
    Call<ProfileImageUploadResponseDto> uploadProfileImage(
            @Path("driverId") long driverId,
            @Part MultipartBody.Part file
    );
}
