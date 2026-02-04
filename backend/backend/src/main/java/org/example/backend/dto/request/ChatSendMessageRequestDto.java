package org.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class ChatSendMessageRequestDto {

    @NotBlank
    private String content;

    public ChatSendMessageRequestDto() {}

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
