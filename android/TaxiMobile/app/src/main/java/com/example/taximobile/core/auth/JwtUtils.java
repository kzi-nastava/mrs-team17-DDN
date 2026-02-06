package com.example.taximobile.core.auth;

import android.util.Base64;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public final class JwtUtils {

    private JwtUtils() {}

    public static String getRole(String token) {
        try {
            JSONObject payload = getPayload(token);
            return payload.optString("role", null);
        } catch (Exception e) {
            return null;
        }
    }

    public static Long getDriverId(String token) {
        try {
            JSONObject payload = getPayload(token);
            if (!payload.has("driverId")) return null;
            return payload.getLong("driverId");
        } catch (Exception e) {
            return null;
        }
    }

    public static Long getUserIdFromSub(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            // "sub" je u headeru/payload-u? Kod tebe je subject u JWT, u payloadu ga jjwt mapira kao "sub"
            JSONObject payload = getPayload(token);
            String sub = payload.optString("sub", null);
            if (sub == null) return null;
            return Long.parseLong(sub);
        } catch (Exception e) {
            return null;
        }
    }

    private static JSONObject getPayload(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length < 2) throw new IllegalArgumentException("Invalid JWT");

        String payloadB64 = parts[1];
        byte[] decoded = Base64.decode(payloadB64, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        String json = new String(decoded, StandardCharsets.UTF_8);
        return new JSONObject(json);
    }
}
