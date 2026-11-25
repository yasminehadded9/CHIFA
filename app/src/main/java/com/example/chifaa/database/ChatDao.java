package com.example.chifaa.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.chifaa.models.ChatMessage;
import com.example.chifaa.models.ChatSessionSummary;
import com.example.chifaa.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Conversations du compagnon (module M5) : tables chat_sessions, messages, safety_events.
 */
public class ChatDao {

    private final DatabaseHelper helper;

    public ChatDao(Context context) {
        helper = DatabaseHelper.getInstance(context);
    }

    // ================================================================= Sessions

    public String createSession(String userPid) {
        String id = UUID.randomUUID().toString();
        ContentValues v = new ContentValues();
        v.put("session_id", id);
        v.put("user_pid", userPid);
        v.put("channel", "App");
        v.put("started_at", DateUtils.nowUtc());
        helper.getWritableDatabase().insertOrThrow(DbSchema.T_CHAT_SESSIONS, null, v);
        return id;
    }

    public void endSession(String sessionId) {
        ContentValues v = new ContentValues();
        v.put("ended_at", DateUtils.nowUtc());
        helper.getWritableDatabase().update(DbSchema.T_CHAT_SESSIONS, v, "session_id = ?", new String[]{sessionId});
    }

    /** Sécurité : une utilisatrice ne peut ouvrir que ses propres conversations. */
    public boolean belongsTo(String sessionId, String userPid) {
        Cursor c = helper.getReadableDatabase().rawQuery("SELECT 1 FROM " + DbSchema.T_CHAT_SESSIONS
                + " WHERE session_id = ? AND user_pid = ?", new String[]{sessionId, userPid});
        boolean ok = c.moveToFirst();
        c.close();
        return ok;
    }

    /** Historique : une ligne par conversation, la plus récente d'abord. */
    public List<ChatSessionSummary> getSessions(String userPid) {
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT s.session_id, s.started_at, "
                        + "(SELECT m.message_text FROM " + DbSchema.T_MESSAGES + " m WHERE m.session_id = s.session_id "
                        + " AND m.sender_role = 'User' ORDER BY m.message_timestamp LIMIT 1), "
                        + "(SELECT COUNT(*) FROM " + DbSchema.T_MESSAGES + " m WHERE m.session_id = s.session_id), "
                        + "(SELECT MAX(m.distress_flag) FROM " + DbSchema.T_MESSAGES + " m WHERE m.session_id = s.session_id) "
                        + "FROM " + DbSchema.T_CHAT_SESSIONS + " s WHERE s.user_pid = ? "
                        + "ORDER BY s.started_at DESC", new String[]{userPid});
        List<ChatSessionSummary> result = new ArrayList<>();
        while (c.moveToNext()) {
            ChatSessionSummary s = new ChatSessionSummary();
            s.sessionId = c.getString(0);
            s.startedAt = c.getString(1);
            s.preview = c.getString(2);
            s.messageCount = c.getInt(3);
            s.hadDistress = !c.isNull(4) && c.getInt(4) == 1;
            if (s.messageCount > 0) result.add(s); // on n'affiche pas les conversations vides
        }
        c.close();
        return result;
    }

    // ================================================================= Messages

    /**
     * Enregistre un message.
     * @param language   langue détectée (Darija, French, MSA, Mixed)
     * @param latencyMs  temps de réponse du compagnon (null pour un message de l'utilisatrice)
     * @param kbVersion  version de la base de contenus utilisée, format KB-YYYYMMDD (ou null)
     */
    public String addMessage(ChatMessage m, String language, Integer latencyMs, String kbVersion) {
        String id = UUID.randomUUID().toString();
        ContentValues v = new ContentValues();
        v.put("message_id", id);
        v.put("session_id", m.sessionId);
        v.put("content_id", m.contentId);
        v.put("sender_role", m.role);
        v.put("message_timestamp", m.timestamp);
        v.put("message_text", m.text);
        v.put("detected_language", language);
        v.put("topic_tag", m.topicTag);
        v.put("distress_flag", m.distress ? 1 : 0);
        v.put("rephrase_flag", 0);
        v.put("error_flag", 0);
        v.put("response_latency_ms", latencyMs);
        v.put("kb_version", kbVersion);
        helper.getWritableDatabase().insertOrThrow(DbSchema.T_MESSAGES, null, v);
        m.messageId = id;
        return id;
    }

    /** Note de 1 à 5 donnée par l'utilisatrice à une réponse. */
    public void setFeedback(String messageId, int rating) {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Note entre 1 et 5");
        ContentValues v = new ContentValues();
        v.put("user_feedback", rating);
        helper.getWritableDatabase().update(DbSchema.T_MESSAGES, v,
                "message_id = ? AND sender_role = 'AI'", new String[]{messageId});
    }

    /** Messages d'une conversation, dans l'ordre, avec le titre de la fiche utilisée. */
    public List<ChatMessage> getMessages(String sessionId) {
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT m.message_id, m.sender_role, m.message_text, m.message_timestamp, m.content_id, "
                        + "k.title, m.topic_tag, m.distress_flag, m.user_feedback, m.detected_language "
                        + "FROM " + DbSchema.T_MESSAGES + " m "
                        + "LEFT JOIN " + DbSchema.T_KB_ITEMS + " k ON k.content_id = m.content_id "
                        + "WHERE m.session_id = ? ORDER BY m.message_timestamp, m.rowid",
                new String[]{sessionId});
        List<ChatMessage> result = new ArrayList<>();
        while (c.moveToNext()) {
            ChatMessage m = new ChatMessage();
            m.messageId = c.getString(0);
            m.sessionId = sessionId;
            m.role = c.getString(1);
            m.text = c.getString(2);
            m.timestamp = c.getString(3);
            m.contentId = c.getString(4);
            m.contentTitle = c.getString(5);
            m.topicTag = c.getString(6);
            m.distress = c.getInt(7) == 1;
            m.feedback = c.isNull(8) ? null : c.getInt(8);
            m.language = c.getString(9);
            result.add(m);
        }
        c.close();
        return result;
    }

    // ===================================================== Événement de sécurité

    /**
     * Détresse détectée : un événement est créé pour la file de sécurité du back-office
     * (statut "Open", à traiter par un référent sécurité).
     */
    public void addSafetyEvent(String userPid, String sessionId, String severity) {
        ContentValues v = new ContentValues();
        v.put("event_id", UUID.randomUUID().toString());
        v.put("user_pid", userPid);
        v.put("session_id", sessionId);
        v.put("trigger_type", "Distress keyword");
        v.put("severity", severity);
        v.put("action_taken", "Resource shared");
        v.put("event_status", "Open");
        v.put("flagged_at", DateUtils.nowUtc());
        SQLiteDatabase db = helper.getWritableDatabase();
        db.insertOrThrow(DbSchema.T_SAFETY_EVENTS, null, v);
    }
}
