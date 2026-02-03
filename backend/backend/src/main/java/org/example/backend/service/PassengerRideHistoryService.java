package org.example.backend.service;

import org.example.backend.dto.response.PassengerRideHistoryResponseDto;
import org.example.backend.repository.PassengerRideRepository;
import org.example.backend.repository.UserLookupRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class PassengerRideHistoryService {

    private final PassengerRideRepository repo;
    private final UserLookupRepository users;

    public PassengerRideHistoryService(PassengerRideRepository repo, UserLookupRepository users) {
        this.repo = repo;
        this.users = users;
    }

    public List<PassengerRideHistoryResponseDto> getMyRideHistory(long userId, LocalDate from, LocalDate to) {
        UserLookupRepository.UserBasic u = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        String email = u.email();
        if (email == null || email.trim().isEmpty()) {
            return List.of();
        }

        return repo.findPassengerRides(email, from, to);
    }
}
