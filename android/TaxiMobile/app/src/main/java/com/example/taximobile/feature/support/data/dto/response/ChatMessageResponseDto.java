package com.example.taximobile.feature.support.data.dto.response;

public class ChatMessageResponseDto {
    private long id;
    private String senderRole; // PASSENGER/DRIVER/ADMIN
    private String content;
    private String sentAt; // OffsetDateTime kao string (ISO)

    public ChatMessageResponseDto() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }
}
