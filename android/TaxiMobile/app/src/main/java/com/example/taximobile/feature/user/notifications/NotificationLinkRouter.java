package com.example.taximobile.feature.user.notifications;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.example.taximobile.feature.user.data.dto.response.NotificationResponseDto;
import com.example.taximobile.feature.user.ui.PassengerActiveRideActivity;
import com.example.taximobile.feature.user.ui.UserNotificationsActivity;

import java.util.Locale;

public final class NotificationLinkRouter {

    private NotificationLinkRouter() {}

    public static Intent intentForNotification(Context context, NotificationResponseDto notification) {
        long rideId = extractRideId(notification != null ? notification.getLinkUrl() : null);
        String type = notification != null ? notification.getType() : null;

        if (rideId > 0) {
            Intent i = new Intent(context, PassengerActiveRideActivity.class);
            i.putExtra(PassengerActiveRideActivity.EXTRA_RIDE_ID, rideId);

            if (type != null && !type.trim().isEmpty()) {
                i.putExtra(PassengerActiveRideActivity.EXTRA_NOTIFICATION_TYPE, type.trim());
            }
            if ("RIDE_FINISHED".equalsIgnoreCase(type)) {
                i.putExtra(PassengerActiveRideActivity.EXTRA_READ_ONLY, true);
            }
            return i;
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
        if (isBlank(path)) return false;

        String p = path.trim().toLowerCase(Locale.US);
        if (p.endsWith("/")) {
            p = p.substring(0, p.length() - 1);
        }
        return p.equals("/user/ride-tracking")
                || p.endsWith("/user/ride-tracking")
                || p.equals("/ride-tracking")
                || p.endsWith("/ride-tracking");
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
