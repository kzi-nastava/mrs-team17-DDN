package com.example.taximobile.feature.user.data;

import com.example.taximobile.feature.user.data.dto.response.PassengerRideHistoryResponseDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface UserRideHistoryApi {

    @GET("api/rides/history")
    Call<List<PassengerRideHistoryResponseDto>> getMyRideHistory(
            @Query("from") String fromIsoDate,
            @Query("to") String toIsoDate
    );
}
