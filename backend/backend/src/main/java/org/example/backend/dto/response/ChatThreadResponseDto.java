package org.example.backend.dto.response;

import java.time.OffsetDateTime;

public class ChatThreadResponseDto {
    private long id;
    private long userId;
    private OffsetDateTime lastMessageAt;

    public ChatThreadResponseDto() {}

    public ChatThreadResponseDto(long id, long userId, OffsetDateTime lastMessageAt) {
        this.id = id;
        this.userId = userId;
        this.lastMessageAt = lastMessageAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public OffsetDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(OffsetDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
}
