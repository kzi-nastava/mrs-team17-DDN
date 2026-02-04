package org.example.backend.dto.response;

import java.time.OffsetDateTime;

public class NotificationResponseDto {
    private long id;
    private String type;
    private String title;
    private String message;
    private String linkUrl;
    private OffsetDateTime createdAt;
    private OffsetDateTime readAt;

    public NotificationResponseDto() {}

    public NotificationResponseDto(long id, String type, String title, String message,
                                   String linkUrl, OffsetDateTime createdAt, OffsetDateTime readAt) {
        this.id = id; this.type = type; this.title = title; this.message = message;
        this.linkUrl = linkUrl; this.createdAt = createdAt; this.readAt = readAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getLinkUrl() { return linkUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getReadAt() { return readAt; }
    public void setReadAt(OffsetDateTime readAt) { this.readAt = readAt; }
}
