package com.example.taximobile.feature.user.data.dto.response;

public class NotificationResponseDto {
    private long id;
    private String type;
    private String title;
    private String message;
    private String linkUrl;
    private String createdAt; // ISO string
    private String readAt;    // ISO string ili null

    public NotificationResponseDto() {}

    public long getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getLinkUrl() { return linkUrl; }
    public String getCreatedAt() { return createdAt; }
    public String getReadAt() { return readAt; }

    public void setId(long id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setTitle(String title) { this.title = title; }
    public void setMessage(String message) { this.message = message; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setReadAt(String readAt) { this.readAt = readAt; }
}
