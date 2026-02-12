package com.example.taximobile.feature.user.notifications;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.taximobile.R;
import com.example.taximobile.core.auth.JwtUtils;
import com.example.taximobile.core.network.TokenStorage;
import com.example.taximobile.feature.user.data.NotificationsRepository;
import com.example.taximobile.feature.user.data.dto.response.NotificationResponseDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class UserForegroundNotificationPoller {

    private static final long POLL_INTERVAL_MS = 15_000L;
    private static final int POLL_LIMIT = 100;

    private static final String CHANNEL_ID = "ride_updates";
    private static final String PREFS_NAME = "user_notifications";
    private static final String LAST_SEEN_PREFIX = "notif_last_seen_id_user_";

    private final Activity activity;
    private final NotificationsRepository repo;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private boolean running = false;

    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            pollOnce();
        }
    };

    public UserForegroundNotificationPoller(Activity activity) {
        this.activity = activity;
        this.repo = new NotificationsRepository(activity);
    }

    public void start() {
        if (running) return;
        if (!canPostSystemNotifications()) return;

        running = true;
        createNotificationChannelIfNeeded();

        handler.removeCallbacks(pollRunnable);
        handler.post(pollRunnable);
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(pollRunnable);
    }

    private void pollOnce() {
        if (!running) return;

        if (!canPostSystemNotifications()) {
            stop();
            return;
        }

        repo.list(POLL_LIMIT, new NotificationsRepository.ListCb() {
            @Override
            public void onSuccess(List<NotificationResponseDto> items) {
                handlePollResult(items);
                scheduleNext();
            }

            @Override
            public void onError(String msg) {
                scheduleNext();
            }
        });
    }

    private void scheduleNext() {
        if (!running) return;
        handler.removeCallbacks(pollRunnable);
        handler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
    }

    private void handlePollResult(List<NotificationResponseDto> items) {
        Long userId = currentUserId();
        if (userId == null || userId <= 0) return;

        SharedPreferences sp = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String key = LAST_SEEN_PREFIX + userId;
        long lastSeen = sp.getLong(key, Long.MIN_VALUE);

        // First poll after session start: create a baseline.
        if (lastSeen == Long.MIN_VALUE) {
            if (items == null || items.isEmpty()) {
                sp.edit().putLong(key, 0L).apply();
                return;
            }

            long baselineMaxId = -1L;
            for (NotificationResponseDto n : items) {
                if (n != null && n.getId() > baselineMaxId) {
                    baselineMaxId = n.getId();
                }
            }
            sp.edit().putLong(key, Math.max(0L, baselineMaxId)).apply();
            return;
        }

        if (items == null || items.isEmpty()) return;

        long maxId = -1L;
        for (NotificationResponseDto n : items) {
            if (n != null && n.getId() > maxId) {
                maxId = n.getId();
            }
        }
        if (maxId <= 0) return;

        List<NotificationResponseDto> fresh = new ArrayList<>();
        for (NotificationResponseDto n : items) {
            if (n != null && n.getId() > lastSeen) {
                fresh.add(n);
            }
        }

        if (fresh.isEmpty()) {
            if (maxId > lastSeen) {
                sp.edit().putLong(key, maxId).apply();
            }
            return;
        }

        fresh.sort(Comparator.comparingLong(NotificationResponseDto::getId));
        for (NotificationResponseDto n : fresh) {
            showSystemNotification(n);
        }

        sp.edit().putLong(key, Math.max(lastSeen, maxId)).apply();
    }

    private void showSystemNotification(NotificationResponseDto n) {
        if (n == null || !canPostSystemNotifications()) return;

        String title = nonEmpty(n.getTitle(), activity.getString(R.string.notif_default_title));
        String message = nonEmpty(n.getMessage(), activity.getString(R.string.notif_default_message));

        android.content.Intent openIntent = NotificationLinkRouter.intentForNotification(activity, n);
        openIntent.addFlags(
                android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                        | android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        int requestCode = safeNotificationId(n.getId());
        PendingIntent pi = PendingIntent.getActivity(
                activity,
                requestCode,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi);

        NotificationManagerCompat.from(activity).notify(requestCode, builder.build());
    }

    private void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                activity.getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(activity.getString(R.string.notif_channel_description));

        NotificationManager manager = activity.getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private boolean canPostSystemNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return NotificationManagerCompat.from(activity).areNotificationsEnabled();
    }

    private Long currentUserId() {
        String token = new TokenStorage(activity.getApplicationContext()).getToken();
        if (token == null || token.trim().isEmpty()) return null;
        return JwtUtils.getUserIdFromSub(token);
    }

    private static int safeNotificationId(long notificationId) {
        long base = Math.abs(notificationId % 1_000_000L);
        return (int) (30_000L + base);
    }

    private static String nonEmpty(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) return fallback;
        return value.trim();
    }
}
