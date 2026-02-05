package org.example.backend.repository;

import org.example.backend.dto.response.AdminUserOptionResponseDto;

import java.util.List;

public interface AdminUserSelectRepository {
    List<AdminUserOptionResponseDto> listUsersByRole(String role, String query, int limit);
}
