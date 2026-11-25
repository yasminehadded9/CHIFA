package com.example.chifaa.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chifaa.R;
import com.example.chifaa.database.AccountDao;
import com.example.chifaa.models.UserProfile;
import com.example.chifaa.utils.AppConstants;
import com.example.chifaa.utils.LocaleUtils;
import com.example.chifaa.utils.SessionManager;
import com.example.chifaa.utils.UiUtils;
import com.example.chifaa.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Mon profil (module M2) :
 *  - modifier tranche d'âge, gouvernorat, langue d'échange et surnom (à tout moment) ;
 *  - changer la langue de l'interface (français / arabe, droite à gauche) ;
 *  - se déconnecter.
 * Le bouton "Enregistrer" ne s'active que s'il y a une modification.
 */
public class ProfileActivity extends AppCompatActivity {

    private AccountDao dao;
    private SessionManager session;
    private UserProfile profile;

    private TextInputLayout tilDisplayName, tilAgeBand, tilGovernorate, tilLanguage;
    private TextInputEditText etDisplayName;
    private MaterialButton btnSave;

    // Positions choisies dans les listes déroulantes
    private int ageIndex, governorateIndex, languageIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        UiUtils.setupHeader(this, 0, R.string.profile_screen_title, R.string.profile_screen_subtitle);

        dao = new AccountDao(this);
        session = new SessionManager(this);
        profile = dao.getProfile(session.getUserPid());
        if (profile == null) {
            finish();
            return;
        }

        tilDisplayName = findViewById(R.id.tilDisplayName);
        tilAgeBand = findViewById(R.id.tilAgeBand);
        tilGovernorate = findViewById(R.id.tilGovernorate);
        tilLanguage = findViewById(R.id.tilLanguage);
        etDisplayName = findViewById(R.id.etDisplayName);
        btnSave = findViewById(R.id.btnSave);

        afficherIdentite();
        remplirFormulaire();
        brancherEcouteurs();
        configurerLangueInterface();

        // Retour avec des modifications non enregistrées -> confirmation
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (aDesModifications()) {
                    confirmerAbandon();
                } else {
                    finish();
                }
            }
        });
    }

    // =================================================================== Affichage

    private void afficherIdentite() {
        TextView tvName = findViewById(R.id.tvProfileName);
        TextView tvInitial = findViewById(R.id.tvProfileInitial);
        View ribbon = findViewById(R.id.ivProfileRibbon);

        if (profile.displayName == null) {
            tvName.setText(R.string.profile_no_name);
            tvInitial.setVisibility(View.GONE);
            ribbon.setVisibility(View.VISIBLE);
        } else {
            tvName.setText(profile.displayName);
            tvInitial.setText(profile.displayName.substring(0, 1).toUpperCase());
            tvInitial.setVisibility(View.VISIBLE);
            ribbon.setVisibility(View.GONE);
        }

        ((TextView) findViewById(R.id.tvMemberSince)).setText(
                getString(R.string.profile_member_since, formaterDate(profile.registrationDate)));
        ((TextView) findViewById(R.id.tvPid)).setText(profile.userPid);
    }

    /** "2026-11-25" -> "25 novembre 2026" (ou en arabe selon l'interface). */
    private String formaterDate(String isoDate) {
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDate);
            return DateFormat.getDateInstance(DateFormat.LONG).format(date);
        } catch (Exception e) {
            return isoDate;
        }
    }

    /** Pré-remplit le formulaire avec les valeurs enregistrées (code -> libellé). */
    private void remplirFormulaire() {
        ageIndex = AppConstants.indexOf(AppConstants.AGE_BANDS, profile.ageBand);
        governorateIndex = AppConstants.indexOf(AppConstants.GOVERNORATE_CODES, profile.governorate);
        languageIndex = AppConstants.indexOf(AppConstants.LANGUAGES, profile.preferredLanguage);

        etDisplayName.setText(profile.displayName == null ? "" : profile.displayName);
        choisir(R.id.acAgeBand, R.array.age_bands, ageIndex);
        choisir(R.id.acGovernorate, R.array.governorates, governorateIndex);
        choisir(R.id.acLanguage, R.array.languages, languageIndex);
    }

    private void choisir(int viewId, int arrayRes, int index) {
        MaterialAutoCompleteTextView ac = findViewById(viewId);
        String[] labels = getResources().getStringArray(arrayRes);
        if (index >= 0 && index < labels.length) {
            ac.setText(labels[index], false); // false = ne pas filtrer la liste
        }
    }

    // ================================================================== Écouteurs

    private void brancherEcouteurs() {
        etDisplayName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilDisplayName.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                majBoutonEnregistrer();
            }
        });

        ((MaterialAutoCompleteTextView) findViewById(R.id.acAgeBand)).setOnItemClickListener(
                (parent, view, position, id) -> {
                    ageIndex = position;
                    tilAgeBand.setError(null);
                    majBoutonEnregistrer();
                });
        ((MaterialAutoCompleteTextView) findViewById(R.id.acGovernorate)).setOnItemClickListener(
                (parent, view, position, id) -> {
                    governorateIndex = position;
                    tilGovernorate.setError(null);
                    majBoutonEnregistrer();
                });
        ((MaterialAutoCompleteTextView) findViewById(R.id.acLanguage)).setOnItemClickListener(
                (parent, view, position, id) -> {
                    languageIndex = position;
                    tilLanguage.setError(null);
                    majBoutonEnregistrer();
                });
    }

    private String surnomSaisi() {
        String raw = etDisplayName.getText() == null ? "" : etDisplayName.getText().toString();
        return raw.trim().replaceAll("\\s+", " ");
    }

    /** Compare le formulaire aux valeurs enregistrées. */
    private boolean aDesModifications() {
        String savedName = profile.displayName == null ? "" : profile.displayName;
        return !surnomSaisi().equals(savedName)
                || ageIndex != AppConstants.indexOf(AppConstants.AGE_BANDS, profile.ageBand)
                || governorateIndex != AppConstants.indexOf(AppConstants.GOVERNORATE_CODES, profile.governorate)
                || languageIndex != AppConstants.indexOf(AppConstants.LANGUAGES, profile.preferredLanguage);
    }

    private void majBoutonEnregistrer() {
        btnSave.setEnabled(aDesModifications());
    }

    // ================================================================ Enregistrer

    /** Bouton "Enregistrer les modifications". */
    public void enregistrer(View v) {
        // 1. Contrôle de saisie (mêmes règles qu'à l'inscription)
        TextInputLayout firstError = null;

        String nameMessage = messageSurnom(surnomSaisi());
        if (nameMessage != null) {
            tilDisplayName.setError(nameMessage);
            firstError = tilDisplayName;
        }
        if (!ValidationUtils.isValidChoice(ageIndex, AppConstants.AGE_BANDS)) {
            tilAgeBand.setError(getString(R.string.error_age_band));
            if (firstError == null) firstError = tilAgeBand;
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
            UiUtils.shake(firstError);
            firstError.requestFocus();
            return;
        }

        // 2. Enregistrement
        String name = surnomSaisi();
        try {
            dao.updateProfile(profile.userPid,
                    AppConstants.AGE_BANDS[ageIndex],
                    AppConstants.GOVERNORATE_CODES[governorateIndex],
                    AppConstants.LANGUAGES[languageIndex],
                    name.isEmpty() ? null : name);
        } catch (Exception e) {
            Log.e("CHIFAA", "Mise à jour du profil impossible", e);
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_LONG).show();
            return;
        }

        // 3. On relit le profil et on rafraîchit l'écran
        profile = dao.getProfile(profile.userPid);
        afficherIdentite();
        majBoutonEnregistrer();
        UiUtils.hideKeyboard(this);
        Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show();
    }

    private String messageSurnom(String name) {
        switch (ValidationUtils.validateDisplayName(name)) {
            case TOO_SHORT:
                return getString(R.string.name_error_short);
            case TOO_LONG:
                return getString(R.string.name_error_long);
            case INVALID_CHARS:
                return getString(R.string.name_error_chars);
            default:
                return null;
        }
    }

    private void confirmerAbandon() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.profile_unsaved_title)
                .setMessage(R.string.profile_unsaved_message)
                .setNegativeButton(R.string.profile_discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setPositiveButton(R.string.profile_keep_editing, null)
                .show();
    }

    // ===================================================== Langue de l'interface

    private void configurerLangueInterface() {
        MaterialButtonToggleGroup toggle = findViewById(R.id.toggleAppLanguage);
        toggle.check(LocaleUtils.ARABIC.equals(LocaleUtils.current()) ? R.id.btnLangAr : R.id.btnLangFr);

        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            String wanted = (checkedId == R.id.btnLangAr) ? LocaleUtils.ARABIC : LocaleUtils.FRENCH;
            if (!wanted.equals(LocaleUtils.current())) {
                // L'activité est recréée automatiquement dans la nouvelle langue
                LocaleUtils.apply(wanted);
            }
        });
    }

    // ================================================================ Navigation

    public void ouvrirMesDonnees(View v) {
        Toast.makeText(this, R.string.soon_my_data, Toast.LENGTH_SHORT).show();
    }

    public void seDeconnecter(View v) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.home_logout_confirm_title)
                .setMessage(R.string.home_logout_confirm_message)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.home_logout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        session.logout();
                        Intent intent = new Intent(ProfileActivity.this, WelcomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }
}
