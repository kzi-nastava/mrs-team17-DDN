package com.example.taximobile.feature.support.data;

import android.content.Context;

import com.example.taximobile.core.network.ApiClient;
import com.example.taximobile.feature.support.data.dto.request.ChatSendMessageRequestDto;
import com.example.taximobile.feature.support.data.dto.response.ChatMessageResponseDto;
import com.example.taximobile.feature.support.data.dto.response.ChatThreadResponseDto;

import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRepository {

    private final ChatApi api;

    public ChatRepository(Context ctx) {
        this.api = ApiClient.get(ctx).create(ChatApi.class);
    }

    public interface ThreadCb {
        void onSuccess(ChatThreadResponseDto thread);
        void onError(String msg);
    }

    public interface MessagesCb {
        void onSuccess(List<ChatMessageResponseDto> items);
        void onError(String msg);
    }

    public interface SendCb {
        void onSuccess(ChatMessageResponseDto msg);
        void onError(String msg);
    }

    public void getMyThread(ThreadCb cb) {
        api.getMyThread().enqueue(new Callback<ChatThreadResponseDto>() {
            @Override public void onResponse(Call<ChatThreadResponseDto> call, Response<ChatThreadResponseDto> res) {
                if (!res.isSuccessful()) { cb.onError("HTTP " + res.code()); return; }
                ChatThreadResponseDto body = res.body();
                if (body == null) { cb.onError("Empty response"); return; }
                cb.onSuccess(body);
            }
            @Override public void onFailure(Call<ChatThreadResponseDto> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void getMyMessages(Long afterId, int limit, MessagesCb cb) {
        api.getMyMessages(afterId, limit).enqueue(new Callback<List<ChatMessageResponseDto>>() {
            @Override public void onResponse(Call<List<ChatMessageResponseDto>> call, Response<List<ChatMessageResponseDto>> res) {
                if (!res.isSuccessful()) { cb.onError("HTTP " + res.code()); return; }
                List<ChatMessageResponseDto> body = res.body();
                cb.onSuccess(body != null ? body : Collections.emptyList());
            }
            @Override public void onFailure(Call<List<ChatMessageResponseDto>> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }

    public void sendMyMessage(String content, SendCb cb) {
        ChatSendMessageRequestDto req = new ChatSendMessageRequestDto(content);
        api.sendMyMessage(req).enqueue(new Callback<ChatMessageResponseDto>() {
            @Override public void onResponse(Call<ChatMessageResponseDto> call, Response<ChatMessageResponseDto> res) {
                if (!res.isSuccessful()) { cb.onError("HTTP " + res.code()); return; }
                ChatMessageResponseDto body = res.body();
                if (body == null) { cb.onError("Empty response"); return; }
                cb.onSuccess(body);
            }
            @Override public void onFailure(Call<ChatMessageResponseDto> call, Throwable t) {
                cb.onError(t.getMessage() != null ? t.getMessage() : "Network error");
            }
        });
    }
}
