package com.example.chifaa.utils;

/**
 * Listes fermées du catalogue de données (valeurs stockées en base).
 *
 * Ces tableaux servent à DEUX endroits :
 *  1. dans DbSchema, pour générer les contraintes CHECK (... IN (...)) ;
 *  2. dans les écrans, pour convertir le choix de l'utilisatrice en code.
 * Ainsi, l'interface et la base ne peuvent jamais être désynchronisées.
 *
 * Les libellés affichés (en français) sont dans res/values/arrays.xml,
 * dans le même ordre (position i du libellé = position i du code).
 */
public final class AppConstants {

    private AppConstants() { }

    /** Position d'un code dans une liste fermée (-1 si absent). Sert à retrouver le libellé affiché. */
    public static int indexOf(String[] values, String code) {
        if (code == null) return -1;
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(code)) return i;
        }
        return -1;
    }

    // ---------------------------------------------------------------- Profil
    public static final String[] AGE_BANDS = {"18–29", "30–39", "40–49", "50–59", "60+"};

    /** Codes ISO 3166-2:TN, même ordre que R.array.governorates. */
    public static final String[] GOVERNORATE_CODES = {
            "TN-11", "TN-12", "TN-13", "TN-14", "TN-21", "TN-22", "TN-23", "TN-31",
            "TN-32", "TN-33", "TN-34", "TN-41", "TN-42", "TN-43", "TN-51", "TN-52",
            "TN-53", "TN-61", "TN-71", "TN-72", "TN-73", "TN-81", "TN-82", "TN-83"
    };

    public static final String[] LANGUAGES = {"Darija", "French", "MSA", "Mixed"};
    /**
     * Langues des fiches et des services. ⚠ MÊME ORDRE que R.array.content_languages
     * (Français, Arabe standard, Darija) : position i du libellé = position i du code.
     */
    public static final String[] CONTENT_LANGUAGES = {"French", "MSA", "Darija"};
    public static final String[] CHANNELS = {"App", "Web", "WhatsApp", "Voice"};

    // ---------------------------------------------------------- Consentements
    public static final String CONSENT_SERVICE = "Service use";
    public static final String CONSENT_HEALTH = "Health data";
    public static final String CONSENT_SURVEY = "Survey";

    public static final String[] CONSENT_PURPOSES = {
            CONSENT_SERVICE, CONSENT_HEALTH, "AI training reuse", "Research interview",
            CONSENT_SURVEY, "Usability test", "Recording"
    };
    public static final String STATUS_GIVEN = "Given";
    public static final String STATUS_WITHDRAWN = "Withdrawn";
    public static final String[] CONSENT_STATUSES = {STATUS_GIVEN, STATUS_WITHDRAWN, "Expired"};
    public static final String[] CAPTURE_METHODS = {"In-app", "Written form", "Recorded verbal"};

    /** Version actuelle des textes de consentement (format vX.Y). */
    public static final String CONSENT_TEXT_VERSION = "v1.0";

    // ------------------------------------------------------------------ Santé
    public static final String PREFER_NOT_TO_SAY = "Prefer not to say";

    public static final String[] CANCER_TYPES = {"Breast", "Cervical", "Other", PREFER_NOT_TO_SAY};

    /** 7 étapes du parcours (AT-007). À aligner sur le catalogue fourni par l'encadrant. */
    public static final String[] CARE_PHASES = {
            "Screening", "Diagnosis", "Pre-treatment", "Active treatment",
            "Post-treatment follow-up", "Remission", "Recurrence"
    };

    public static final String[] DIAGNOSIS_BANDS = {"<1 yr", "1–2 yrs", "3–5 yrs", ">5 yrs"};

    public static final String[] FACILITY_TYPES = {
            "Public hospital", "Private clinic", "Abroad", PREFER_NOT_TO_SAY
    };

    public static final String[] TREATMENT_TYPES = {
            "Surgery", "Reconstruction", "Chemotherapy", "Radiotherapy",
            "Hormone therapy", "Targeted therapy", "Immunotherapy", PREFER_NOT_TO_SAY
    };

    /** 11 effets secondaires (AT-073). À aligner sur le catalogue fourni par l'encadrant. */
    public static final String[] SIDE_EFFECTS = {
            "Fatigue", "Nausea", "Pain", "Hair loss", "Lymphedema", "Hot flashes",
            "Sleep problems", "Anxiety", "Low mood", "Skin changes", "Neuropathy"
    };

    // ------------------------------------------------------------ Compagnon
    public static final String[] SENDER_ROLES = {"User", "AI", "Human reviewer"};

    // -------------------------------------------------------------- Sécurité
    /** 7 origines d'alerte (AT-022). À aligner sur le catalogue fourni par l'encadrant. */
    public static final String[] TRIGGER_TYPES = {
            "Distress keyword", "Self-harm mention", "Medical emergency", "Dosage request",
            "Diagnosis request", "Human requested", "Reviewer flag"
    };
    public static final String[] SEVERITIES = {"Low", "Medium", "High", "Critical"};
    public static final String[] ACTIONS_TAKEN = {"Resource shared", "Human contacted", "Referral", "None needed"};
    public static final String[] EVENT_STATUSES = {"Open", "In progress", "Resolved", "Closed"};
    public static final String[] REFERRAL_OUTCOMES = {"Contacted", "Attended", "Declined", "Unknown", "N/A"};

    // --------------------------------------------------------------- Équipe
    public static final String[] STAFF_ROLES = {
            "Data steward", "Clinical reviewer", "Safety reviewer", "Content team", "Admin"
    };

    // ------------------------------------------------------------ Contenus
    /** 5 types de source (AT-044). À aligner sur le catalogue fourni par l'encadrant. */
    public static final String[] SOURCE_TYPES = {
            "Clinical guideline", "Peer-reviewed article", "Institutional website",
            "Expert consensus", "Patient association"
    };
    public static final String[] EVIDENCE_LEVELS = {"High", "Moderate", "Low", "Expert opinion"};

    /** Taxonomie des sujets (question Q-08 du catalogue : à confirmer avec l'encadrant). */
    public static final String TOPIC_FERTILITY = "Fertility";
    public static final String[] KB_TOPICS = {
            "Diagnosis", "Treatment", "Side effects", "Nutrition",
            "Emotional wellbeing", TOPIC_FERTILITY, "Daily life", "Rights & CNAM"
    };
    public static final String[] LOCAL_APPLICABILITY = {"Applies", "Needs adaptation", "Not applicable"};
    public static final String APPROVED = "Clinically approved";
    public static final String[] APPROVAL_STATUSES = {"Draft", APPROVED, "Retired"};

    // ------------------------------------------------------------- Annuaire
    /** 9 types de service (AT-032), conformes à la liste du module M6. */
    public static final String[] SERVICE_TYPES = {
            "Oncology", "Psychological", "Legal", "Fertility", "Financial",
            "Peer support", "Prostheses & wigs", "Transport", "CNAM assistance"
    };
    public static final String[] COSTS = {"Free", "Covered by CNAM", "Paid", "Unknown"};
    public static final String[] VERIFICATION_METHODS = {"Phone call", "Site visit", "Partner confirmation"};

    // ------------------------------------------------------------------ Jeu
    public static final String[] COMPLETION_STATUSES = {"Started", "Completed"};

    // -------------------------------------------------------------- Enquête
    public static final String[] UNMET_NEEDS = {
            "Information", "Emotional", "Financial", "Legal", "Fertility", "Practical", "Other"
    };
    public static final String[] SUPPORT_SOURCES = {
            "Family", "Peers", "Association", "Clinician", "Online", "CHIFAA"
    };

    // ---------------------------------------------- Demandes et journal
    public static final String[] RECORD_TYPES = {
            "Access request", "Rectification", "Deletion request",
            "Consent withdrawal", "Incident / breach"
    };
    public static final String[] LOG_ACTIONS = {"Read", "Export", "Edit", "Delete"};
    public static final String[] LOG_PURPOSES = {
            "Safety follow-up", "Support", "Analysis", "Maintenance", "Audit", "Other"
    };
}
