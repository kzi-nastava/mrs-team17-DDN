package org.example.backend.service;

import org.example.backend.repository.ScheduledRideReminderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ScheduledRideReminderService {

    private static final int AHEAD_MINUTES = 16;
    private static final int BEHIND_MINUTES = 1;

    private final Map<String, Long> sent = new ConcurrentHashMap<>();

    private final ScheduledRideReminderRepository repo;
    private final NotificationService notificationService;

    public ScheduledRideReminderService(
            ScheduledRideReminderRepository repo,
            NotificationService notificationService
    ) {
        this.repo = repo;
        this.notificationService = notificationService;
    }

    @Scheduled(fixedDelay = 30_000)
    public void sendScheduledRideReminders() {
        OffsetDateTime now = OffsetDateTime.now();
        long nowEpoch = now.toEpochSecond();

        List<ScheduledRideReminderRepository.ReminderRideRow> rides =
                repo.findRidesInWindow(AHEAD_MINUTES, BEHIND_MINUTES);

        for (var r : rides) {
            if (r.scheduledAt() == null) continue;

            long scheduledEpoch = r.scheduledAt().toEpochSecond();
            long deltaSeconds = scheduledEpoch - nowEpoch;

            Integer minuteKey = pickMinuteKey(deltaSeconds);
            if (minuteKey == null) continue;

            if (!shouldSend(r.rideId(), minuteKey, nowEpoch)) continue;

            notificationService.notifyScheduledRideReminder(r.rideId(), minuteKey);
        }

        cleanup(nowEpoch);
    }

    private Integer pickMinuteKey(long deltaSeconds) {
        if (deltaSeconds <= 15 * 60 && deltaSeconds > 14 * 60) return 15;
        if (deltaSeconds <= 10 * 60 && deltaSeconds > 9 * 60) return 10;
        if (deltaSeconds <= 5 * 60 && deltaSeconds > 4 * 60) return 5;
        if (deltaSeconds >= 0 && deltaSeconds <= 60) return 0;

        return null;
    }

    private boolean shouldSend(long rideId, int minuteKey, long nowEpoch) {
        String key = rideId + ":" + minuteKey;

        Long prev = sent.putIfAbsent(key, nowEpoch);
        if (prev == null) return true;

        if (nowEpoch - prev < 120) return false;

        sent.put(key, nowEpoch);
        return true;
    }

    private void cleanup(long nowEpoch) {
        long cutoff = nowEpoch - 3600;
        if (sent.size() < 2000) return;

        for (var it = sent.entrySet().iterator(); it.hasNext();) {
            var e = it.next();
            if (e.getValue() < cutoff) it.remove();
        }
    }
}
