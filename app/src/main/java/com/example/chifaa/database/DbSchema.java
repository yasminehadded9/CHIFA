package com.example.chifaa.database;

import com.example.chifaa.utils.AppConstants;

/**
 * Schéma SQLite de CHIFAA : 16 tables principales + 5 tables d'association = 21 tables.
 *
 * Adaptation du modèle PostgreSQL du cahier des charges à SQLite :
 *  - UUID          -> TEXT (36 caractères, généré en Java avec UUID.randomUUID())
 *  - BOOLEAN       -> INTEGER 0/1 avec CHECK (x IN (0,1))
 *  - DATE          -> TEXT au format ISO 8601 'YYYY-MM-DD'
 *  - TIMESTAMPTZ   -> TEXT au format ISO 8601 UTC 'YYYY-MM-DDTHH:MM:SSZ'
 *  - BYTEA         -> BLOB
 *  - listes fermées -> CHECK (colonne IN (...)) générées depuis AppConstants
 *  - formats (KB-####, ORG-###...) -> CHECK avec GLOB
 *  - champs à choix multiples -> tables d'association (clé primaire composée)
 *
 * Cette classe n'utilise aucune classe Android : on peut la tester en Java pur.
 */
public final class DbSchema {

    private DbSchema() { }

    // ============================================================ Noms des tables
    public static final String T_USERS = "users";
    public static final String T_IDENTITIES = "identities";
    public static final String T_CONSENTS = "consents";
    public static final String T_HEALTH_PROFILES = "health_profiles";
    public static final String T_HEALTH_TREATMENTS = "health_profile_treatments";
    public static final String T_HEALTH_CHECKINS = "health_checkins";
    public static final String T_CHECKIN_SIDE_EFFECTS = "checkin_side_effects";
    public static final String T_CHAT_SESSIONS = "chat_sessions";
    public static final String T_MESSAGES = "messages";
    public static final String T_SAFETY_EVENTS = "safety_events";
    public static final String T_KB_ITEMS = "kb_items";
    public static final String T_ORGANISATIONS = "organisations";
    public static final String T_ORG_LANGUAGES = "organisation_languages";
    public static final String T_GAME_MODULES = "game_modules";
    public static final String T_GAME_ATTEMPTS = "game_attempts";
    public static final String T_SURVEY_RESPONSES = "survey_responses";
    public static final String T_SURVEY_UNMET_NEEDS = "survey_unmet_needs";
    public static final String T_SURVEY_SUPPORT_SOURCES = "survey_support_sources";
    public static final String T_STAFF = "staff";
    public static final String T_DATA_REQUESTS = "data_requests";
    public static final String T_ACCESS_LOG = "access_log";

    // ============================================================ Fragments réutilisables
    /** Date/heure courante UTC au format ISO 8601 (valeur par défaut SQL). */
    private static final String NOW_UTC = "(strftime('%Y-%m-%dT%H:%M:%SZ','now'))";
    private static final String TODAY = "(date('now'))";
    private static final String GOV_GLOB = "GLOB 'TN-[0-9][0-9]'";
    private static final String VERSION_GLOB = "GLOB 'v[0-9]*.[0-9]*'";

    /** Construit "('a','b','c')" à partir d'un tableau Java (apostrophes échappées). */
    static String in(String[] values) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append('\'').append(values[i].replace("'", "''")).append('\'');
        }
        return sb.append(')').toString();
    }

    /** Colonne booléenne SQLite. */
    private static String bool(String name, boolean notNull, String defaultValue) {
        return name + " INTEGER" + (notNull ? " NOT NULL" : "")
                + (defaultValue != null ? " DEFAULT " + defaultValue : "")
                + " CHECK (" + name + " IN (0, 1))";
    }

    // ============================================================ 1. Profil pseudonyme
    static final String CREATE_USERS = "CREATE TABLE " + T_USERS + " ("
            + "user_pid TEXT PRIMARY KEY NOT NULL CHECK (length(user_pid) = 36), "
            + "age_band TEXT NOT NULL CHECK (age_band IN " + in(AppConstants.AGE_BANDS) + "), "
            + "governorate TEXT NOT NULL CHECK (governorate " + GOV_GLOB + "), "
            + "preferred_language TEXT NOT NULL CHECK (preferred_language IN " + in(AppConstants.LANGUAGES) + "), "
            + "access_channel TEXT DEFAULT 'App' CHECK (access_channel IN " + in(AppConstants.CHANNELS) + "), "
            + "age_confirmed_18plus INTEGER NOT NULL CHECK (age_confirmed_18plus = 1), "
            + "registration_date TEXT NOT NULL DEFAULT " + TODAY + ", "
            + "last_active_date TEXT, "
            + "CHECK (last_active_date IS NULL OR last_active_date >= registration_date))";

    // ============================================================ 2. Identités (chiffrées)
    static final String CREATE_IDENTITIES = "CREATE TABLE " + T_IDENTITIES + " ("
            + "identity_record_id TEXT PRIMARY KEY NOT NULL, "
            + "user_pid TEXT NOT NULL UNIQUE REFERENCES " + T_USERS + "(user_pid) ON DELETE CASCADE, "
            + "phone_number_enc BLOB NOT NULL, "
            + "phone_hash TEXT NOT NULL UNIQUE CHECK (length(phone_hash) = 64), "
            + "display_name_enc BLOB, "
            + "created_at TEXT NOT NULL DEFAULT " + NOW_UTC + ")";

    // ============================================================ 3. Consentements
    static final String CREATE_CONSENTS = "CREATE TABLE " + T_CONSENTS + " ("
            + "consent_id TEXT PRIMARY KEY NOT NULL CHECK (consent_id GLOB 'CS-[0-9][0-9][0-9][0-9][0-9]'), "
            + "user_pid TEXT REFERENCES " + T_USERS + "(user_pid) ON DELETE SET NULL, "
            + "consent_purpose TEXT NOT NULL CHECK (consent_purpose IN " + in(AppConstants.CONSENT_PURPOSES) + "), "
            + "consent_version TEXT NOT NULL CHECK (consent_version " + VERSION_GLOB + "), "
            + "consent_status TEXT NOT NULL CHECK (consent_status IN " + in(AppConstants.CONSENT_STATUSES) + "), "
            + "given_at TEXT NOT NULL, "
            + "withdrawn_at TEXT, "
            + "capture_method TEXT NOT NULL CHECK (capture_method IN " + in(AppConstants.CAPTURE_METHODS) + "), "
            + "CHECK (withdrawn_at IS NULL OR withdrawn_at >= given_at), "
            + "CHECK (consent_status <> 'Withdrawn' OR withdrawn_at IS NOT NULL))";

    // ============================================================ 4. Profil santé
    static final String CREATE_HEALTH_PROFILES = "CREATE TABLE " + T_HEALTH_PROFILES + " ("
            + "user_pid TEXT PRIMARY KEY NOT NULL REFERENCES " + T_USERS + "(user_pid) ON DELETE CASCADE, "
            + "cancer_type TEXT CHECK (cancer_type IN " + in(AppConstants.CANCER_TYPES) + "), "
            + "care_phase TEXT CHECK (care_phase IN " + in(AppConstants.CARE_PHASES) + "), "
            + "diagnosis_period_band TEXT CHECK (diagnosis_period_band IN " + in(AppConstants.DIAGNOSIS_BANDS) + "), "
            // SQLite interdit date('now') dans un CHECK : la borne "année en cours" est contrôlée en Java.
            + "diagnosis_year INTEGER CHECK (diagnosis_year BETWEEN 1950 AND 2100), "
            + "facility_type TEXT CHECK (facility_type IN " + in(AppConstants.FACILITY_TYPES) + "), "
            + bool("fertility_info_interest", false, null) + ", "
            + "health_consent_version TEXT NOT NULL CHECK (health_consent_version " + VERSION_GLOB + "), "
            + "updated_at TEXT NOT NULL DEFAULT " + NOW_UTC + ")";

    static final String CREATE_HEALTH_TREATMENTS = "CREATE TABLE " + T_HEALTH_TREATMENTS + " ("
            + "user_pid TEXT NOT NULL REFERENCES " + T_HEALTH_PROFILES + "(user_pid) ON DELETE CASCADE, "
            + "treatment_type TEXT NOT NULL CHECK (treatment_type IN " + in(AppConstants.TREATMENT_TYPES) + "), "
            + "PRIMARY KEY (user_pid, treatment_type))";

    // ============================================================ 5. Bilans
    static final String CREATE_HEALTH_CHECKINS = "CREATE TABLE " + T_HEALTH_CHECKINS + " ("
            + "checkin_id TEXT PRIMARY KEY NOT NULL, "
            + "user_pid TEXT NOT NULL REFERENCES " + T_USERS + "(user_pid) ON DELETE CASCADE, "
            + "checkin_date TEXT NOT NULL)";

    static final String CREATE_CHECKIN_SIDE_EFFECTS = "CREATE TABLE " + T_CHECKIN_SIDE_EFFECTS + " ("
            + "checkin_id TEXT NOT NULL REFERENCES " + T_HEALTH_CHECKINS + "(checkin_id) ON DELETE CASCADE, "
            + "side_effect TEXT NOT NULL CHECK (side_effect IN " + in(AppConstants.SIDE_EFFECTS) + "), "
            + "PRIMARY KEY (checkin_id, side_effect))";

    // ============================================================ 6. Conversations
    static final String CREATE_CHAT_SESSIONS = "CREATE TABLE " + T_CHAT_SESSIONS + " ("
            + "session_id TEXT PRIMARY KEY NOT NULL, "
            + "user_pid TEXT NOT NULL REFERENCES " + T_USERS + "(user_pid) ON DELETE CASCADE, "
            + "channel TEXT NOT NULL DEFAULT 'App' CHECK (channel IN " + in(AppConstants.CHANNELS) + "), "
            + "started_at TEXT NOT NULL, "
            + "ended_at TEXT, "
            + "CHECK (ended_at IS NULL OR ended_at >= started_at))";

    static final String CREATE_MESSAGES = "CREATE TABLE " + T_MESSAGES + " ("
            + "message_id TEXT PRIMARY KEY NOT NULL, "
            + "session_id TEXT NOT NULL REFERENCES " + T_CHAT_SESSIONS + "(session_id) ON DELETE CASCADE, "
            + "content_id TEXT REFERENCES " + T_KB_ITEMS + "(content_id) ON DELETE SET NULL, "
            + "sender_role TEXT NOT NULL CHECK (sender_role IN " + in(AppConstants.SENDER_ROLES) + "), "
            + "message_timestamp TEXT NOT NULL, "
            + "message_text TEXT NOT NULL CHECK (length(trim(message_text)) > 0), "
            + "detected_language TEXT CHECK (detected_language IN " + in(AppConstants.LANGUAGES) + "), "
            + "topic_tag TEXT, "
            + bool("distress_flag", true, "0") + ", "
            + bool("rephrase_flag", true, "0") + ", "
            + bool("error_flag", true, "0") + ", "
            + "response_latency_ms INTEGER CHECK (response_latency_ms >= 0), "
            + "kb_version TEXT CHECK (kb_version GLOB 'KB-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]'), "
            + "user_feedback INTEGER CHECK (user_feedback BETWEEN 1 AND 5))";

    // ============================================================ 7. Événements de sécurité
    static final String CREATE_SAFETY_EVENTS = "CREATE TABLE " + T_SAFETY_EVENTS + " ("
            + "event_id TEXT PRIMARY KEY NOT NULL, "
            + "user_pid TEXT REFERENCES " + T_USERS + "(user_pid) ON DELETE SET NULL, "
            + "session_id TEXT REFERENCES " + T_CHAT_SESSIONS + "(session_id) ON DELETE SET NULL, "
            + "reviewer_id TEXT REFERENCES " + T_STAFF + "(staff_id) ON DELETE RESTRICT, "
            + "org_id TEXT REFERENCES " + T_ORGANISATIONS + "(org_id) ON DELETE SET NULL, "
            + "trigger_type TEXT NOT NULL CHECK (trigger_type IN " + in(AppConstants.TRIGGER_TYPES) + "), "
            + "severity TEXT NOT NULL CHECK (severity IN " + in(AppConstants.SEVERITIES) + "), "
            + "action_taken TEXT CHECK (action_taken IN " + in(AppConstants.ACTIONS_TAKEN) + "), "
            + "event_status TEXT NOT NULL DEFAULT 'Open' CHECK (event_status IN " + in(AppConstants.EVENT_STATUSES) + "), "
            + "flagged_at TEXT NOT NULL, "
            + "resolved_at TEXT, "
            + "referral_outcome TEXT CHECK (referral_outcome IN " + in(AppConstants.REFERRAL_OUTCOMES) + "), "
            + "CHECK (resolved_at IS NULL OR resolved_at >= flagged_at))";

    // ============================================================ 8. Fiches médicales
    static final String CREATE_KB_ITEMS = "CREATE TABLE " + T_KB_ITEMS + " ("
            + "content_id TEXT PRIMARY KEY NOT NULL CHECK (content_id GLOB 'KB-[0-9][0-9][0-9][0-9]'), "
            + "clinical_reviewer TEXT REFERENCES " + T_STAFF + "(staff_id) ON DELETE RESTRICT, "
            + "superseded_by TEXT REFERENCES " + T_KB_ITEMS + "(content_id) ON DELETE SET NULL, "
            + "title TEXT NOT NULL CHECK (length(title) BETWEEN 1 AND 200), "
            + "topic TEXT NOT NULL, "
            + "content_text TEXT NOT NULL, "
            + "language TEXT NOT NULL CHECK (language IN " + in(AppConstants.CONTENT_LANGUAGES) + "), "
            + "source_type TEXT NOT NULL CHECK (source_type IN " + in(AppConstants.SOURCE_TYPES) + "), "
            + "source_reference TEXT NOT NULL, "
            + "evidence_level TEXT CHECK (evidence_level IN " + in(AppConstants.EVIDENCE_LEVELS) + "), "
            + "local_applicability TEXT CHECK (local_applicability IN " + in(AppConstants.LOCAL_APPLICABILITY) + "), "
            + "content_version TEXT NOT NULL CHECK (content_version " + VERSION_GLOB + "), "
            + "approval_status TEXT NOT NULL DEFAULT 'Draft' CHECK (approval_status IN " + in(AppConstants.APPROVAL_STATUSES) + "), "
            + "review_date TEXT, "
            + "expiry_date TEXT, "
            + bool("is_fictional", true, "1") + ", "
            + "CHECK (approval_status <> 'Clinically approved' OR review_date IS NOT NULL), "
            + "CHECK (expiry_date IS NULL OR review_date IS NULL OR expiry_date > review_date))";

    // ============================================================ 9. Annuaire
    static final String CREATE_ORGANISATIONS = "CREATE TABLE " + T_ORGANISATIONS + " ("
            + "org_id TEXT PRIMARY KEY NOT NULL CHECK (org_id GLOB 'ORG-[0-9][0-9][0-9]'), "
            + "org_name TEXT NOT NULL CHECK (length(org_name) BETWEEN 1 AND 200), "
            + "service_type TEXT NOT NULL CHECK (service_type IN " + in(AppConstants.SERVICE_TYPES) + "), "
            + "governorate TEXT NOT NULL CHECK (governorate " + GOV_GLOB + "), "
            + "contact_phone TEXT, "
            + "address TEXT, "
            + "cost TEXT CHECK (cost IN " + in(AppConstants.COSTS) + "), "
            + "verified_date TEXT NOT NULL, "
            + "verification_method TEXT CHECK (verification_method IN " + in(AppConstants.VERIFICATION_METHODS) + "))";

    static final String CREATE_ORG_LANGUAGES = "CREATE TABLE " + T_ORG_LANGUAGES + " ("
            + "org_id TEXT NOT NULL REFERENCES " + T_ORGANISATIONS + "(org_id) ON DELETE CASCADE, "
            + "language TEXT NOT NULL CHECK (language IN " + in(AppConstants.CONTENT_LANGUAGES) + "), "
            + "PRIMARY KEY (org_id, language))";

    // ============================================================ 10. Jeu éducatif
    static final String CREATE_GAME_MODULES = "CREATE TABLE " + T_GAME_MODULES + " ("
            + "module_id TEXT PRIMARY KEY NOT NULL CHECK (module_id GLOB 'GM-[0-9][0-9]'), "
            + "title TEXT NOT NULL, "
            + "topic TEXT NOT NULL, "
            + bool("active", true, "1") + ")";

    static final String CREATE_GAME_ATTEMPTS = "CREATE TABLE " + T_GAME_ATTEMPTS + " ("
            + "attempt_id TEXT PRIMARY KEY NOT NULL, "
            + "user_pid TEXT NOT NULL REFERENCES " + T_USERS + "(user_pid) ON DELETE CASCADE, "
            + "module_id TEXT NOT NULL REFERENCES " + T_GAME_MODULES + "(module_id) ON DELETE RESTRICT, "
            + "attempt_number INTEGER NOT NULL CHECK (attempt_number >= 1), "
            + "pre_score INTEGER CHECK (pre_score BETWEEN 0 AND 100), "
            + "quiz_score INTEGER CHECK (quiz_score BETWEEN 0 AND 100), "
            + "completion_status TEXT NOT NULL CHECK (completion_status IN " + in(AppConstants.COMPLETION_STATUSES) + "), "
            + "completed_at TEXT, "
            + "UNIQUE (user_pid, module_id, attempt_number), "
            + "CHECK (completion_status <> 'Completed' OR completed_at IS NOT NULL))";

    // ============================================================ 11. Enquête
    static final String CREATE_SURVEY_RESPONSES = "CREATE TABLE " + T_SURVEY_RESPONSES + " ("
            + "response_id TEXT PRIMARY KEY NOT NULL, "
            + "user_pid TEXT NOT NULL REFERENCES " + T_USERS + "(user_pid) ON DELETE CASCADE, "
            + "consent_id TEXT NOT NULL REFERENCES " + T_CONSENTS + "(consent_id) ON DELETE RESTRICT, "
            + "survey_wave TEXT NOT NULL CHECK (survey_wave GLOB 'W[0-9]*'), "
            + "survey_date TEXT NOT NULL, "
            + "time_since_diagnosis_band TEXT CHECK (time_since_diagnosis_band IN " + in(AppConstants.DIAGNOSIS_BANDS) + "), "
            + "quality_of_life_score REAL, "
            + "chifaa_satisfaction INTEGER CHECK (chifaa_satisfaction BETWEEN 1 AND 5), "
            + "open_comments TEXT)";

    static final String CREATE_SURVEY_UNMET_NEEDS = "CREATE TABLE " + T_SURVEY_UNMET_NEEDS + " ("
            + "response_id TEXT NOT NULL REFERENCES " + T_SURVEY_RESPONSES + "(response_id) ON DELETE CASCADE, "
            + "need TEXT NOT NULL CHECK (need IN " + in(AppConstants.UNMET_NEEDS) + "), "
            + "PRIMARY KEY (response_id, need))";

    static final String CREATE_SURVEY_SUPPORT_SOURCES = "CREATE TABLE " + T_SURVEY_SUPPORT_SOURCES + " ("
            + "response_id TEXT NOT NULL REFERENCES " + T_SURVEY_RESPONSES + "(response_id) ON DELETE CASCADE, "
            + "source TEXT NOT NULL CHECK (source IN " + in(AppConstants.SUPPORT_SOURCES) + "), "
            + "PRIMARY KEY (response_id, source))";

    // ============================================================ 12. Équipe
    static final String CREATE_STAFF = "CREATE TABLE " + T_STAFF + " ("
            + "staff_id TEXT PRIMARY KEY NOT NULL CHECK (staff_id GLOB '[A-Z][A-Z]-[0-9][0-9]*'), "
            + "login TEXT NOT NULL UNIQUE, "
            + "password_hash TEXT NOT NULL, "
            + "role TEXT NOT NULL CHECK (role IN " + in(AppConstants.STAFF_ROLES) + "), "
            + bool("mfa_enabled", true, "1") + ", "
            + bool("active", true, "1") + ", "
            + "created_at TEXT NOT NULL DEFAULT " + NOW_UTC + ")";

    // ============================================================ 13. Demandes
    static final String CREATE_DATA_REQUESTS = "CREATE TABLE " + T_DATA_REQUESTS + " ("
            + "request_id TEXT PRIMARY KEY NOT NULL CHECK (request_id GLOB 'REQ-[0-9][0-9][0-9]'), "
            + "user_pid TEXT REFERENCES " + T_USERS + "(user_pid) ON DELETE SET NULL, "
            + "handled_by TEXT REFERENCES " + T_STAFF + "(staff_id) ON DELETE RESTRICT, "
            + "record_type TEXT NOT NULL CHECK (record_type IN " + in(AppConstants.RECORD_TYPES) + "), "
            + "received_at TEXT NOT NULL, "
            + "closed_at TEXT, "
            + "outcome TEXT, "
            + bool("inpdp_notified", true, "0") + ", "
            + "CHECK (closed_at IS NULL OR closed_at >= received_at))";

    // ============================================================ 14. Journal d'accès
    static final String CREATE_ACCESS_LOG = "CREATE TABLE " + T_ACCESS_LOG + " ("
            + "log_id TEXT PRIMARY KEY NOT NULL, "
            + "staff_id TEXT NOT NULL REFERENCES " + T_STAFF + "(staff_id) ON DELETE RESTRICT, "
            + "table_name TEXT NOT NULL, "
            + "record_ref TEXT NOT NULL, "
            + "action TEXT NOT NULL CHECK (action IN " + in(AppConstants.LOG_ACTIONS) + "), "
            + "purpose TEXT NOT NULL CHECK (purpose IN " + in(AppConstants.LOG_PURPOSES) + "), "
            + "accessed_at TEXT NOT NULL DEFAULT " + NOW_UTC + ")";

    // ============================================================ Déclencheurs (règles métier)
    /** access_log est en ajout seul : ni modification, ni suppression. */
    static final String TRG_ACCESS_LOG_NO_UPDATE = "CREATE TRIGGER trg_access_log_no_update "
            + "BEFORE UPDATE ON " + T_ACCESS_LOG + " BEGIN "
            + "SELECT RAISE(ABORT, 'access_log est en ajout seul'); END";

    static final String TRG_ACCESS_LOG_NO_DELETE = "CREATE TRIGGER trg_access_log_no_delete "
            + "BEFORE DELETE ON " + T_ACCESS_LOG + " BEGIN "
            + "SELECT RAISE(ABORT, 'access_log est en ajout seul'); END";

    /** Un compte du personnel n'est jamais supprimé : on le désactive (active = 0). */
    static final String TRG_STAFF_NO_DELETE = "CREATE TRIGGER trg_staff_no_delete "
            + "BEFORE DELETE ON " + T_STAFF + " BEGIN "
            + "SELECT RAISE(ABORT, 'Un compte du personnel se désactive, il ne se supprime pas'); END";

    // ============================================================ Index (recherches fréquentes)
    static final String[] INDEXES = {
            "CREATE INDEX idx_consents_user ON " + T_CONSENTS + "(user_pid, consent_purpose)",
            "CREATE INDEX idx_checkins_user ON " + T_HEALTH_CHECKINS + "(user_pid, checkin_date)",
            "CREATE INDEX idx_sessions_user ON " + T_CHAT_SESSIONS + "(user_pid)",
            "CREATE INDEX idx_messages_session ON " + T_MESSAGES + "(session_id, message_timestamp)",
            "CREATE INDEX idx_kb_topic_lang ON " + T_KB_ITEMS + "(topic, language, approval_status)",
            "CREATE INDEX idx_org_search ON " + T_ORGANISATIONS + "(governorate, service_type)",
            "CREATE INDEX idx_safety_status ON " + T_SAFETY_EVENTS + "(event_status, severity)",
            "CREATE INDEX idx_attempts_user ON " + T_GAME_ATTEMPTS + "(user_pid, module_id)"
    };

    // ============================================================ Ordre de création
    /** Tables parentes d'abord, puis tables enfants. */
    public static final String[] CREATE_TABLES = {
            CREATE_USERS, CREATE_IDENTITIES, CREATE_CONSENTS,
            CREATE_HEALTH_PROFILES, CREATE_HEALTH_TREATMENTS,
            CREATE_HEALTH_CHECKINS, CREATE_CHECKIN_SIDE_EFFECTS,
            CREATE_STAFF, CREATE_KB_ITEMS,
            CREATE_ORGANISATIONS, CREATE_ORG_LANGUAGES,
            CREATE_CHAT_SESSIONS, CREATE_MESSAGES, CREATE_SAFETY_EVENTS,
            CREATE_GAME_MODULES, CREATE_GAME_ATTEMPTS,
            CREATE_SURVEY_RESPONSES, CREATE_SURVEY_UNMET_NEEDS, CREATE_SURVEY_SUPPORT_SOURCES,
            CREATE_DATA_REQUESTS, CREATE_ACCESS_LOG
    };

    public static final String[] CREATE_TRIGGERS = {
            TRG_ACCESS_LOG_NO_UPDATE, TRG_ACCESS_LOG_NO_DELETE, TRG_STAFF_NO_DELETE
    };

    /** Tables enfants d'abord : utilisé par onUpgrade() pour tout supprimer proprement. */
    public static final String[] DROP_ORDER = {
            T_ACCESS_LOG, T_DATA_REQUESTS,
            T_SURVEY_SUPPORT_SOURCES, T_SURVEY_UNMET_NEEDS, T_SURVEY_RESPONSES,
            T_GAME_ATTEMPTS, T_GAME_MODULES,
            T_SAFETY_EVENTS, T_MESSAGES, T_CHAT_SESSIONS,
            T_ORG_LANGUAGES, T_ORGANISATIONS,
            T_KB_ITEMS,
            T_CHECKIN_SIDE_EFFECTS, T_HEALTH_CHECKINS,
            T_HEALTH_TREATMENTS, T_HEALTH_PROFILES,
            T_CONSENTS, T_IDENTITIES, T_USERS,
            T_STAFF
    };
}
