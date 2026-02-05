// app/src/main/java/com/example/taximobile/feature/driver/data/DriverApi.java
package com.example.taximobile.feature.driver.data;

import com.example.taximobile.feature.driver.data.dto.response.DriverRideDetailsResponseDto;
import com.example.taximobile.feature.driver.data.dto.response.DriverRideHistoryResponseDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
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
}
