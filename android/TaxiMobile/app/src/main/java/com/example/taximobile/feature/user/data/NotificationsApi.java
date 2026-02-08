package com.example.taximobile.feature.user.data;

import com.example.taximobile.feature.user.data.dto.response.NotificationResponseDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface NotificationsApi {

    @GET("api/notifications/me")
    Call<List<NotificationResponseDto>> myNotifications(@Query("limit") int limit);

    @GET("api/notifications/me/unread-count")
    Call<Long> myUnreadCount();

    @POST("api/notifications/me/{id}/read")
    Call<Void> markRead(@Path("id") long id);
}
