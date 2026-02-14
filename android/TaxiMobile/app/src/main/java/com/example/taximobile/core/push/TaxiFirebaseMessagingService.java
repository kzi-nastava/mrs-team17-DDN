package com.example.taximobile.core.push;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.taximobile.R;
import com.example.taximobile.core.network.TokenStorage;
import com.example.taximobile.feature.user.data.dto.response.NotificationResponseDto;
import com.example.taximobile.feature.user.notifications.NotificationLinkRouter;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class TaxiFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "TaxiFCM";
    private static final String CHANNEL_ID = "ride_updates";

    private static final String PREFS_NAME = "fcm";
    private static final String KEY_LATEST_TOKEN = "latest_token";
    public static final String EXTRA_NOTIFICATION_ID = "extra_notification_id";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(KEY_LATEST_TOKEN, token)
                .apply();

        syncTokenToBackendIfPossible(token);
        Log.d(TAG, "New FCM token: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (!canPostNotifications()) {
            Log.w(TAG, "Notification permission is missing. Skipping FCM notification display.");
            return;
        }

        createNotificationChannelIfNeeded();

        Map<String, String> data = remoteMessage.getData();
        NotificationResponseDto dto = toNotificationDto(data);

        String fallbackTitle = remoteMessage.getNotification() != null
                ? remoteMessage.getNotification().getTitle()
                : null;
        String fallbackBody = remoteMessage.getNotification() != null
                ? remoteMessage.getNotification().getBody()
                : null;

        String title = firstNonBlank(dto.getTitle(), fallbackTitle, getString(R.string.notif_default_title));
        String message = firstNonBlank(dto.getMessage(), fallbackBody, getString(R.string.notif_default_message));

        Intent openIntent = NotificationLinkRouter.intentForNotification(this, dto);
        openIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        long notificationId = parsePositiveLong(data != null ? data.get("id") : null);
        if (notificationId > 0) {
            openIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        }

        int requestCode = safeNotificationId(data != null ? data.get("id") : null);
        PendingIntent pi = PendingIntent.getActivity(
                this,
                requestCode,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi);

        NotificationManagerCompat.from(this).notify(requestCode, builder.build());
    }

    private NotificationResponseDto toNotificationDto(Map<String, String> data) {
        NotificationResponseDto dto = new NotificationResponseDto();
        if (data == null || data.isEmpty()) return dto;

        dto.setType(firstNonBlank(data.get("type"), data.get("notificationType")));
        dto.setTitle(firstNonBlank(data.get("title")));
        dto.setMessage(firstNonBlank(data.get("message"), data.get("body")));
        dto.setLinkUrl(firstNonBlank(data.get("linkUrl"), data.get("link"), data.get("deepLink")));
        return dto;
    }

    private void createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
        );
        channel.setDescription(getString(R.string.notif_channel_description));

        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private boolean canPostNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return NotificationManagerCompat.from(this).areNotificationsEnabled();
    }

    private static int safeNotificationId(String idRaw) {
        if (idRaw != null && !idRaw.trim().isEmpty()) {
            try {
                long id = Long.parseLong(idRaw.trim());
                long normalized = Math.abs(id % 1_000_000L);
                return (int) (30_000L + normalized);
            } catch (NumberFormatException ignore) {
                // fall back below
            }
        }
        return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
    }

    private static long parsePositiveLong(String raw) {
        if (raw == null || raw.trim().isEmpty()) return -1L;
        try {
            long value = Long.parseLong(raw.trim());
            return value > 0 ? value : -1L;
        } catch (Exception ignore) {
            return -1L;
        }
    }

    private void syncTokenToBackendIfPossible(String token) {
        if (token == null || token.trim().isEmpty()) return;

        String jwt = new TokenStorage(getApplicationContext()).getToken();
        if (jwt == null || jwt.trim().isEmpty()) return;

        new PushTokenRepository(getApplicationContext()).registerToken(
                token.trim(),
                "ANDROID",
                new PushTokenRepository.VoidCb() {
                    @Override
                    public void onSuccess() {
                        // No-op.
                    }

                    @Override
                    public void onError(String msg) {
                        // Token sync is best-effort.
                    }
                }
        );
    }

    private static String firstNonBlank(String... values) {
        if (values == null) return null;
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }
}
