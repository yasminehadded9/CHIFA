package com.example.chifaa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chifaa.R;

/**
 * Écran d'accueil : point de départ vers l'inscription, la connexion
 * ou l'espace équipe (back-office, étape 9).
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    /** Bouton "Créer mon compte" -> vérification de l'âge. */
    public void creerCompte(View v) {
        startActivity(new Intent(this, SignupAgeActivity.class));
    }

    /** Bouton "J'ai déjà un compte" -> numéro + code SMS. */
    public void seConnecter(View v) {
        Intent intent = new Intent(this, PhoneActivity.class);
        intent.putExtra(PhoneActivity.EXTRA_MODE, PhoneActivity.MODE_LOGIN);
        startActivity(intent);
    }

    /** Bouton "Accès équipe CHIFAA" (back-office). */
    public void espaceEquipe(View v) {
        Toast.makeText(this, R.string.coming_soon_staff, Toast.LENGTH_SHORT).show();
    }
}
