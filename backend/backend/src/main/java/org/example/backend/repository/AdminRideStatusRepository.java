package org.example.backend.repository;

import org.example.backend.dto.response.AdminRideStatusRowDto;
import java.util.List;

public interface AdminRideStatusRepository {
    List<AdminRideStatusRowDto> list(String q, int limit);
}
