package org.example.backend.repository;

import org.example.backend.dto.response.DriverRideDetailsResponseDto;
import org.example.backend.dto.response.DriverRideHistoryResponseDto;
import org.example.backend.dto.response.PassengerInfoResponseDto;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Repository
public class InMemoryDriverRideRepository implements DriverRideRepository {

    private final Map<Long, DriverRideDetailsResponseDto> detailsById = new HashMap<>();
    private final List<DriverRideHistoryResponseDto> history = new ArrayList<>();

    public InMemoryDriverRideRepository() {
        seed();
    }

    private void seed() {
        DriverRideDetailsResponseDto d1 = new DriverRideDetailsResponseDto();
        d1.setRideId(1L);
        d1.setStartedAt(OffsetDateTime.of(2025, 12, 13, 14, 30, 0, 0, ZoneOffset.ofHours(1)));
        d1.setEndedAt(null);
        d1.setStartAddress("FTN");
        d1.setDestinationAddress("Železnička");
        d1.setStops(List.of());
        d1.setCanceled(false);
        d1.setCanceledBy(null);
        d1.setPrice(820);
        d1.setPanicTriggered(false);

        PassengerInfoResponseDto p11 = new PassengerInfoResponseDto();
        p11.setName("Petar Petrović");
        p11.setEmail("petar@email.com");

        PassengerInfoResponseDto p12 = new PassengerInfoResponseDto();
        p12.setName("Marko Marković");
        p12.setEmail("marko@email.com");

        d1.setPassengers(List.of(p11, p12));
        detailsById.put(1L, d1);

        DriverRideHistoryResponseDto h1 = new DriverRideHistoryResponseDto();
        h1.setRideId(1L);
        h1.setStartedAt(d1.getStartedAt());
        h1.setStartAddress(d1.getStartAddress());
        h1.setEndAddress(d1.getDestinationAddress());
        h1.setPrice(d1.getPrice());
        h1.setCanceled(d1.isCanceled());
        h1.setStatus("ACTIVE");
        history.add(h1);

        DriverRideDetailsResponseDto d2 = new DriverRideDetailsResponseDto();
        d2.setRideId(2L);
        d2.setStartedAt(OffsetDateTime.of(2025, 12, 12, 9, 10, 0, 0, ZoneOffset.ofHours(1)));
        d2.setEndedAt(OffsetDateTime.of(2025, 12, 12, 9, 35, 0, 0, ZoneOffset.ofHours(1)));
        d2.setStartAddress("Bulevar oslobođenja");
        d2.setDestinationAddress("Limanski park");
        d2.setStops(List.of("Spens"));
        d2.setCanceled(true);
        d2.setCanceledBy("PASSENGER");
        d2.setPrice(540);
        d2.setPanicTriggered(false);

        PassengerInfoResponseDto p21 = new PassengerInfoResponseDto();
        p21.setName("Jovan Jovanović");
        p21.setEmail("jovan@email.com");

        d2.setPassengers(List.of(p21));
        detailsById.put(2L, d2);

        DriverRideHistoryResponseDto h2 = new DriverRideHistoryResponseDto();
        h2.setRideId(2L);
        h2.setStartedAt(d2.getStartedAt());
        h2.setStartAddress(d2.getStartAddress());
        h2.setEndAddress(d2.getDestinationAddress());
        h2.setPrice(d2.getPrice());
        h2.setCanceled(d2.isCanceled());
        h2.setStatus("CANCELED");
        history.add(h2);
    }

    @Override
    public List<DriverRideHistoryResponseDto> findDriverRides(Long driverId, LocalDate from, LocalDate to) {
        List<DriverRideHistoryResponseDto> result = new ArrayList<>(history);

        if (from != null) {
            result.removeIf(r -> r.getStartedAt() == null || r.getStartedAt().toLocalDate().isBefore(from));
        }
        if (to != null) {
            result.removeIf(r -> r.getStartedAt() == null || r.getStartedAt().toLocalDate().isAfter(to));
        }

        result.sort((a, b) -> {
            if (a.getStartedAt() == null && b.getStartedAt() == null) return 0;
            if (a.getStartedAt() == null) return 1;
            if (b.getStartedAt() == null) return -1;
            return b.getStartedAt().compareTo(a.getStartedAt());
        });

        return result;
    }

    @Override
    public Optional<DriverRideDetailsResponseDto> findDriverRideDetails(Long driverId, Long rideId) {
        return Optional.ofNullable(detailsById.get(rideId));
    }

    @Override
    public Optional<DriverRideDetailsResponseDto> findActiveRideDetails(Long driverId) {
        return detailsById.values().stream()
                .filter(d -> !d.isCanceled())
                .filter(d -> d.getEndedAt() == null)
                .findFirst();
    }
}
