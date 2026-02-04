package org.example.backend.controller;

import org.example.backend.dto.response.NotificationResponseDto;
import org.example.backend.repository.NotificationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository repo;

    public NotificationController(NotificationRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/me")
    public List<NotificationResponseDto> myNotifications(
            @RequestParam(defaultValue = "50") int limit
    ) {
        return repo.listForUser(currentUserId(), limit);
    }

    @GetMapping("/me/unread-count")
    public long myUnreadCount() {
        return repo.countUnread(currentUserId());
    }

    @PostMapping("/me/{id}/read")
    public void markRead(@PathVariable long id) {
        repo.markRead(currentUserId(), id);
    }

    private long currentUserId() {
        var a = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        Object p = (a != null) ? a.getPrincipal() : null;
        if (p instanceof Long l) return l;
        if (p instanceof Integer i) return i.longValue();
        if (p instanceof String s) return Long.parseLong(s);
        throw new IllegalStateException("No authenticated principal userId");
    }
}
