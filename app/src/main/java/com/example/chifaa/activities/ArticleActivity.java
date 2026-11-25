package com.example.chifaa.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chifaa.R;
import com.example.chifaa.database.KbDao;
import com.example.chifaa.models.KbItem;
import com.example.chifaa.utils.AppConstants;
import com.example.chifaa.utils.LabelUtils;
import com.example.chifaa.utils.ReadHistory;
import com.example.chifaa.utils.SessionManager;
import com.example.chifaa.utils.UiUtils;

/**
 * Fiche détaillée (M4) : titre, texte, source, date de revue, niveau de preuve.
 * La fiche est marquée "consultée" (lecture hors ligne).
 */
public class ArticleActivity extends AppCompatActivity {

    public static final String EXTRA_CONTENT_ID = "content_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        UiUtils.setupHeader(this, 0, R.string.article_title, R.string.library_subtitle);

        String contentId = getIntent().getStringExtra(EXTRA_CONTENT_ID);
        KbItem item = contentId == null ? null : new KbDao(this).getById(contentId);
        if (item == null) {
            // Fiche retirée ou expirée entre-temps
            Toast.makeText(this, R.string.article_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        afficher(item);
        new ReadHistory(this, new SessionManager(this).getUserPid()).markViewed(item.contentId);
    }

    private void afficher(KbItem item) {
        // Le titre de l'en-tête reprend le sujet ; le grand titre est dans la page
        String topic = LabelUtils.label(this, R.array.kb_topics, AppConstants.KB_TOPICS, item.topic);
        ((TextView) findViewById(R.id.tvHeaderTitle)).setText(topic);
        findViewById(R.id.tvHeaderSubtitle).setVisibility(View.GONE);

        findViewById(R.id.bannerFictional).setVisibility(item.fictional ? View.VISIBLE : View.GONE);

        ((TextView) findViewById(R.id.tvTopic)).setText(topic);

        TextView tvEvidence = findViewById(R.id.tvEvidence);
        if (item.evidenceLevel == null) {
            tvEvidence.setVisibility(View.GONE);
        } else {
            tvEvidence.setText(getString(R.string.library_evidence_fmt, LabelUtils.label(this,
                    R.array.evidence_levels, AppConstants.EVIDENCE_LEVELS, item.evidenceLevel)));
        }

        ((TextView) findViewById(R.id.tvTitle)).setText(item.title);
        ((TextView) findViewById(R.id.tvMeta)).setText(getString(R.string.article_version_fmt,
                item.contentVersion, LabelUtils.label(this, R.array.content_languages,
                        AppConstants.CONTENT_LANGUAGES, item.language)));
        ((TextView) findViewById(R.id.tvContent)).setText(item.contentText);

        ((TextView) findViewById(R.id.tvSourceType)).setText(LabelUtils.label(this,
                R.array.source_types, AppConstants.SOURCE_TYPES, item.sourceType));
        ((TextView) findViewById(R.id.tvSourceRef)).setText(item.sourceReference);
        ((TextView) findViewById(R.id.tvReview)).setText(getString(R.string.article_review_fmt,
                LabelUtils.longDate(item.reviewDate), LabelUtils.longDate(item.expiryDate)));
    }
}
