package com.example.taximobile.feature.support.data.dto.request;

public class ChatSendMessageRequestDto {
    private String content;

    public ChatSendMessageRequestDto() {}

    public ChatSendMessageRequestDto(String content) {
        this.content = content;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
