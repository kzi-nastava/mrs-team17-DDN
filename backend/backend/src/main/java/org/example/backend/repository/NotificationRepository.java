package org.example.backend.repository;

import org.example.backend.dto.response.NotificationResponseDto;
import java.util.List;

public interface NotificationRepository {

    record CreatedNotification(
            long notificationId,
            long userId,
            String type,
            String title,
            String message,
            String linkUrl
    ) {}

    List<NotificationResponseDto> listForUser(long userId, int limit);
    long countUnread(long userId);
    boolean markRead(long userId, long notificationId);

    List<CreatedNotification> createForUsers(
            List<Long> userIds,
            String type,
            String title,
            String message,
            String linkUrl
    );
}
