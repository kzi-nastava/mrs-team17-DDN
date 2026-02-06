package com.example.taximobile.feature.admin.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.feature.admin.data.AdminChatRepository;
import com.example.taximobile.feature.support.data.dto.response.ChatThreadResponseDto;

import java.util.ArrayList;
import java.util.List;

public class AdminSupportThreadsActivity extends AdminBaseActivity {

    private RecyclerView recycler;
    private TextView tvEmpty, tvError;

    private final List<ChatThreadResponseDto> items = new ArrayList<>();
    private AdminThreadsAdapter adapter;
    private AdminChatRepository repo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_admin_threads);
        toolbar.setTitle("Support");

        recycler = v.findViewById(R.id.recycler);
        tvEmpty = v.findViewById(R.id.tvEmpty);
        tvError = v.findViewById(R.id.tvError);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminThreadsAdapter(items, this::openThread);

        recycler.setAdapter(adapter);

        repo = new AdminChatRepository(this);
        load();
    }

    private void load() {
        repo.listThreads(null, 50, new AdminChatRepository.ThreadsCb() {
            @Override public void onSuccess(List<ChatThreadResponseDto> list) {
                runOnUiThread(() -> {
                    items.clear();
                    items.addAll(list);
                    adapter.notifyDataSetChanged();
                    tvEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override public void onError(String msg) {
                runOnUiThread(() -> {
                    tvError.setText(msg);
                    tvError.setVisibility(View.VISIBLE);
                });
            }
        });
    }

    private void openThread(ChatThreadResponseDto t) {
        Intent i = new Intent(this, AdminSupportChatActivity.class);
        i.putExtra(AdminSupportChatActivity.EXTRA_THREAD_ID, t.getId());
        i.putExtra(AdminSupportChatActivity.EXTRA_TITLE,
                t.getUserName() != null ? t.getUserName() : t.getUserEmail());
        startActivity(i);
    }
}
