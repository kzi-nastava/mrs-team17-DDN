package com.example.taximobile.feature.user.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.feature.user.data.NotificationsRepository;
import com.example.taximobile.feature.user.data.dto.response.NotificationResponseDto;
import com.example.taximobile.feature.user.notifications.NotificationLinkRouter;

import java.util.ArrayList;
import java.util.List;

public class UserNotificationsActivity extends UserBaseActivity
        implements NotificationsAdapter.OnNotificationClick {

    private NotificationsRepository repo;

    private ProgressBar progress;
    private TextView empty;
    private RecyclerView list;

    private NotificationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_user_notifications);

        toolbar.setTitle(getString(R.string.title_notifications));

        repo = new NotificationsRepository(this);

        progress = v.findViewById(R.id.nProgress);
        empty = v.findViewById(R.id.nEmpty);
        list = v.findViewById(R.id.nList);

        adapter = new NotificationsAdapter(new ArrayList<>(), this);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        load();
    }

    private void load() {
        setLoading(true);
        repo.list(100, new NotificationsRepository.ListCb() {
            @Override
            public void onSuccess(List<NotificationResponseDto> items) {
                adapter.setItems(items);
                setLoading(false);
                empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(String msg) {
                setLoading(false);
                Toast.makeText(UserNotificationsActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        list.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(NotificationResponseDto n, int position) {
        if (n.getReadAt() == null) {
            adapter.markReadAt(position);
            repo.markRead(n.getId(), new NotificationsRepository.VoidCb() {
                @Override public void onSuccess() {}
                @Override public void onError(String msg) {
                    Toast.makeText(UserNotificationsActivity.this, msg, Toast.LENGTH_LONG).show();
                }
            });
        }

        Intent target = NotificationLinkRouter.intentForNotification(this, n);
        if (target.getComponent() != null
                && UserNotificationsActivity.class.getName().equals(target.getComponent().getClassName())) {
            return;
        }
        target.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(target);
    }
}
