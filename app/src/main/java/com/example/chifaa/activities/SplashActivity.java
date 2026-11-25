package com.example.chifaa.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chifaa.R;
import com.example.chifaa.database.DatabaseHelper;
import com.example.chifaa.utils.SessionManager;

/**
 * Écran de démarrage :
 *  1. affiche le logo avec une animation ;
 *  2. crée/ouvre la base SQLite (les 21 tables sont créées au tout premier lancement) ;
 *  3. redirige vers l'espace connecté, l'onboarding (première fois) ou l'accueil.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "CHIFAA";
    private static final long SPLASH_DELAY_MS = 1800;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable goNext = new Runnable() {
        @Override
        public void run() {
            ouvrirEcranSuivant();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Version affichée en bas de l'écran
        TextView tvVersion = findViewById(R.id.tvVersion);
        tvVersion.setText(getString(R.string.version_label, lireVersion()));

        // Animations
        View logo = findViewById(R.id.logoContainer);
        Animation logoAnim = AnimationUtils.loadAnimation(this, R.anim.splash_logo);
        logo.startAnimation(logoAnim);

        // Une animation par vue (une même instance ne doit pas être partagée)
        Animation nameAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
        findViewById(R.id.tvAppName).startAnimation(nameAnim);

        Animation taglineAnim = AnimationUtils.loadAnimation(this, R.anim.fade_in_up);
        taglineAnim.setStartOffset(500);
        findViewById(R.id.tvTagline).startAnimation(taglineAnim);

        // Création / ouverture de la base de données
        initialiserBase();

        handler.postDelayed(goNext, SPLASH_DELAY_MS);
    }

    /** Lit la version de l'application (versionName du build.gradle). */
    private String lireVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0";
        }
    }

    private void initialiserBase() {
        try {
            DatabaseHelper db = DatabaseHelper.getInstance(this);
            int nbTables = db.countTables();
            Log.i(TAG, "Base SQLite prête : " + nbTables + " tables");
        } catch (Exception e) {
            Log.e(TAG, "Erreur à la création de la base", e);
        }
    }

    private void ouvrirEcranSuivant() {
        SessionManager session = new SessionManager(this);
        Intent intent;
        if (session.isLoggedIn()) {
            intent = new Intent(this, HomeActivity.class);        // déjà connectée
        } else if (session.isOnboardingDone()) {
            intent = new Intent(this, WelcomeActivity.class);
        } else {
            intent = new Intent(this, OnboardingActivity.class);
        }
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish(); // on ne revient pas sur le splash avec le bouton retour
    }

    @Override
    protected void onDestroy() {
        // Si l'utilisatrice quitte pendant le splash, on annule la redirection
        handler.removeCallbacks(goNext);
        super.onDestroy();
    }
}
