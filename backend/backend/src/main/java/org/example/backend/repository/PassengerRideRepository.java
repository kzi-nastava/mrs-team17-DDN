package org.example.backend.repository;

import org.example.backend.dto.response.PassengerRideHistoryResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface PassengerRideRepository {
    List<PassengerRideHistoryResponseDto> findPassengerRides(String passengerEmail, LocalDate from, LocalDate to);
}
