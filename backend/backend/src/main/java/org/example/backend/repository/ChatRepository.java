package org.example.backend.repository;

import org.example.backend.dto.response.ChatMessageResponseDto;
import org.example.backend.dto.response.ChatThreadResponseDto;

import java.util.List;
import java.util.Optional;

public interface ChatRepository {

    Optional<ChatThreadResponseDto> findThreadByUserId(long userId);

    ChatThreadResponseDto createThreadForUser(long userId);

    Optional<ChatThreadResponseDto> findThreadById(long threadId);

    List<ChatThreadResponseDto> listThreads(String query, int limit);

    List<ChatMessageResponseDto> findMessages(long threadId, Long afterId, int limit);

    ChatMessageResponseDto insertMessage(long threadId, long senderUserId, String senderRole, String content);

    boolean touchThreadLastMessageAt(long threadId);
}
