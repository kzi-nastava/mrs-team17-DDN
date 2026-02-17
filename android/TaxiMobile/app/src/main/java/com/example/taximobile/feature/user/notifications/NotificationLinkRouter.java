package com.example.taximobile.feature.user.notifications;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.example.taximobile.feature.user.data.dto.response.NotificationResponseDto;
import com.example.taximobile.feature.user.ui.PassengerActiveRideActivity;
import com.example.taximobile.feature.user.ui.PassengerRateRideActivity;
import com.example.taximobile.feature.user.ui.UserFavoriteRoutesActivity;
import com.example.taximobile.feature.user.ui.UserHomeActivity;
import com.example.taximobile.feature.user.ui.UserNotificationsActivity;
import com.example.taximobile.feature.user.ui.UserOrderRideActivity;
import com.example.taximobile.feature.user.ui.UserProfileActivity;
import com.example.taximobile.feature.user.ui.UserReportsActivity;
import com.example.taximobile.feature.user.ui.UserRideHistoryActivity;
import com.example.taximobile.feature.support.ui.SupportChatActivity;

import java.util.Locale;

public final class NotificationLinkRouter {

    private NotificationLinkRouter() {}

    public static Intent intentForNotification(Context context, NotificationResponseDto notification) {
        String linkUrl = notification != null ? notification.getLinkUrl() : null;
        String type = notification != null ? notification.getType() : null;
        Uri uri = toUri(linkUrl);
        String path = normalizePath(uri != null ? uri.getPath() : null);

        if (isRideTrackingPath(path)) {
            long rideId = extractRideId(linkUrl);
            Intent i = new Intent(context, PassengerActiveRideActivity.class);
            if (rideId > 0) {
                i.putExtra(PassengerActiveRideActivity.EXTRA_RIDE_ID, rideId);
            }

            if (type != null && !type.trim().isEmpty()) {
                i.putExtra(PassengerActiveRideActivity.EXTRA_NOTIFICATION_TYPE, type.trim());
            }
            if ("RIDE_FINISHED".equalsIgnoreCase(type)) {
                i.putExtra(PassengerActiveRideActivity.EXTRA_READ_ONLY, true);
            }
            return i;
        }

        if (isRateRidePath(path)) {
            Intent i = new Intent(context, PassengerRateRideActivity.class);
            long rideId = extractRateRideId(path);
            if (rideId > 0) {
                i.putExtra(PassengerRateRideActivity.EXTRA_RIDE_ID, rideId);
            }
            return i;
        }

        if (matchesPath(path, "/user/home", "/home")) {
            return new Intent(context, UserHomeActivity.class);
        }
        if (matchesPath(path, "/user/ride-history", "/ride-history")) {
            return new Intent(context, UserRideHistoryActivity.class);
        }
        if (matchesPath(path, "/user/reports", "/reports")) {
            return new Intent(context, UserReportsActivity.class);
        }
        if (matchesPath(path, "/user/profile", "/profile")) {
            return new Intent(context, UserProfileActivity.class);
        }
        if (matchesPath(path, "/user/support", "/support")) {
            return new Intent(context, SupportChatActivity.class);
        }
        if (matchesPath(path, "/user/order-ride", "/order-ride")) {
            return new Intent(context, UserOrderRideActivity.class);
        }
        if (matchesPath(path, "/user/favourite-rides", "/favourite-rides")) {
            return new Intent(context, UserFavoriteRoutesActivity.class);
        }
        if (matchesPath(path, "/user/notifications", "/notifications")) {
            return new Intent(context, UserNotificationsActivity.class);
        }

        return new Intent(context, UserNotificationsActivity.class);
    }

    public static long extractRideId(String linkUrl) {
        if (isBlank(linkUrl)) return -1L;

        try {
            Uri uri = toUri(linkUrl.trim());
            if (uri == null || !isRideTrackingPath(uri.getPath())) return -1L;

            String rideIdRaw = uri.getQueryParameter("rideId");
            if (isBlank(rideIdRaw)) return -1L;

            long rideId = Long.parseLong(rideIdRaw.trim());
            return rideId > 0 ? rideId : -1L;
        } catch (Exception ignore) {
            return -1L;
        }
    }

    private static Uri toUri(String raw) {
        if (isBlank(raw)) return null;

        if (raw.contains("://")) {
            return Uri.parse(raw);
        }
        if (raw.startsWith("/")) {
            return Uri.parse("https://local.app" + raw);
        }

        return Uri.parse("https://local.app/" + raw);
    }

    private static boolean isRideTrackingPath(String path) {
        String p = normalizePath(path);
        return matchesPath(p, "/user/ride-tracking", "/ride-tracking");
    }

    private static boolean isRateRidePath(String path) {
        String p = normalizePath(path);
        return p.matches("^/user/rides/\\d+/rate$") || p.matches("^/rides/\\d+/rate$");
    }

    private static long extractRateRideId(String normalizedPath) {
        if (isBlank(normalizedPath)) return -1L;

        String[] parts = normalizedPath.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (!"rides".equals(parts[i])) continue;
            if (i + 1 >= parts.length) return -1L;
            try {
                long rideId = Long.parseLong(parts[i + 1]);
                return rideId > 0 ? rideId : -1L;
            } catch (Exception ignore) {
                return -1L;
            }
        }
        return -1L;
    }

    private static boolean matchesPath(String path, String... candidates) {
        if (isBlank(path) || candidates == null) return false;

        for (String candidate : candidates) {
            String c = normalizePath(candidate);
            if (path.equals(c)) return true;
        }
        return false;
    }

    private static String normalizePath(String path) {
        if (isBlank(path)) return "";
        String p = path.trim().toLowerCase(Locale.US);
        if (!p.startsWith("/")) p = "/" + p;
        while (p.length() > 1 && p.endsWith("/")) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
