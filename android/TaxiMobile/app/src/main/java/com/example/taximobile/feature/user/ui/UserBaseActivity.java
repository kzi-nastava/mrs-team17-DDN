package com.example.taximobile.feature.user.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.taximobile.R;
import com.example.taximobile.core.auth.LogoutManager;
import com.example.taximobile.feature.user.notifications.UserForegroundNotificationPoller;
import com.example.taximobile.feature.support.ui.SupportChatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public abstract class UserBaseActivity extends AppCompatActivity {

    protected MaterialToolbar toolbar;
    protected DrawerLayout drawerLayout;
    protected NavigationView navView;
    protected FrameLayout baseContent;
    private UserForegroundNotificationPoller notificationPoller;

    private static final int REQ_POST_NOTIFICATIONS = 8101;
    private static boolean notificationPermissionRequestedThisSession = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_base);

        toolbar = findViewById(R.id.baseToolbar);
        drawerLayout = findViewById(R.id.baseDrawer);
        navView = findViewById(R.id.baseNav);
        baseContent = findViewById(R.id.baseContent);
        notificationPoller = new UserForegroundNotificationPoller(this);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (notificationPoller == null) return;

        if (canStartForegroundNotificationPolling()) {
            notificationPoller.start();
        } else {
            notificationPoller.stop();
        }
    }

    @Override
    protected void onPause() {
        if (notificationPoller != null) {
            notificationPoller.stop();
        }
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode != REQ_POST_NOTIFICATIONS || notificationPoller == null) return;

        if (canStartForegroundNotificationPolling()) {
            notificationPoller.start();
        } else {
            notificationPoller.stop();
        }
    }

    private boolean canStartForegroundNotificationPolling() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        if (!notificationPermissionRequestedThisSession) {
            notificationPermissionRequestedThisSession = true;
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST_NOTIFICATIONS);
        }
        return false;
    }

    protected View inflateContent(int layoutResId) {
        View v = getLayoutInflater().inflate(layoutResId, baseContent, false);
        baseContent.addView(v);
        return v;
    }
}
