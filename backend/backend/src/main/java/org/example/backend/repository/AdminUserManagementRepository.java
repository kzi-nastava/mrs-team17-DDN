package org.example.backend.repository;

import org.example.backend.dto.response.AdminUserStatusResponseDto;

import java.util.List;
import java.util.Optional;

public interface AdminUserManagementRepository {

    List<AdminUserStatusResponseDto> listUsersWithStatus(String role, String query, int limit);

    Optional<AdminUserStatusResponseDto> findUserStatusById(long userId);

    int updateBlockStatus(long userId, boolean blocked, String blockReason);
}
