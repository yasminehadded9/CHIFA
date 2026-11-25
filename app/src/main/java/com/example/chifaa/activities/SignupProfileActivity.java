package com.example.chifaa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chifaa.R;
import com.example.chifaa.models.SignupData;
import com.example.chifaa.utils.AppConstants;
import com.example.chifaa.utils.UiUtils;
import com.example.chifaa.utils.ValidationUtils;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Inscription · étape 3 : profil (tranche d'âge, gouvernorat, langue).
 * Les trois champs sont obligatoires (NOT NULL dans la table users).
 * Le libellé choisi (français) est converti en code de base via AppConstants.
 */
public class SignupProfileActivity extends AppCompatActivity {

    private SignupData data;

    private TextInputLayout tilAgeBand, tilGovernorate, tilLanguage;

    // Position choisie dans chaque liste (-1 = rien choisi)
    private int ageIndex = -1;
    private int governorateIndex = -1;
    private int languageIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_profile);
        UiUtils.setupHeader(this, 3, R.string.profile_title, R.string.profile_subtitle);

        data = (SignupData) getIntent().getSerializableExtra(SignupData.EXTRA);
        if (data == null) {
            finish();
            return;
        }

        tilAgeBand = findViewById(R.id.tilAgeBand);
        tilGovernorate = findViewById(R.id.tilGovernorate);
        tilLanguage = findViewById(R.id.tilLanguage);

        // Mémorise la position choisie et efface l'erreur éventuelle
        MaterialAutoCompleteTextView acAge = findViewById(R.id.acAgeBand);
        acAge.setOnItemClickListener((parent, view, position, id) -> {
            ageIndex = position;
            tilAgeBand.setError(null);
        });

        MaterialAutoCompleteTextView acGov = findViewById(R.id.acGovernorate);
        acGov.setOnItemClickListener((parent, view, position, id) -> {
            governorateIndex = position;
            tilGovernorate.setError(null);
        });

        MaterialAutoCompleteTextView acLang = findViewById(R.id.acLanguage);
        acLang.setOnItemClickListener((parent, view, position, id) -> {
            languageIndex = position;
            tilLanguage.setError(null);
        });
    }

    /** Bouton "Continuer" : on vérifie TOUS les champs et on signale chaque erreur. */
    public void continuer(View v) {
        TextInputLayout firstError = null;

        if (!ValidationUtils.isValidChoice(ageIndex, AppConstants.AGE_BANDS)) {
            tilAgeBand.setError(getString(R.string.error_age_band));
            firstError = tilAgeBand;
        }
        if (!ValidationUtils.isValidChoice(governorateIndex, AppConstants.GOVERNORATE_CODES)) {
            tilGovernorate.setError(getString(R.string.error_governorate));
            if (firstError == null) firstError = tilGovernorate;
        }
        if (!ValidationUtils.isValidChoice(languageIndex, AppConstants.LANGUAGES)) {
            tilLanguage.setError(getString(R.string.error_language));
            if (firstError == null) firstError = tilLanguage;
        }

        if (firstError != null) {
            UiUtils.shake(firstError); // on attire l'attention sur le premier champ en erreur
            return;
        }

        data.ageBand = AppConstants.AGE_BANDS[ageIndex];
        data.governorate = AppConstants.GOVERNORATE_CODES[governorateIndex];
        data.preferredLanguage = AppConstants.LANGUAGES[languageIndex];

        Intent intent = new Intent(this, PhoneActivity.class);
        intent.putExtra(PhoneActivity.EXTRA_MODE, PhoneActivity.MODE_SIGNUP);
        intent.putExtra(SignupData.EXTRA, data);
        startActivity(intent);
    }
}
