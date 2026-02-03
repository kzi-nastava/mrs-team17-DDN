package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.request.ChatSendMessageRequestDto;
import org.example.backend.dto.response.ChatMessageResponseDto;
import org.example.backend.dto.response.ChatThreadResponseDto;
import org.example.backend.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/chats")
public class AdminChatController {

    private final ChatService chatService;

    public AdminChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public List<ChatThreadResponseDto> listThreads(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return chatService.listThreads(query, clampLimit(limit));
    }

    @GetMapping("/{threadId}/messages")
    public List<ChatMessageResponseDto> getThreadMessages(
            @PathVariable Long threadId,
            @RequestParam(required = false) Long afterId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return chatService.getMessagesForThread(threadId, afterId, clampLimit(limit));
    }

    @PostMapping("/{threadId}/messages")
    public ChatMessageResponseDto sendAdminMessage(
            @PathVariable Long threadId,
            @Valid @RequestBody ChatSendMessageRequestDto req
    ) {
        return chatService.sendAdminMessage(currentUserId(), threadId, req.getContent());
    }

    private int clampLimit(int limit) {
        if (limit < 1) return 1;
        if (limit > 200) return 200;
        return limit;
    }

    private long currentUserId() {
        var a = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        Object p = (a != null) ? a.getPrincipal() : null;
        if (p instanceof Long l) return l;
        if (p instanceof Integer i) return i.longValue();
        if (p instanceof String s) return Long.parseLong(s);
        throw new IllegalStateException("No authenticated principal userId");
    }
}
