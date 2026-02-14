package com.example.taximobile.feature.user.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.taximobile.R;
import com.example.taximobile.core.auth.JwtUtils;
import com.example.taximobile.core.auth.LogoutManager;
import com.example.taximobile.core.network.TokenStorage;
import com.example.taximobile.core.push.PushTokenRepository;
import com.example.taximobile.core.push.TaxiFirebaseMessagingService;
import com.example.taximobile.feature.user.data.NotificationsRepository;
import com.example.taximobile.feature.support.ui.SupportChatActivity;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public abstract class UserBaseActivity extends AppCompatActivity {

    protected MaterialToolbar toolbar;
    protected DrawerLayout drawerLayout;
    protected NavigationView navView;
    protected FrameLayout baseContent;
    private NotificationsRepository notificationsRepo;
    private PushTokenRepository pushTokenRepository;
    private SharedPreferences pushSyncPrefs;

    private final Handler unreadHandler = new Handler(Looper.getMainLooper());
    private static final long UNREAD_POLL_MS = 5_000L;
    private final Runnable unreadRefreshRunnable = new Runnable() {
        @Override
        public void run() {
            refreshUnreadCount();
            unreadHandler.postDelayed(this, UNREAD_POLL_MS);
        }
    };

    private static final String PUSH_SYNC_PREFS = "push_token_sync";
    private static final String PUSH_SYNC_LAST_USER_ID = "last_user_id";
    private static final String PUSH_SYNC_LAST_TOKEN = "last_token";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_base);

        toolbar = findViewById(R.id.baseToolbar);
        drawerLayout = findViewById(R.id.baseDrawer);
        navView = findViewById(R.id.baseNav);
        baseContent = findViewById(R.id.baseContent);
        notificationsRepo = new NotificationsRepository(this);
        pushTokenRepository = new PushTokenRepository(this);
        pushSyncPrefs = getSharedPreferences(PUSH_SYNC_PREFS, MODE_PRIVATE);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.drawer_open,
                R.string.drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                if (!(this instanceof UserHomeActivity)) {
                    startActivity(new Intent(this, UserHomeActivity.class));
                }

            } else if (id == R.id.nav_history) {
                if (!(this instanceof UserRideHistoryActivity)) {
                    startActivity(new Intent(this, UserRideHistoryActivity.class));
                }

            } else if (id == R.id.nav_rate_ride) {
                if (!(this instanceof PassengerRateRideActivity)) {
                    startActivity(new Intent(this, PassengerRateRideActivity.class));
                }

            } else if (id == R.id.nav_live_tracking) {
                if (!(this instanceof PassengerActiveRideActivity)) {
                    startActivity(new Intent(this, PassengerActiveRideActivity.class));
                }

            } else if (id == R.id.nav_support) {
                startActivity(new Intent(this, SupportChatActivity.class));

            } else if (id == R.id.nav_profile) {
                if (!(this instanceof UserProfileActivity)) {
                    startActivity(new Intent(this, UserProfileActivity.class));
                }

            } else if (id == R.id.nav_notifications) {
                if (!(this instanceof UserNotificationsActivity)) {
                    startActivity(new Intent(this, UserNotificationsActivity.class));
                }

            } else if (id == R.id.nav_reports) {
                if (!(this instanceof UserReportsActivity)) {
                    startActivity(new Intent(this, UserReportsActivity.class));
                }

            } else if (id == R.id.nav_logout) {
                LogoutManager.logout(this);
                drawerLayout.closeDrawers();
                return true;

            } else {
                Intent i = new Intent(this, UserPlaceholderActivity.class);
                i.putExtra(UserPlaceholderActivity.EXTRA_TITLE, String.valueOf(item.getTitle()));
                startActivity(i);
            }

            drawerLayout.closeDrawers();
            return true;
        });

        refreshUnreadCount();
        registerPushTokenWithBackend();
        markNotificationAsReadFromIntent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerPushTokenWithBackend();
        markNotificationAsReadFromIntent();
        startUnreadPolling();
    }

    @Override
    protected void onPause() {
        stopUnreadPolling();
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            setIntent(intent);
        }
        markNotificationAsReadFromIntent();
    }

    private void startUnreadPolling() {
        unreadHandler.removeCallbacks(unreadRefreshRunnable);
        unreadHandler.post(unreadRefreshRunnable);
    }

    private void stopUnreadPolling() {
        unreadHandler.removeCallbacks(unreadRefreshRunnable);
    }

    private void refreshUnreadCount() {
        if (notificationsRepo == null || navView == null) return;

        notificationsRepo.unreadCount(new NotificationsRepository.CountCb() {
            @Override
            public void onSuccess(long count) {
                runOnUiThread(() -> updateNotificationsMenuTitle(count));
            }

            @Override
            public void onError(String msg) {
                // Keep the current title; transient network errors should not reset UI state.
            }
        });
    }

    private void markNotificationAsReadFromIntent() {
        Intent in = getIntent();
        if (in == null || notificationsRepo == null) return;

        long notificationId = in.getLongExtra(TaxiFirebaseMessagingService.EXTRA_NOTIFICATION_ID, -1L);
        if (notificationId <= 0) return;

        in.removeExtra(TaxiFirebaseMessagingService.EXTRA_NOTIFICATION_ID);
        notificationsRepo.markRead(notificationId, new NotificationsRepository.VoidCb() {
            @Override
            public void onSuccess() {
                refreshUnreadCount();
            }

            @Override
            public void onError(String msg) {
                refreshUnreadCount();
            }
        });
    }

    private void updateNotificationsMenuTitle(long unreadCount) {
        if (navView == null || navView.getMenu() == null) return;
        android.view.MenuItem item = navView.getMenu().findItem(R.id.nav_notifications);
        if (item == null) return;

        String baseTitle = getString(R.string.menu_notifications);
        if (unreadCount > 0) {
            item.setTitle(baseTitle + " (" + unreadCount + ")");
        } else {
            item.setTitle(baseTitle);
        }
    }

    private void registerPushTokenWithBackend() {
        Long userId = currentAuthenticatedUserId();
        if (userId == null || userId <= 0 || pushTokenRepository == null) return;

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) return;

            String currentFcmToken = task.getResult();
            if (currentFcmToken == null || currentFcmToken.trim().isEmpty()) return;

            String normalizedToken = currentFcmToken.trim();
            if (isPushTokenAlreadySynced(userId, normalizedToken)) return;

            pushTokenRepository.registerToken(normalizedToken, "ANDROID", new PushTokenRepository.VoidCb() {
                @Override
                public void onSuccess() {
                    savePushTokenSync(userId, normalizedToken);
                }

                @Override
                public void onError(String msg) {
                    // Try again on next resume/polling cycle.
                }
            });
        });
    }

    private Long currentAuthenticatedUserId() {
        String jwt = new TokenStorage(getApplicationContext()).getToken();
        if (jwt == null || jwt.trim().isEmpty()) return null;
        return JwtUtils.getUserIdFromSub(jwt);
    }

    private boolean isPushTokenAlreadySynced(long userId, String token) {
        if (pushSyncPrefs == null || token == null || token.trim().isEmpty()) return false;
        long lastUserId = pushSyncPrefs.getLong(PUSH_SYNC_LAST_USER_ID, -1L);
        String lastToken = pushSyncPrefs.getString(PUSH_SYNC_LAST_TOKEN, null);
        return userId == lastUserId && token.equals(lastToken);
    }

    private void savePushTokenSync(long userId, String token) {
        if (pushSyncPrefs == null || token == null || token.trim().isEmpty()) return;
        pushSyncPrefs.edit()
                .putLong(PUSH_SYNC_LAST_USER_ID, userId)
                .putString(PUSH_SYNC_LAST_TOKEN, token)
                .apply();
    }

    protected View inflateContent(int layoutResId) {
        View v = getLayoutInflater().inflate(layoutResId, baseContent, false);
        baseContent.addView(v);
        return v;
    }
}
