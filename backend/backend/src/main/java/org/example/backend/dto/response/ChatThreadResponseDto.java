package org.example.backend.dto.response;

import java.time.OffsetDateTime;

public class ChatThreadResponseDto {
    private long id;
    private long userId;
    private String userName;
    private String userEmail;
    private OffsetDateTime lastMessageAt;

    public ChatThreadResponseDto() {}

    public ChatThreadResponseDto(long id, long userId, OffsetDateTime lastMessageAt) {
        this.id = id;
        this.userId = userId;
        this.userName = null;
        this.userEmail = null;
        this.lastMessageAt = lastMessageAt;
    }

    public ChatThreadResponseDto(long id, long userId, OffsetDateTime lastMessageAt, String userName, String userEmail) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.lastMessageAt = lastMessageAt;
    }


    public ChatThreadResponseDto(long id, long userId, String userName, String userEmail, OffsetDateTime lastMessageAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.lastMessageAt = lastMessageAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public OffsetDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(OffsetDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
}
