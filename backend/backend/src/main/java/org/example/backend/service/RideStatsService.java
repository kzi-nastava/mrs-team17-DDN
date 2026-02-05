package org.example.backend.service;

import org.example.backend.dto.response.RideStatsAveragesDto;
import org.example.backend.dto.response.RideStatsPointDto;
import org.example.backend.dto.response.RideStatsReportResponseDto;
import org.example.backend.dto.response.RideStatsTotalsDto;
import org.example.backend.repository.DriverRepository;
import org.example.backend.repository.RideStatsRepository;
import org.example.backend.repository.UserLookupRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RideStatsService {

    private final RideStatsRepository stats;
    private final DriverRepository drivers;
    private final UserLookupRepository users;

    public RideStatsService(RideStatsRepository stats, DriverRepository drivers, UserLookupRepository users) {
        this.stats = stats;
        this.drivers = drivers;
        this.users = users;
    }

    public RideStatsReportResponseDto buildMyReport(long userId, boolean isDriver, LocalDate from, LocalDate to) {
        validateRange(from, to);

        if (isDriver) {
            long driverId = drivers.findDriverIdByUserId(userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Driver profile not found"));

            List<RideStatsPointDto> raw = stats.aggregateForDriver(driverId, from, to);
            return buildReport(raw, from, to, "DRIVER", "USER", userId);
        }

        UserLookupRepository.UserBasic u = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        List<RideStatsPointDto> raw = stats.aggregateForPassengerEmail(u.email(), from, to);
        return buildReport(raw, from, to, "PASSENGER", "USER", userId);
    }

    public RideStatsReportResponseDto buildAdminReport(String targetRole, Long targetUserId, LocalDate from, LocalDate to) {
        validateRange(from, to);

        String role = (targetRole == null) ? "" : targetRole.trim().toUpperCase();
        if (!role.equals("DRIVER") && !role.equals("PASSENGER")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "role must be DRIVER or PASSENGER");
        }

        String scope = (targetUserId == null) ? "ALL" : "USER";

        List<RideStatsPointDto> raw;

        if (targetUserId == null) {
            raw = stats.aggregateForAllCompleted(from, to);
            return buildReport(raw, from, to, role, scope, null);
        }

        if (role.equals("DRIVER")) {
            long driverId = drivers.findDriverIdByUserId(targetUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected user is not a driver"));
            raw = stats.aggregateForDriver(driverId, from, to);
        } else {
            UserLookupRepository.UserBasic u = users.findById(targetUserId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected user not found"));
            raw = stats.aggregateForPassengerEmail(u.email(), from, to);
        }

        return buildReport(raw, from, to, role, scope, targetUserId);
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from and to are required (YYYY-MM-DD)");
        }
        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be <= to");
        }
    }

    private RideStatsReportResponseDto buildReport(
            List<RideStatsPointDto> raw,
            LocalDate from,
            LocalDate to,
            String targetRole,
            String scope,
            Long targetUserId
    ) {
        Map<LocalDate, RideStatsPointDto> byDate = new HashMap<>();
        for (RideStatsPointDto p : raw) {
            if (p.getDate() != null) {
                byDate.put(p.getDate(), p);
            }
        }

        long cumRides = 0L;
        double cumKm = 0.0;
        double cumMoney = 0.0;

        java.util.ArrayList<RideStatsPointDto> points = new java.util.ArrayList<>();

        int days = 0;
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            days++;

            RideStatsPointDto base = byDate.get(d);
            long rides = (base == null) ? 0L : base.getRides();
            double km = (base == null) ? 0.0 : base.getKilometers();
            double money = (base == null) ? 0.0 : base.getMoney();

            cumRides += rides;
            cumKm += km;
            cumMoney += money;

            RideStatsPointDto p = new RideStatsPointDto();
            p.setDate(d);
            p.setRides(rides);
            p.setKilometers(km);
            p.setMoney(money);
            p.setCumulativeRides(cumRides);
            p.setCumulativeKilometers(cumKm);
            p.setCumulativeMoney(cumMoney);
            points.add(p);
        }

        RideStatsTotalsDto totals = new RideStatsTotalsDto();
        totals.setRides(cumRides);
        totals.setKilometers(cumKm);
        totals.setMoney(cumMoney);

        RideStatsAveragesDto avg = new RideStatsAveragesDto();
        avg.setRidesPerDay(days == 0 ? 0.0 : (double) cumRides / days);
        avg.setKilometersPerDay(days == 0 ? 0.0 : cumKm / days);
        avg.setMoneyPerDay(days == 0 ? 0.0 : cumMoney / days);

        if (cumRides > 0) {
            avg.setKilometersPerRide(cumKm / cumRides);
            avg.setMoneyPerRide(cumMoney / cumRides);
        } else {
            avg.setKilometersPerRide(0.0);
            avg.setMoneyPerRide(0.0);
        }

        RideStatsReportResponseDto res = new RideStatsReportResponseDto();
        res.setTargetRole(targetRole);
        res.setScope(scope);
        res.setTargetUserId(targetUserId);
        res.setFrom(from);
        res.setTo(to);
        res.setDays(days);
        res.setPoints(points);
        res.setTotals(totals);
        res.setAverages(avg);
        return res;
    }
}
