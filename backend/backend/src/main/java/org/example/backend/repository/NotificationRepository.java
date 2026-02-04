package org.example.backend.repository;

import org.example.backend.dto.response.NotificationResponseDto;
import java.util.List;

public interface NotificationRepository {
    List<NotificationResponseDto> listForUser(long userId, int limit);
    long countUnread(long userId);
    boolean markRead(long userId, long notificationId);

    void createForUsers(List<Long> userIds, String type, String title, String message, String linkUrl);
}
