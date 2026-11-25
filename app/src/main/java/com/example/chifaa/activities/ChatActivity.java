package com.example.chifaa.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chifaa.R;
import com.example.chifaa.adapters.ChatAdapter;
import com.example.chifaa.companion.CompanionEngine;
import com.example.chifaa.database.AccountDao;
import com.example.chifaa.database.ChatDao;
import com.example.chifaa.database.KbDao;
import com.example.chifaa.models.ChatMessage;
import com.example.chifaa.models.UserProfile;
import com.example.chifaa.utils.AppConstants;
import com.example.chifaa.utils.DateUtils;
import com.example.chifaa.utils.LabelUtils;
import com.example.chifaa.utils.LocalizedText;
import com.example.chifaa.utils.SessionManager;
import com.example.chifaa.utils.UiUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Compagnon conversationnel (module M5, version de base).
 * Chaque question est analysée par CompanionEngine puis enregistrée (messages) avec
 * la date, la langue détectée et le sujet. En cas de détresse : ressources d'aide
 * immédiates + événement de sécurité dans la base.
 */
public class ChatActivity extends AppCompatActivity implements ChatAdapter.Listener {

    /** Conversation existante à rouvrir (depuis l'historique). */
    public static final String EXTRA_SESSION_ID = "session_id";

    static final int MIN_LENGTH = 2;
    static final int MAX_LENGTH = 500;
    private static final long REPLY_DELAY_MS = 700; // petit délai : la réponse ne "claque" pas

    private ChatDao chatDao;
    private KbDao kbDao;
    private String userPid;
    private String sessionId; // créée au premier message (pas de conversation vide)
    private String preferredLanguage; // langue préférée du profil (pour les questions "Mixed")

    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;
    private RecyclerView rv;
    private TextInputLayout tilMessage;
    private TextInputEditText etMessage;
    private FloatingActionButton fabSend;
    private View suggestions, tvTyping;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatDao = new ChatDao(this);
        kbDao = new KbDao(this);
        userPid = new SessionManager(this).getUserPid();
        if (userPid == null) {
            finish();
            return;
        }

        UserProfile profile = new AccountDao(this).getProfile(userPid);
        preferredLanguage = profile == null ? null : profile.preferredLanguage;

        rv = findViewById(R.id.rvMessages);
        tilMessage = findViewById(R.id.tilMessage);
        etMessage = findViewById(R.id.etMessage);
        fabSend = findViewById(R.id.fabSend);
        suggestions = findViewById(R.id.suggestions);
        tvTyping = findViewById(R.id.tvTyping);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true); // les derniers messages restent en bas
        rv.setLayoutManager(lm);
        adapter = new ChatAdapter(messages, this);
        rv.setAdapter(adapter);

        UiUtils.clearErrorOnInput(tilMessage);
        etMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    envoyer(v);
                    return true;
                }
                return false;
            }
        });

        // Conversation existante (historique) ou nouvelle conversation
        String existing = getIntent().getStringExtra(EXTRA_SESSION_ID);
        if (existing != null && chatDao.belongsTo(existing, userPid)) {
            sessionId = existing;
            messages.addAll(chatDao.getMessages(sessionId));
            suggestions.setVisibility(View.GONE);
        } else {
            messages.add(messageAccueil());
            creerSuggestions();
        }
        adapter.notifyDataSetChanged();
        defilerEnBas();
    }

    /** Message d'accueil affiché, mais pas enregistré en base. */
    private ChatMessage messageAccueil() {
        ChatMessage m = new ChatMessage();
        m.role = ChatMessage.ROLE_AI;
        m.text = getString(R.string.chat_welcome);
        return m;
    }

    private void creerSuggestions() {
        ChipGroup group = findViewById(R.id.chipsSuggestions);
        int[] texts = {R.string.chat_suggestion_1, R.string.chat_suggestion_2,
                R.string.chat_suggestion_3, R.string.chat_suggestion_4};
        for (int res : texts) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_filter_chip, group, false);
            chip.setText(res);
            chip.setCheckable(false);
            final String question = getString(res);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    traiterQuestion(question);
                }
            });
            group.addView(chip);
        }
    }

    // ================================================================== Envoi

    /** Bouton "Envoyer" (ou touche Envoyer du clavier). */
    public void envoyer(View v) {
        String text = etMessage.getText() == null ? "" : etMessage.getText().toString().trim();

        // Contrôle de saisie
        if (text.isEmpty()) {
            UiUtils.showFieldError(tilMessage, getString(R.string.chat_error_empty));
            return;
        }
        if (text.length() < MIN_LENGTH) {
            UiUtils.showFieldError(tilMessage, getString(R.string.chat_error_short));
            return;
        }
        if (text.length() > MAX_LENGTH) {
            UiUtils.showFieldError(tilMessage, getString(R.string.chat_error_long));
            return;
        }
        etMessage.setText("");
        traiterQuestion(text);
    }

    private void traiterQuestion(String question) {
        suggestions.setVisibility(View.GONE);
        if (sessionId == null) {
            sessionId = chatDao.createSession(userPid);
        }

        // 1. Analyse (rapide, locale) : on mesure le temps de réponse réel
        long start = System.currentTimeMillis();
        final CompanionEngine.Result result =
                CompanionEngine.analyze(question, kbDao.search(null, null, null, null));
        final int latency = (int) (System.currentTimeMillis() - start);
        final boolean distress = result.kind == CompanionEngine.Kind.DISTRESS;
        // La réponse est rédigée dans la langue de la question
        final String replyLanguage = CompanionEngine.replyLanguage(result.language, preferredLanguage);

        // 2. Message de l'utilisatrice (langue détectée + indicateur de détresse)
        ChatMessage userMsg = new ChatMessage();
        userMsg.sessionId = sessionId;
        userMsg.role = ChatMessage.ROLE_USER;
        userMsg.text = question;
        userMsg.timestamp = DateUtils.nowUtc();
        userMsg.topicTag = result.topic;
        userMsg.distress = distress;
        userMsg.language = result.language;
        chatDao.addMessage(userMsg, result.language, null, null);
        ajouter(userMsg);

        // 3. Détresse : événement de sécurité IMMÉDIAT (avant même l'affichage)
        if (distress) {
            chatDao.addSafetyEvent(userPid, sessionId, result.severity);
        }

        // 4. Réponse du compagnon après un court délai (la carte détresse s'affiche sans délai)
        fabSend.setEnabled(false);
        tvTyping.setVisibility(distress ? View.GONE : View.VISIBLE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ChatMessage reply = construireReponse(result, replyLanguage);
                chatDao.addMessage(reply, replyLanguage, latency, kbVersion());
                ajouter(reply);
                tvTyping.setVisibility(View.GONE);
                fabSend.setEnabled(true);
            }
        }, distress ? 0 : REPLY_DELAY_MS);
    }

    /**
     * Transforme le résultat de l'analyse en message du compagnon,
     * rédigé dans la langue de la question (français, arabe standard ou darija).
     */
    private ChatMessage construireReponse(CompanionEngine.Result r, String language) {
        Context lc = LocalizedText.forLanguage(this, language); // textes dans la bonne langue
        ChatMessage m = new ChatMessage();
        m.sessionId = sessionId;
        m.role = ChatMessage.ROLE_AI;
        m.timestamp = DateUtils.nowUtc();
        m.language = language;

        switch (r.kind) {
            case DISTRESS:
                m.topicTag = ChatMessage.TAG_DISTRESS;
                m.distress = true;
                m.text = lc.getString(R.string.chat_distress_title) + "\n" + lc.getString(R.string.chat_distress_text);
                break;
            case DOSAGE:
                m.topicTag = ChatMessage.TAG_MEDICAL_LIMIT;
                m.text = lc.getString(R.string.chat_dosage);
                break;
            case DIAGNOSIS:
                m.topicTag = ChatMessage.TAG_MEDICAL_LIMIT;
                m.text = lc.getString(R.string.chat_diagnosis);
                break;
            case ANSWER:
                m.topicTag = r.topic;
                m.contentId = r.item.contentId;
                m.contentTitle = r.item.title;
                String text = lc.getString(R.string.chat_answer_intro_fmt, r.item.title, r.excerpt);
                // La fiche n'existe pas dans la langue de la question : on le signale
                if (!language.equals(r.item.language)) {
                    text += "\n\n" + lc.getString(R.string.chat_answer_other_language_fmt,
                            LabelUtils.label(lc, R.array.content_languages,
                                    AppConstants.CONTENT_LANGUAGES, r.item.language));
                }
                m.text = text + "\n\n" + lc.getString(R.string.chat_answer_outro);
                break;
            default: // NO_MATCH : le compagnon le dit et propose l'annuaire
                m.topicTag = ChatMessage.TAG_NO_MATCH;
                m.text = lc.getString(R.string.chat_no_match);
                break;
        }
        return m;
    }

    /** Version de la base de contenus au format KB-YYYYMMDD. */
    private String kbVersion() {
        return "KB-" + DateUtils.todayUtc().replace("-", "");
    }

    private void ajouter(ChatMessage m) {
        messages.add(m);
        adapter.notifyItemInserted(messages.size() - 1);
        defilerEnBas();
    }

    private void defilerEnBas() {
        if (!messages.isEmpty()) rv.scrollToPosition(messages.size() - 1);
    }

    // ============================================================ ChatAdapter

    @Override
    public void onOpenCard(String contentId) {
        Intent intent = new Intent(this, ArticleActivity.class);
        intent.putExtra(ArticleActivity.EXTRA_CONTENT_ID, contentId);
        startActivity(intent);
    }

    @Override
    public void onOpenDirectory(String serviceType) {
        Intent intent = new Intent(this, DirectoryActivity.class);
        if (serviceType != null) intent.putExtra(DirectoryActivity.EXTRA_SERVICE_TYPE, serviceType);
        startActivity(intent);
    }

    @Override
    public void onRate(ChatMessage message, int rating) {
        chatDao.setFeedback(message.messageId, rating);
        message.feedback = rating;
        adapter.notifyItemChanged(messages.indexOf(message));
    }

    // ============================================================= Navigation

    public void retour(View v) {
        finish();
    }

    public void ouvrirHistorique(View v) {
        startActivity(new Intent(this, ChatHistoryActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (sessionId != null) {
            chatDao.endSession(sessionId); // date de fin de la conversation
        }
        super.onDestroy();
    }
}
