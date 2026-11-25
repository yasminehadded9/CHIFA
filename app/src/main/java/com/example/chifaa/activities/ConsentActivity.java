package com.example.chifaa.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.chifaa.R;
import com.example.chifaa.models.SignupData;
import com.example.chifaa.utils.AppConstants;
import com.example.chifaa.utils.DateUtils;
import com.example.chifaa.utils.UiUtils;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Inscription · étape 2 : consentements séparés, UN écran par finalité.
 *   0 = Utilisation du service (obligatoire)
 *   1 = Données de santé       (facultatif)
 *   2 = Enquête                (facultatif)
 * L'heure d'acceptation (UTC) est mémorisée ; l'écriture en base se fait
 * à la fin de l'inscription, avec la version du texte et le mode "In-app".
 */
public class ConsentActivity extends AppCompatActivity {

    private static final int NB_CONSENTS = 3;

    // Contenu de chaque écran (même index que la description ci-dessus)
    private static final int[] TITLES = {
            R.string.consent_service_title, R.string.consent_health_title, R.string.consent_survey_title};
    private static final int[] TEXTS = {
            R.string.consent_service_text, R.string.consent_health_text, R.string.consent_survey_text};
    private static final int[] DATA = {
            R.string.consent_service_data, R.string.consent_health_data, R.string.consent_survey_data};
    private static final int[] ICONS = {
            R.drawable.ic_shield, R.drawable.ic_health, R.drawable.ic_poll};

    private SignupData data;
    private int index = 0;

    private ImageView ivIcon;
    private TextView tvBadge, tvTitle, tvText, tvData, tvVersion, tvError, tvSubtitle;
    private MaterialCheckBox cbRead;
    private ScrollView scroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consent);
        UiUtils.setupHeader(this, 2, R.string.consent_title, R.string.consent_title);

        data = (SignupData) getIntent().getSerializableExtra(SignupData.EXTRA);
        if (data == null) { // sécurité : on ne doit pas arriver ici sans passer par l'âge
            finish();
            return;
        }

        ivIcon = findViewById(R.id.ivConsentIcon);
        tvBadge = findViewById(R.id.tvConsentBadge);
        tvTitle = findViewById(R.id.tvConsentTitle);
        tvText = findViewById(R.id.tvConsentText);
        tvData = findViewById(R.id.tvConsentData);
        tvVersion = findViewById(R.id.tvConsentVersion);
        tvError = findViewById(R.id.tvConsentError);
        tvSubtitle = findViewById(R.id.tvHeaderSubtitle);
        cbRead = findViewById(R.id.cbRead);
        scroll = findViewById(R.id.scrollConsent);

        cbRead.setOnCheckedChangeListener((button, checked) -> {
            if (checked) tvError.setVisibility(View.GONE);
        });

        // Retour : écran de consentement précédent, ou écran de l'âge
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (index > 0) {
                    index--;
                    afficherConsentement();
                } else {
                    finish();
                }
            }
        });

        afficherConsentement();
    }

    /** Remplit l'écran avec le consentement numéro "index". */
    private void afficherConsentement() {
        boolean required = (index == 0);

        tvSubtitle.setText(getString(R.string.consent_subtitle_fmt, index + 1, NB_CONSENTS));
        ivIcon.setImageResource(ICONS[index]);
        tvTitle.setText(TITLES[index]);
        tvText.setText(TEXTS[index]);
        tvData.setText(DATA[index]);
        tvVersion.setText(getString(R.string.consent_version_fmt, AppConstants.CONSENT_TEXT_VERSION));

        tvBadge.setText(required ? R.string.consent_required : R.string.consent_optional);
        tvBadge.setBackgroundResource(required ? R.drawable.bg_badge_required : R.drawable.bg_badge_optional);
        tvBadge.setTextColor(ContextCompat.getColor(this,
                required ? R.color.brand_raspberry : R.color.brand_teal));

        // Nouvel écran : case décochée, pas d'erreur, retour en haut
        cbRead.setChecked(false);
        tvError.setVisibility(View.GONE);
        scroll.scrollTo(0, 0);
    }

    /** Bouton "J'accepte". */
    public void accepter(View v) {
        if (!cbRead.isChecked()) {
            tvError.setVisibility(View.VISIBLE);
            UiUtils.shake(cbRead);
            return;
        }
        String now = DateUtils.nowUtc();
        if (index == 0) data.serviceConsentAt = now;
        else if (index == 1) data.healthConsentAt = now;
        else data.surveyConsentAt = now;
        suivant();
    }

    /** Bouton "Je refuse". */
    public void refuser(View v) {
        if (index == 0) {
            // Le consentement "service" est indispensable
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.consent_service_refused_title)
                    .setMessage(R.string.consent_service_refused_message)
                    .setPositiveButton(R.string.consent_reread, null)
                    .setNegativeButton(R.string.consent_quit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            retourAccueil();
                        }
                    })
                    .show();
            return;
        }
        // Consentement facultatif refusé : rien n'est enregistré
        if (index == 1) data.healthConsentAt = null;
        else data.surveyConsentAt = null;
        suivant();
    }

    private void suivant() {
        if (index < NB_CONSENTS - 1) {
            index++;
            afficherConsentement();
        } else {
            Intent intent = new Intent(this, SignupProfileActivity.class);
            intent.putExtra(SignupData.EXTRA, data);
            startActivity(intent);
        }
    }

    private void retourAccueil() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
