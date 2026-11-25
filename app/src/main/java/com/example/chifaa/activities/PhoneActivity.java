package com.example.chifaa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chifaa.R;
import com.example.chifaa.database.AccountDao;
import com.example.chifaa.models.SignupData;
import com.example.chifaa.utils.UiUtils;
import com.example.chifaa.utils.ValidationUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Saisie du numéro de téléphone, en deux modes :
 *  - MODE_SIGNUP : inscription, étape 4 (numéro + surnom facultatif) ;
 *  - MODE_LOGIN  : connexion (numéro seul).
 */
public class PhoneActivity extends AppCompatActivity {

    public static final String EXTRA_MODE = "mode";
    public static final String MODE_SIGNUP = "signup";
    public static final String MODE_LOGIN = "login";

    private String mode;
    private SignupData data;

    private TextInputLayout tilPhone, tilDisplayName;
    private TextInputEditText etPhone, etDisplayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = MODE_LOGIN;

        tilPhone = findViewById(R.id.tilPhone);
        tilDisplayName = findViewById(R.id.tilDisplayName);
        etPhone = findViewById(R.id.etPhone);
        etDisplayName = findViewById(R.id.etDisplayName);

        if (isSignup()) {
            data = (SignupData) getIntent().getSerializableExtra(SignupData.EXTRA);
            if (data == null) {
                finish();
                return;
            }
            UiUtils.setupHeader(this, 4, R.string.phone_title_signup, R.string.phone_subtitle_signup);
        } else {
            UiUtils.setupHeader(this, 0, R.string.phone_title_login, R.string.phone_subtitle_login);
            tilDisplayName.setVisibility(View.GONE);
            findViewById(R.id.rowNoAccount).setVisibility(View.VISIBLE);
        }

        // Les erreurs disparaissent dès que l'utilisatrice corrige
        UiUtils.clearErrorOnInput(tilPhone);
        UiUtils.clearErrorOnInput(tilDisplayName);

        // Touche "OK" du clavier = bouton "Recevoir le code"
        TextView.OnEditorActionListener submitOnDone = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    envoyerCode(v);
                    return true;
                }
                return false;
            }
        };
        etPhone.setOnEditorActionListener(submitOnDone);
        etDisplayName.setOnEditorActionListener(submitOnDone);

        etPhone.requestFocus();
    }

    private boolean isSignup() {
        return MODE_SIGNUP.equals(mode);
    }

    /** Bouton "Recevoir le code". */
    public void envoyerCode(View v) {
        String rawPhone = etPhone.getText() == null ? "" : etPhone.getText().toString();
        String rawName = etDisplayName.getText() == null ? "" : etDisplayName.getText().toString();

        // 1. Contrôle du format du numéro
        String phoneError = messagePhone(rawPhone);
        if (phoneError != null) {
            UiUtils.showFieldError(tilPhone, phoneError);
            return;
        }
        String phone = ValidationUtils.normalizePhone(rawPhone);

        // 2. Contrôle du surnom (facultatif)
        String displayName = null;
        if (isSignup()) {
            String nameError = messageName(rawName);
            if (nameError != null) {
                UiUtils.showFieldError(tilDisplayName, nameError);
                return;
            }
            displayName = rawName.trim().replaceAll("\\s+", " ");
            if (displayName.isEmpty()) displayName = null;
        }

        // 3. Contrôle en base : le compte doit exister (connexion) ou non (inscription)
        AccountDao dao = new AccountDao(this);
        boolean exists = dao.phoneExists(phone);
        if (isSignup() && exists) {
            UiUtils.showFieldError(tilPhone, getString(R.string.phone_error_exists));
            return;
        }
        if (!isSignup() && !exists) {
            UiUtils.showFieldError(tilPhone, getString(R.string.phone_error_unknown));
            return;
        }

        UiUtils.hideKeyboard(this);

        Intent intent = new Intent(this, OtpActivity.class);
        intent.putExtra(EXTRA_MODE, mode);
        intent.putExtra(OtpActivity.EXTRA_PHONE, phone);
        if (isSignup()) {
            data.phone = phone;
            data.displayName = displayName;
            intent.putExtra(SignupData.EXTRA, data);
        }
        startActivity(intent);
    }

    /** Bouton "Créer un compte" (mode connexion). */
    public void allerInscription(View v) {
        startActivity(new Intent(this, SignupAgeActivity.class));
        finish();
    }

    /** Traduit le résultat de la validation en message clair (ou null si valide). */
    private String messagePhone(String raw) {
        switch (ValidationUtils.validatePhone(raw)) {
            case EMPTY:
                return getString(R.string.phone_error_empty);
            case INVALID_CHARS:
                return getString(R.string.phone_error_chars);
            case WRONG_LENGTH:
                return getString(R.string.phone_error_length,
                        ValidationUtils.normalizePhone(raw).length());
            case BAD_PREFIX:
                return getString(R.string.phone_error_prefix);
            default:
                return null;
        }
    }

    private String messageName(String raw) {
        switch (ValidationUtils.validateDisplayName(raw)) {
            case TOO_SHORT:
                return getString(R.string.name_error_short);
            case TOO_LONG:
                return getString(R.string.name_error_long);
            case INVALID_CHARS:
                return getString(R.string.name_error_chars);
            default:
                return null; // OK ou vide (facultatif)
        }
    }
}
