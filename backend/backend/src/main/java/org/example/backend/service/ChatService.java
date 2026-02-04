package org.example.backend.service;

import org.example.backend.dto.response.ChatMessageResponseDto;
import org.example.backend.dto.response.ChatThreadResponseDto;
import org.example.backend.repository.ChatRepository;
import org.example.backend.repository.UserLookupRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserLookupRepository userLookupRepository;

    public ChatService(ChatRepository chatRepository, UserLookupRepository userLookupRepository) {
        this.chatRepository = chatRepository;
        this.userLookupRepository = userLookupRepository;
    }

    public ChatThreadResponseDto getOrCreateThreadForUser(long userId) {
        // validacija: user postoji
        userLookupRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return chatRepository.findThreadByUserId(userId)
                .orElseGet(() -> {
                    try {
                        return chatRepository.createThreadForUser(userId);
                    } catch (DataIntegrityViolationException e) {
                        // ako se paralelno kreira thread (unique user_id)
                        return chatRepository.findThreadByUserId(userId)
                                .orElseThrow(() -> new ResponseStatusException(
                                        HttpStatus.INTERNAL_SERVER_ERROR, "Chat thread create failed"
                                ));
                    }
                });
    }

    public List<ChatMessageResponseDto> getMessagesForUser(long userId, Long afterId, int limit) {
        ChatThreadResponseDto thread = getOrCreateThreadForUser(userId);
        return chatRepository.findMessages(thread.getId(), afterId, limit);
    }

    public ChatMessageResponseDto sendUserMessage(long userId, String content) {
        String text = normalizeContent(content);

        String role = userLookupRepository.findRoleById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // ovaj endpoint je za putnika/vozača
        if (!"PASSENGER".equals(role) && !"DRIVER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only passengers/drivers can send messages here");
        }

        ChatThreadResponseDto thread = getOrCreateThreadForUser(userId);

        ChatMessageResponseDto msg = chatRepository.insertMessage(thread.getId(), userId, role, text);
        chatRepository.touchThreadLastMessageAt(thread.getId());
        return msg;
    }

    public List<ChatThreadResponseDto> listThreads(String query, int limit) {
        return chatRepository.listThreads(query, limit);
    }

    public List<ChatMessageResponseDto> getMessagesForThread(long threadId, Long afterId, int limit) {
        chatRepository.findThreadById(threadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found"));

        return chatRepository.findMessages(threadId, afterId, limit);
    }

    public ChatMessageResponseDto sendAdminMessage(long adminUserId, long threadId, String content) {
        String text = normalizeContent(content);

        String role = userLookupRepository.findRoleById(adminUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!"ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }

        chatRepository.findThreadById(threadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found"));

        ChatMessageResponseDto msg = chatRepository.insertMessage(threadId, adminUserId, "ADMIN", text);
        chatRepository.touchThreadLastMessageAt(threadId);
        return msg;
    }

    private String normalizeContent(String content) {
        String text = (content == null) ? "" : content.trim();
        if (text.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content is empty");
        }
        if (text.length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Message content too long");
        }
        return text;
    }
}
