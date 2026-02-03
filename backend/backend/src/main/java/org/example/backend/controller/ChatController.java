package org.example.backend.controller;

import jakarta.validation.Valid;
import org.example.backend.dto.request.ChatSendMessageRequestDto;
import org.example.backend.dto.response.ChatMessageResponseDto;
import org.example.backend.dto.response.ChatThreadResponseDto;
import org.example.backend.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/thread/me")
    public ChatThreadResponseDto getMyThread() {
        return chatService.getOrCreateThreadForUser(currentUserId());
    }

    @GetMapping("/messages/me")
    public List<ChatMessageResponseDto> getMyMessages(
            @RequestParam(required = false) Long afterId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return chatService.getMessagesForUser(currentUserId(), afterId, clampLimit(limit));
    }

    @PostMapping("/messages/me")
    public ChatMessageResponseDto sendMyMessage(
            @Valid @RequestBody ChatSendMessageRequestDto req
    ) {
        return chatService.sendUserMessage(currentUserId(), req.getContent());
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
