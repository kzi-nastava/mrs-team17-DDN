package com.example.taximobile.feature.support.data.dto.response;

public class ChatThreadResponseDto {
    private long id;
    private long userId;
    private String userName;
    private String userEmail;
    private String lastMessageAt; // OffsetDateTime kao string (ISO)

    public ChatThreadResponseDto() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(String lastMessageAt) { this.lastMessageAt = lastMessageAt; }
}
