package com.example.taximobile.feature.admin.data;

import com.example.taximobile.feature.support.data.dto.request.ChatSendMessageRequestDto;
import com.example.taximobile.feature.support.data.dto.response.ChatMessageResponseDto;
import com.example.taximobile.feature.support.data.dto.response.ChatThreadResponseDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface AdminChatApi {

    @GET("/api/admin/chats")
    Call<List<ChatThreadResponseDto>> listThreads(
            @Query("query") String query,
            @Query("limit") int limit
    );

    @GET("/api/admin/chats/{threadId}/messages")
    Call<List<ChatMessageResponseDto>> getThreadMessages(
            @Path("threadId") long threadId,
            @Query("afterId") Long afterId,
            @Query("limit") int limit
    );

    @POST("/api/admin/chats/{threadId}/messages")
    Call<ChatMessageResponseDto> sendAdminMessage(
            @Path("threadId") long threadId,
            @Body ChatSendMessageRequestDto body
    );
}
