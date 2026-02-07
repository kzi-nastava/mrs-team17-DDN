package com.example.taximobile.feature.user.data;

import com.example.taximobile.feature.user.data.dto.response.FavoriteRoutePointResponseDto;
import com.example.taximobile.feature.user.data.dto.response.FavoriteRouteResponseDto;
import com.example.taximobile.feature.user.data.dto.response.PassengerRideHistoryResponseDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class FavoriteRouteKeyUtil {

    private FavoriteRouteKeyUtil() {}

    public static String fromRideDto(PassengerRideHistoryResponseDto dto) {
        if (dto == null) return "";
        return fromRide(
                dto.getStartAddress(),
                dto.getDestinationAddress(),
                dto.getStops()
        );
    }

    public static String fromRide(String start, String destination, List<String> stops) {
        String a = normalize(start);
        String b = normalize(destination);
        String c = joinStops(stops);
        return a + "||" + b + "||" + c;
    }

    public static String fromFavoriteDto(FavoriteRouteResponseDto dto) {
        if (dto == null) return "";

        String start = dto.getStart() != null ? dto.getStart().getAddress() : null;
        String destination = dto.getDestination() != null ? dto.getDestination().getAddress() : null;

        List<String> stops = new ArrayList<>();
        if (dto.getStops() != null) {
            for (FavoriteRoutePointResponseDto s : dto.getStops()) {
                if (s != null) stops.add(s.getAddress());
            }
        }

        return fromRide(start, destination, stops);
    }

    private static String joinStops(List<String> stops) {
        if (stops == null || stops.isEmpty()) return "";
        List<String> normalized = new ArrayList<>();
        for (String s : stops) normalized.add(normalize(s));
        return String.join("|", normalized);
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}
