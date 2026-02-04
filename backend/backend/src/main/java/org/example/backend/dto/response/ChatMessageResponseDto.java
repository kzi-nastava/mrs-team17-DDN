package org.example.backend.dto.response;

import java.time.OffsetDateTime;

public class ChatMessageResponseDto {
    private long id;
    private String senderRole;   // PASSENGER/DRIVER/ADMIN
    private String content;
    private OffsetDateTime sentAt;

    public ChatMessageResponseDto() {}

    public ChatMessageResponseDto(long id, String senderRole, String content, OffsetDateTime sentAt) {
        this.id = id;
        this.senderRole = senderRole;
        this.content = content;
        this.sentAt = sentAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public OffsetDateTime getSentAt() { return sentAt; }
    public void setSentAt(OffsetDateTime sentAt) { this.sentAt = sentAt; }
}
