package org.example.backend.repository;

import org.example.backend.dto.response.RideStatsPointDto;

import java.time.LocalDate;
import java.util.List;

public interface RideStatsRepository {

    List<RideStatsPointDto> aggregateForDriver(long driverId, LocalDate from, LocalDate to);

    List<RideStatsPointDto> aggregateForPassengerEmail(String email, LocalDate from, LocalDate to);

    List<RideStatsPointDto> aggregateForAllCompleted(LocalDate from, LocalDate to);
}
