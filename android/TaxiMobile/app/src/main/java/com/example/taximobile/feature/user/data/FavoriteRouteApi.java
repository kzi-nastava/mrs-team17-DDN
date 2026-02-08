package com.example.taximobile.feature.user.data;

import com.example.taximobile.feature.user.data.dto.response.AddFavoriteFromRideResponseDto;
import com.example.taximobile.feature.user.data.dto.response.FavoriteRouteResponseDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface FavoriteRouteApi {

    @GET("api/users/{userId}/favorite-routes")
    Call<List<FavoriteRouteResponseDto>> listFavorites(@Path("userId") long userId);

    @POST("api/users/{userId}/favorite-routes/from-ride/{rideId}")
    Call<AddFavoriteFromRideResponseDto> addFromRide(
            @Path("userId") long userId,
            @Path("rideId") long rideId
    );

    @DELETE("api/users/{userId}/favorite-routes/{favoriteRouteId}")
    Call<Void> deleteFavorite(
            @Path("userId") long userId,
            @Path("favoriteRouteId") long favoriteRouteId
    );
}
