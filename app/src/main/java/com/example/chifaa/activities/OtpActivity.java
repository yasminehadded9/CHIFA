package com.example.chifaa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chifaa.R;
import com.example.chifaa.database.AccountDao;
import com.example.chifaa.models.SignupData;
import com.example.chifaa.utils.SessionManager;
import com.example.chifaa.utils.UiUtils;
import com.example.chifaa.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.security.SecureRandom;
import java.util.Locale;

/**
 * Vérification par code SMS SIMULÉ (aucun SMS réel n'est envoyé : le code
 * s'affiche dans un encadré "SMS simulé").
 * Règles : 6 chiffres, valable 5 minutes, 3 essais, renvoi possible après 30 s.
 * Si le code est bon :
 *  - inscription -> création du compte en base, puis ouverture de la session ;
 *  - connexion   -> ouverture de la session.
 */
public class OtpActivity extends AppCompatActivity {

    public static final String EXTRA_PHONE = "phone";

    private static final long CODE_VALIDITY_MS = 5 * 60 * 1000;
    private static final long RESEND_DELAY_MS = 30 * 1000;
    private static final int MAX_ATTEMPTS = 3;

    private String mode;
    private String phone;
    private SignupData data;

    private String expectedCode;
    private long codeCreatedAt;
    private int attemptsLeft;

    private TextInputLayout tilOtp;
    private TextInputEditText etOtp;
    private TextView tvSmsBody;
    private MaterialButton btnResend, btnVerify;
    private CountDownTimer resendTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        mode = getIntent().getStringExtra(PhoneActivity.EXTRA_MODE);
        phone = getIntent().getStringExtra(EXTRA_PHONE);
        data = (SignupData) getIntent().getSerializableExtra(SignupData.EXTRA);
        if (phone == null || (isSignup() && data == null)) {
            finish();
            return;
        }

        UiUtils.setupHeader(this, isSignup() ? 5 : 0, R.string.otp_title, R.string.otp_title);
        ((TextView) findViewById(R.id.tvHeaderSubtitle)).setText(
                getString(R.string.otp_subtitle_fmt, ValidationUtils.formatPhone(phone)));

        tilOtp = findViewById(R.id.tilOtp);
        etOtp = findViewById(R.id.etOtp);
        tvSmsBody = findViewById(R.id.tvSmsBody);
        btnResend = findViewById(R.id.btnResend);
        btnVerify = findViewById(R.id.btnVerify);

        // Efface l'erreur à la saisie et vérifie automatiquement au 6e chiffre
        etOtp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilOtp.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == ValidationUtils.OTP_LENGTH && btnVerify.isEnabled()) {
                    verifier(etOtp);
                }
            }
        });
        etOtp.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    verifier(v);
                    return true;
                }
                return false;
            }
        });

        genererCode();
        etOtp.requestFocus();
    }

    private boolean isSignup() {
        return PhoneActivity.MODE_SIGNUP.equals(mode);
    }

    /** Crée un nouveau code et "l'envoie" (affichage dans l'encadré SMS simulé). */
    private void genererCode() {
        int value = new SecureRandom().nextInt(1_000_000);
        expectedCode = String.format(Locale.US, "%06d", value);
        codeCreatedAt = SystemClock.elapsedRealtime();
        attemptsLeft = MAX_ATTEMPTS;

        tvSmsBody.setText(getString(R.string.otp_sms_body_fmt, expectedCode));
        findViewById(R.id.smsCard).startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.fade_in_up));

        btnVerify.setEnabled(true);
        demarrerCompteARebours();
    }

    /** Bouton "Renvoyer le code" désactivé pendant 30 secondes. */
    private void demarrerCompteARebours() {
        if (resendTimer != null) resendTimer.cancel();
        btnResend.setEnabled(false);
        resendTimer = new CountDownTimer(RESEND_DELAY_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) Math.ceil(millisUntilFinished / 1000.0);
                btnResend.setText(getString(R.string.otp_resend_wait, seconds));
            }

            @Override
            public void onFinish() {
                btnResend.setText(R.string.otp_resend);
                btnResend.setEnabled(true);
            }
        }.start();
    }

    public void renvoyerCode(View v) {
        etOtp.setText("");
        tilOtp.setError(null);
        genererCode();
        Toast.makeText(this, R.string.otp_resent, Toast.LENGTH_SHORT).show();
    }

    /** Bouton "Vérifier" (appelé aussi automatiquement au 6e chiffre). */
    public void verifier(View v) {
        String code = etOtp.getText() == null ? "" : etOtp.getText().toString().trim();

        // 1. Format
        ValidationUtils.OtpResult format = ValidationUtils.validateOtpFormat(code);
        if (format == ValidationUtils.OtpResult.EMPTY) {
            UiUtils.showFieldError(tilOtp, getString(R.string.otp_error_empty));
            return;
        }
        if (format == ValidationUtils.OtpResult.WRONG_FORMAT) {
            UiUtils.showFieldError(tilOtp, getString(R.string.otp_error_format));
            return;
        }

        // 2. Expiration
        if (SystemClock.elapsedRealtime() - codeCreatedAt > CODE_VALIDITY_MS) {
            bloquer(getString(R.string.otp_error_expired));
            return;
        }

        // 3. Comparaison
        if (!code.equals(expectedCode)) {
            attemptsLeft--;
            if (attemptsLeft <= 0) {
                bloquer(getString(R.string.otp_error_locked));
            } else {
                UiUtils.showFieldError(tilOtp, getString(R.string.otp_error_wrong, attemptsLeft));
                etOtp.selectAll();
            }
            return;
        }

        // 4. Code correct
        btnVerify.setEnabled(false);
        UiUtils.hideKeyboard(this);
        terminer();
    }

    /** Code expiré ou trop d'essais : il faut redemander un code. */
    private void bloquer(String message) {
        UiUtils.showFieldError(tilOtp, message);
        btnVerify.setEnabled(false);
        if (resendTimer != null) resendTimer.cancel();
        btnResend.setText(R.string.otp_resend);
        btnResend.setEnabled(true);
    }

    private void terminer() {
        AccountDao dao = new AccountDao(this);
        String userPid;
        try {
            if (isSignup()) {
                userPid = dao.createAccount(data);
            } else {
                userPid = dao.findUserPidByPhone(phone);
                dao.touchLastActive(userPid);
            }
        } catch (Exception e) {
            Log.e("CHIFAA", "Création / connexion impossible", e);
            Toast.makeText(this, R.string.error_generic, Toast.LENGTH_LONG).show();
            btnVerify.setEnabled(true);
            return;
        }

        new SessionManager(this).login(userPid);

        // On vide la pile : le bouton retour ne ramène plus à l'inscription
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(HomeActivity.EXTRA_NEW_ACCOUNT, isSignup());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (resendTimer != null) resendTimer.cancel();
        super.onDestroy();
    }
}
