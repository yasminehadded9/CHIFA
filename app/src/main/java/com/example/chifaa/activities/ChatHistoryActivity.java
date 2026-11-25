package com.example.chifaa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chifaa.R;
import com.example.chifaa.database.ChatDao;
import com.example.chifaa.models.ChatSessionSummary;
import com.example.chifaa.utils.LabelUtils;
import com.example.chifaa.utils.SessionManager;
import com.example.chifaa.utils.UiUtils;

import java.util.List;

/** Historique des conversations (sessions enregistrées avec leurs messages). */
public class ChatHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);
        UiUtils.setupHeader(this, 0, R.string.chat_history_title, R.string.chat_history_subtitle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String pid = new SessionManager(this).getUserPid();
        if (pid == null) {
            finish();
            return;
        }
        List<ChatSessionSummary> sessions = new ChatDao(this).getSessions(pid);
        RecyclerView rv = findViewById(R.id.rvSessions);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new SessionAdapter(sessions));
        findViewById(R.id.tvEmpty).setVisibility(sessions.isEmpty() ? View.VISIBLE : View.GONE);
    }

    public void nouvelleConversation(View v) {
        startActivity(new Intent(this, ChatActivity.class));
        finish();
    }

    private void ouvrir(String sessionId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_SESSION_ID, sessionId);
        startActivity(intent);
    }

    /** Adaptateur interne : une carte par conversation. */
    private class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.Holder> {

        private final List<ChatSessionSummary> sessions;

        SessionAdapter(List<ChatSessionSummary> sessions) {
            this.sessions = sessions;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_session, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            final ChatSessionSummary s = sessions.get(position);
            h.tvPreview.setText(s.preview == null ? "…" : s.preview);
            h.tvMeta.setText(LabelUtils.mediumDateTimeUtc(s.startedAt) + " · "
                    + getString(R.string.chat_history_count_fmt, s.messageCount));
            h.tvSupport.setVisibility(s.hadDistress ? View.VISIBLE : View.GONE);
            h.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ouvrir(s.sessionId);
                }
            });
        }

        @Override
        public int getItemCount() {
            return sessions.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            final TextView tvPreview, tvMeta, tvSupport;

            Holder(@NonNull View v) {
                super(v);
                tvPreview = v.findViewById(R.id.tvPreview);
                tvMeta = v.findViewById(R.id.tvMeta);
                tvSupport = v.findViewById(R.id.tvSupport);
            }
        }
    }
}
