package org.example.backend.controller;

import org.example.backend.dto.response.AdminRideStatusRowDto;
import org.example.backend.repository.AdminRideStatusRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ride-status")
public class AdminRideStatusController {

    private final AdminRideStatusRepository repo;

    public AdminRideStatusController(AdminRideStatusRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<AdminRideStatusRowDto> list(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return repo.list(q, limit);
    }
}
