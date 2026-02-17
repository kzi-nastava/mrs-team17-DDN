package org.example.backend.service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import org.example.backend.repository.NotificationRepository;
import org.example.backend.repository.UserDeviceTokenRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class NotificationService {

    private static final int FCM_MAX_TOKENS_PER_CALL = 500;

    private final JdbcClient jdbc;
    private final NotificationRepository repo;
    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    public NotificationService(
            JdbcClient jdbc,
            NotificationRepository repo,
            UserDeviceTokenRepository userDeviceTokenRepository,
            ObjectProvider<FirebaseMessaging> firebaseMessagingProvider
    ) {
        this.jdbc = jdbc;
        this.repo = repo;
        this.userDeviceTokenRepository = userDeviceTokenRepository;
        this.firebaseMessagingProvider = firebaseMessagingProvider;
    }

    public void notifyRideAccepted(long rideId) {
        List<Long> userIds = registeredPassengerUserIdsForRide(rideId);
        String link = "/user/ride-tracking?rideId=" + rideId;

        createAndPush(
                userIds,
                "RIDE_ACCEPTED",
                "Ride accepted",
                "You were added to a ride and a driver has accepted it.",
                link
        );
    }

    public void notifyRideFinished(long rideId) {
        List<Long> userIds = registeredPassengerUserIdsForRide(rideId);
        String link = "/user/ride-tracking?rideId=" + rideId;

        createAndPush(
                userIds,
                "RIDE_FINISHED",
                "Ride finished",
                "Ride has been successfully completed.",
                link
        );
    }

    public void notifyScheduledRideReminder(long rideId, int minutesToStart) {
        List<Long> userIds = registeredPassengerUserIdsForRide(rideId);
        String link = "/user/ride-tracking?rideId=" + rideId;

        String msg;
        if (minutesToStart <= 0) msg = "Reminder: your scheduled ride should start now.";
        else if (minutesToStart == 1) msg = "Reminder: your scheduled ride starts in 1 minute.";
        else msg = "Reminder: your scheduled ride starts in " + minutesToStart + " minutes.";

        createAndPush(
                userIds,
                "SCHEDULED_RIDE_REMINDER",
                "Scheduled ride reminder",
                msg,
                link
        );
    }

    public void registerDeviceToken(long userId, String token, String platform) {
        if (userId <= 0 || token == null || token.isBlank()) return;
        userDeviceTokenRepository.upsertToken(userId, token.trim(), normalizePlatform(platform));
    }

    private void createAndPush(
            List<Long> userIds,
            String type,
            String title,
            String message,
            String linkUrl
    ) {
        List<NotificationRepository.CreatedNotification> created = repo.createForUsers(
                userIds,
                type,
                title,
                message,
                linkUrl
        );
        pushToRegisteredDevices(created);
    }

    private void pushToRegisteredDevices(List<NotificationRepository.CreatedNotification> created) {
        if (created == null || created.isEmpty()) return;

        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging == null) return;

        Set<Long> targetUserIds = new LinkedHashSet<>();
        for (NotificationRepository.CreatedNotification notification : created) {
            if (notification == null || notification.userId() <= 0) continue;
            targetUserIds.add(notification.userId());
        }
        if (targetUserIds.isEmpty()) return;

        Map<Long, List<String>> tokensByUser = userDeviceTokenRepository.findTokensByUserIds(new ArrayList<>(targetUserIds));
        if (tokensByUser.isEmpty()) return;

        for (NotificationRepository.CreatedNotification notification : created) {
            if (notification == null) continue;

            List<String> tokens = tokensByUser.get(notification.userId());
            if (tokens == null || tokens.isEmpty()) continue;

            sendNotificationInChunks(firebaseMessaging, notification, tokens);
        }
    }

    private void sendNotificationInChunks(
            FirebaseMessaging firebaseMessaging,
            NotificationRepository.CreatedNotification notification,
            List<String> tokens
    ) {
        if (tokens == null || tokens.isEmpty()) return;

        for (int start = 0; start < tokens.size(); start += FCM_MAX_TOKENS_PER_CALL) {
            int endExclusive = Math.min(start + FCM_MAX_TOKENS_PER_CALL, tokens.size());
            List<String> batchTokens = tokens.subList(start, endExclusive);
            try {
                MulticastMessage msg = buildMulticastMessage(notification, batchTokens);
                BatchResponse response = firebaseMessaging.sendEachForMulticast(msg);
                pruneInvalidTokens(batchTokens, response);
            } catch (Exception ignore) {
                // Push must never break the core notification flow.
            }
        }
    }

    private MulticastMessage buildMulticastMessage(
            NotificationRepository.CreatedNotification notification,
            List<String> tokens
    ) {
        String title = nonBlank(notification.title(), "Notification");
        String body = nonBlank(notification.message(), "");
        String type = nonBlank(notification.type(), "UPDATE");
        String linkUrl = nonBlank(notification.linkUrl(), "/user/notifications");

        return MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("id", String.valueOf(notification.notificationId()))
                .putData("type", type)
                .putData("title", title)
                .putData("message", body)
                .putData("linkUrl", linkUrl)
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .build())
                .build();
    }

    private void pruneInvalidTokens(List<String> tokens, BatchResponse response) {
        if (tokens == null || tokens.isEmpty() || response == null) return;
        List<SendResponse> responses = response.getResponses();
        if (responses == null || responses.isEmpty()) return;

        int count = Math.min(tokens.size(), responses.size());
        for (int i = 0; i < count; i++) {
            SendResponse sendResponse = responses.get(i);
            if (sendResponse == null || sendResponse.isSuccessful()) continue;

            FirebaseMessagingException error = sendResponse.getException();
            if (error == null) continue;

            MessagingErrorCode code = error.getMessagingErrorCode();
            if (code == MessagingErrorCode.UNREGISTERED || code == MessagingErrorCode.INVALID_ARGUMENT) {
                userDeviceTokenRepository.deleteToken(tokens.get(i));
            }
        }
    }

    private static String normalizePlatform(String platform) {
        if (platform == null || platform.isBlank()) return "ANDROID";
        String normalized = platform.trim().toUpperCase();
        if ("ANDROID".equals(normalized) || "IOS".equals(normalized) || "WEB".equals(normalized)) {
            return normalized;
        }
        return "ANDROID";
    }

    private static String nonBlank(String value, String fallback) {
        if (value == null || value.isBlank()) return fallback;
        return value.trim();
    }

    private List<Long> registeredPassengerUserIdsForRide(long rideId) {
        return jdbc.sql("""
            select distinct u.id
            from ride_passengers rp
            join users u on lower(u.email) = lower(rp.email)
            where rp.ride_id = ?
              and u.is_active = true
              and u.blocked = false
        """)
                .param(rideId)
                .query(Long.class)
                .list();
    }
}
