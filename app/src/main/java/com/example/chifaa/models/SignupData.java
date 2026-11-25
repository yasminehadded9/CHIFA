package com.example.chifaa.models;

import java.io.Serializable;

/**
 * Données collectées pendant l'inscription, transmises d'écran en écran
 * avec intent.putExtra(EXTRA, data) puis getSerializableExtra(EXTRA).
 * Rien n'est écrit en base avant la validation du code SMS.
 */
public class SignupData implements Serializable {

    public static final String EXTRA = "signup_data";

    // Étape 1 : âge
    public boolean ageConfirmed18plus;

    // Étape 2 : consentements (date ISO 8601 UTC du "J'accepte", null si refusé)
    public String serviceConsentAt;
    public String healthConsentAt;
    public String surveyConsentAt;

    // Étape 3 : profil (codes stockés en base, voir AppConstants)
    public String ageBand;
    public String governorate;
    public String preferredLanguage;

    // Étape 4 : identité (sera chiffrée)
    public String phone;        // 8 chiffres, normalisé
    public String displayName;  // facultatif

    public boolean hasHealthConsent() { return healthConsentAt != null; }

    public boolean hasSurveyConsent() { return surveyConsentAt != null; }
}
