package com.example.taximobile.feature.admin.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.support.data.dto.request.ChatSendMessageRequestDto;
import com.example.taximobile.feature.support.data.dto.response.ChatMessageResponseDto;
import com.example.taximobile.feature.support.data.dto.response.ChatThreadResponseDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminChatRepository {

    private final AdminChatApi api;

    public AdminChatRepository(Context ctx) {
        api = ApiClient.get(ctx)
                .create(AdminChatApi.class);
    }

    /* callbacks */
    public interface ThreadsCb {
        void onSuccess(List<ChatThreadResponseDto> list);
        void onError(String msg);
    }

    public interface MessagesCb {
        void onSuccess(List<ChatMessageResponseDto> list);
        void onError(String msg);
    }

    public interface SendCb {
        void onSuccess(ChatMessageResponseDto msg);
        void onError(String msg);
    }

    /* API calls */

    public void listThreads(String query, int limit, ThreadsCb cb) {
        api.listThreads(query, limit).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<ChatThreadResponseDto>> call,
                                   Response<List<ChatThreadResponseDto>> res) {
                if (res.isSuccessful() && res.body() != null)
                    cb.onSuccess(res.body());
                else
                    cb.onError("Failed to load threads");
            }

            @Override
            public void onFailure(Call<List<ChatThreadResponseDto>> call, Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void getThreadMessages(long threadId, Long afterId, int limit, MessagesCb cb) {
        api.getThreadMessages(threadId, afterId, limit).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<ChatMessageResponseDto>> call,
                                   Response<List<ChatMessageResponseDto>> res) {
                if (res.isSuccessful() && res.body() != null)
                    cb.onSuccess(res.body());
                else
                    cb.onError("Failed to load messages");
            }

            @Override
            public void onFailure(Call<List<ChatMessageResponseDto>> call, Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    public void sendMessage(long threadId, String content, SendCb cb) {
        ChatSendMessageRequestDto dto = new ChatSendMessageRequestDto();
        dto.setContent(content);

        api.sendAdminMessage(threadId, dto).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ChatMessageResponseDto> call,
                                   Response<ChatMessageResponseDto> res) {
                if (res.isSuccessful() && res.body() != null)
                    cb.onSuccess(res.body());
                else
                    cb.onError("Send failed");
            }

            @Override
            public void onFailure(Call<ChatMessageResponseDto> call, Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }
}
