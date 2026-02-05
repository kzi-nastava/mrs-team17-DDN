package com.example.taximobile.core.auth;

import android.app.Activity;
import android.content.Intent;

import com.example.taximobile.core.network.TokenStorage;
import com.example.taximobile.feature.auth.ui.LoginActivity;

public final class LogoutManager {

    private LogoutManager() {}

    public static void logout(Activity activity) {
        new TokenStorage(activity.getApplicationContext()).clear();

        Intent i = new Intent(activity, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(i);
        activity.finish();
    }
}
