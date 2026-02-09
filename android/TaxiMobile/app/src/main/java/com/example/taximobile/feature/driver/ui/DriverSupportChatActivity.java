package com.example.taximobile.feature.driver.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taximobile.R;
import com.example.taximobile.feature.support.adapter.ChatMessagesAdapter;
import com.example.taximobile.feature.support.data.ChatRepository;
import com.example.taximobile.feature.support.data.dto.response.ChatMessageResponseDto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DriverSupportChatActivity extends DriverBaseActivity {

    private RecyclerView recycler;
    private TextView tvEmpty;
    private TextView tvError;
    private EditText etMessage;
    private Button btnSend;

    private ChatRepository repo;

    private final ArrayList<ChatMessageResponseDto> items = new ArrayList<>();
    private final HashSet<Long> seenIds = new HashSet<>();
    private ChatMessagesAdapter adapter;

    private final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
    private static final long POLL_MS = 2000L;
    private long lastId = 0L;

    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            pollNewMessages();
            handler.postDelayed(this, POLL_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflateContent(R.layout.activity_support_chat);
        toolbar.setTitle(getString(R.string.menu_support));

        repo = new ChatRepository(this);

        recycler = v.findViewById(R.id.recycler);
        tvEmpty = v.findViewById(R.id.tvEmpty);
        tvError = v.findViewById(R.id.tvError);
        etMessage = v.findViewById(R.id.etMessage);
        btnSend = v.findViewById(R.id.btnSend);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        recycler.setLayoutManager(lm);

        adapter = new ChatMessagesAdapter(items);
        recycler.setAdapter(adapter);

        btnSend.setOnClickListener(view -> sendMessage());

        repo.getMyThread(new ChatRepository.ThreadCb() {
            @Override
            public void onSuccess(com.example.taximobile.feature.support.data.dto.response.ChatThreadResponseDto thread) {
            }

            @Override
            public void onError(String msg) {
            }
        });

        loadInitial();
    }

    private void loadInitial() {
        showError(null);

        repo.getMyMessages(null, 50, new ChatRepository.MessagesCb() {
            @Override
            public void onSuccess(List<ChatMessageResponseDto> list) {
                runOnUiThread(() -> {
                    items.clear();
                    seenIds.clear();
                    lastId = 0L;

                    if (list != null) {
                        for (ChatMessageResponseDto m : list) {
                            addIfNew(m);
                        }
                    }

                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    scrollToBottom();
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    showError(msg);
                    updateEmptyState();
                });
            }
        });
    }

    private void pollNewMessages() {
        Long after = lastId > 0 ? lastId : null;

        repo.getMyMessages(after, 200, new ChatRepository.MessagesCb() {
            @Override
            public void onSuccess(List<ChatMessageResponseDto> list) {
                if (list == null || list.isEmpty()) {
                    return;
                }

                runOnUiThread(() -> {
                    int start = items.size();
                    int added = 0;

                    for (ChatMessageResponseDto m : list) {
                        if (addIfNew(m)) {
                            added++;
                        }
                    }

                    if (added > 0) {
                        adapter.notifyItemRangeInserted(start, added);
                        updateEmptyState();
                        scrollToBottom();
                    }
                });
            }

            @Override
            public void onError(String msg) {
            }
        });
    }

    private void sendMessage() {
        showError(null);

        String content = etMessage.getText() != null ? etMessage.getText().toString().trim() : "";
        if (TextUtils.isEmpty(content)) {
            return;
        }

        setSending(true);

        repo.sendMyMessage(content, new ChatRepository.SendCb() {
            @Override
            public void onSuccess(ChatMessageResponseDto msg) {
                runOnUiThread(() -> {
                    setSending(false);
                    etMessage.setText("");

                    boolean added = addIfNew(msg);
                    if (added) {
                        adapter.notifyItemInserted(items.size() - 1);
                        updateEmptyState();
                        scrollToBottom();
                    }
                });
            }

            @Override
            public void onError(String msg) {
                runOnUiThread(() -> {
                    setSending(false);
                    showError(msg);
                });
            }
        });
    }

    private boolean addIfNew(ChatMessageResponseDto m) {
        if (m == null) {
            return false;
        }
        long id = m.getId();
        if (id <= 0 || seenIds.contains(id)) {
            return false;
        }

        seenIds.add(id);
        items.add(m);
        lastId = Math.max(lastId, id);
        return true;
    }

    private void setSending(boolean sending) {
        btnSend.setEnabled(!sending);
        btnSend.setText(sending ? "..." : getString(R.string.support_send));
    }

    private void updateEmptyState() {
        boolean empty = items.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    private void showError(String msg) {
        if (msg == null || msg.trim().isEmpty()) {
            tvError.setVisibility(View.GONE);
        } else {
            tvError.setText(msg);
            tvError.setVisibility(View.VISIBLE);
        }
    }

    private void scrollToBottom() {
        if (items.isEmpty()) {
            return;
        }
        recycler.post(() -> recycler.scrollToPosition(items.size() - 1));
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacks(pollRunnable);
        handler.post(pollRunnable);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(pollRunnable);
        super.onPause();
    }
}
