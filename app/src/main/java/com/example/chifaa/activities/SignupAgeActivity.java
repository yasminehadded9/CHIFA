package com.example.chifaa.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chifaa.R;
import com.example.chifaa.models.SignupData;
import com.example.chifaa.utils.UiUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Inscription · étape 1 : vérification de l'âge (18 ans ou plus).
 * Une réponse "moins de 18 ans" bloque l'inscription et affiche un message d'orientation.
 */
public class SignupAgeActivity extends AppCompatActivity {

    private MaterialCardView cardAdult;
    private MaterialCardView cardMinor;
    private TextView tvError;

    /** null = aucune réponse, TRUE = majeure, FALSE = mineure. */
    private Boolean isAdult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_age);
        UiUtils.setupHeader(this, 1, R.string.age_title, R.string.age_subtitle);

        cardAdult = findViewById(R.id.cardAdult);
        cardMinor = findViewById(R.id.cardMinor);
        tvError = findViewById(R.id.tvAgeError);
    }

    public void choisirMajeure(View v) {
        selectionner(true);
    }

    public void choisirMineure(View v) {
        selectionner(false);
    }

    private void selectionner(boolean adult) {
        isAdult = adult;
        cardAdult.setChecked(adult);
        cardMinor.setChecked(!adult);
        tvError.setVisibility(View.GONE);
    }

    /** Bouton "Continuer". */
    public void continuer(View v) {
        // Contrôle de saisie : une réponse est obligatoire
        if (isAdult == null) {
            tvError.setVisibility(View.VISIBLE);
            UiUtils.shake(findViewById(R.id.choicesContainer));
            return;
        }

        if (!isAdult) {
            afficherBlocage();
            return;
        }

        SignupData data = new SignupData();
        data.ageConfirmed18plus = true;

        Intent intent = new Intent(this, ConsentActivity.class);
        intent.putExtra(SignupData.EXTRA, data);
        startActivity(intent);
    }

    /** Refus : on bloque et on oriente, sans rien enregistrer. */
    private void afficherBlocage() {
        new MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.ic_info)
                .setTitle(R.string.age_blocked_title)
                .setMessage(R.string.age_blocked_message)
                .setCancelable(false)
                .setPositiveButton(R.string.age_blocked_action, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(); // retour à l'accueil
                    }
                })
                .show();
    }
}
