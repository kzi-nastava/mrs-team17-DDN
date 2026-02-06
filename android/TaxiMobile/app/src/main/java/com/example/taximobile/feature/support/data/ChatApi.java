package com.example.taximobile.feature.support.data;

import com.example.taximobile.feature.support.data.dto.request.ChatSendMessageRequestDto;
import com.example.taximobile.feature.support.data.dto.response.ChatMessageResponseDto;
import com.example.taximobile.feature.support.data.dto.response.ChatThreadResponseDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ChatApi {

    @GET("api/chat/thread/me")
    Call<ChatThreadResponseDto> getMyThread();

    @GET("api/chat/messages/me")
    Call<List<ChatMessageResponseDto>> getMyMessages(
            @Query("afterId") Long afterId,
            @Query("limit") Integer limit
    );

    @POST("api/chat/messages/me")
    Call<ChatMessageResponseDto> sendMyMessage(@Body ChatSendMessageRequestDto req);
}
