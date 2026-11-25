-- Schéma SQLite CHIFAA (généré depuis DbSchema.java) : 21 tables, 3 déclencheurs, 8 index
-- PRAGMA foreign_keys = ON est activé par DatabaseHelper.onConfigure()

CREATE TABLE users (
    user_pid TEXT PRIMARY KEY NOT NULL CHECK (length(user_pid) = 36),
    age_band TEXT NOT NULL CHECK (age_band IN ('18–29', '30–39', '40–49', '50–59', '60+')),
    governorate TEXT NOT NULL CHECK (governorate GLOB 'TN-[0-9][0-9]'),
    preferred_language TEXT NOT NULL CHECK (preferred_language IN ('Darija', 'French', 'MSA', 'Mixed')),
    access_channel TEXT DEFAULT 'App' CHECK (access_channel IN ('App', 'Web', 'WhatsApp', 'Voice')),
    age_confirmed_18plus INTEGER NOT NULL CHECK (age_confirmed_18plus = 1),
    registration_date TEXT NOT NULL DEFAULT (date('now')),
    last_active_date TEXT,
    CHECK (last_active_date IS NULL OR last_active_date >= registration_date)
);

CREATE TABLE identities (
    identity_record_id TEXT PRIMARY KEY NOT NULL,
    user_pid TEXT NOT NULL UNIQUE REFERENCES users(user_pid) ON DELETE CASCADE,
    phone_number_enc BLOB NOT NULL,
    phone_hash TEXT NOT NULL UNIQUE CHECK (length(phone_hash) = 64),
    display_name_enc BLOB,
    created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ','now'))
);

CREATE TABLE consents (
    consent_id TEXT PRIMARY KEY NOT NULL CHECK (consent_id GLOB 'CS-[0-9][0-9][0-9][0-9][0-9]'),
    user_pid TEXT REFERENCES users(user_pid) ON DELETE SET NULL,
    consent_purpose TEXT NOT NULL CHECK (consent_purpose IN ('Service use', 'Health data', 'AI training reuse', 'Research interview', 'Survey', 'Usability test', 'Recording')),
    consent_version TEXT NOT NULL CHECK (consent_version GLOB 'v[0-9]*.[0-9]*'),
    consent_status TEXT NOT NULL CHECK (consent_status IN ('Given', 'Withdrawn', 'Expired')),
    given_at TEXT NOT NULL,
    withdrawn_at TEXT,
    capture_method TEXT NOT NULL CHECK (capture_method IN ('In-app', 'Written form', 'Recorded verbal')),
    CHECK (withdrawn_at IS NULL OR withdrawn_at >= given_at),
    CHECK (consent_status <> 'Withdrawn' OR withdrawn_at IS NOT NULL)
);

CREATE TABLE health_profiles (
    user_pid TEXT PRIMARY KEY NOT NULL REFERENCES users(user_pid) ON DELETE CASCADE,
    cancer_type TEXT CHECK (cancer_type IN ('Breast', 'Cervical', 'Other', 'Prefer not to say')),
    care_phase TEXT CHECK (care_phase IN ('Screening', 'Diagnosis', 'Pre-treatment', 'Active treatment', 'Post-treatment follow-up', 'Remission', 'Recurrence')),
    diagnosis_period_band TEXT CHECK (diagnosis_period_band IN ('<1 yr', '1–2 yrs', '3–5 yrs', '>5 yrs')),
    diagnosis_year INTEGER CHECK (diagnosis_year BETWEEN 1950 AND 2100),
    facility_type TEXT CHECK (facility_type IN ('Public hospital', 'Private clinic', 'Abroad', 'Prefer not to say')),
    fertility_info_interest INTEGER CHECK (fertility_info_interest IN (0, 1)),
    health_consent_version TEXT NOT NULL CHECK (health_consent_version GLOB 'v[0-9]*.[0-9]*'),
    updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ','now'))
);

CREATE TABLE health_profile_treatments (
    user_pid TEXT NOT NULL REFERENCES health_profiles(user_pid) ON DELETE CASCADE,
    treatment_type TEXT NOT NULL CHECK (treatment_type IN ('Surgery', 'Reconstruction', 'Chemotherapy', 'Radiotherapy', 'Hormone therapy', 'Targeted therapy', 'Immunotherapy', 'Prefer not to say')),
    PRIMARY KEY (user_pid, treatment_type)
);

CREATE TABLE health_checkins (
    checkin_id TEXT PRIMARY KEY NOT NULL,
    user_pid TEXT NOT NULL REFERENCES users(user_pid) ON DELETE CASCADE,
    checkin_date TEXT NOT NULL
);

CREATE TABLE checkin_side_effects (
    checkin_id TEXT NOT NULL REFERENCES health_checkins(checkin_id) ON DELETE CASCADE,
    side_effect TEXT NOT NULL CHECK (side_effect IN ('Fatigue', 'Nausea', 'Pain', 'Hair loss', 'Lymphedema', 'Hot flashes', 'Sleep problems', 'Anxiety', 'Low mood', 'Skin changes', 'Neuropathy')),
    PRIMARY KEY (checkin_id, side_effect)
);

CREATE TABLE staff (
    staff_id TEXT PRIMARY KEY NOT NULL CHECK (staff_id GLOB '[A-Z][A-Z]-[0-9][0-9]*'),
    login TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('Data steward', 'Clinical reviewer', 'Safety reviewer', 'Content team', 'Admin')),
    mfa_enabled INTEGER NOT NULL DEFAULT 1 CHECK (mfa_enabled IN (0, 1)),
    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
    created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ','now'))
);

CREATE TABLE kb_items (
    content_id TEXT PRIMARY KEY NOT NULL CHECK (content_id GLOB 'KB-[0-9][0-9][0-9][0-9]'),
    clinical_reviewer TEXT REFERENCES staff(staff_id) ON DELETE RESTRICT,
    superseded_by TEXT REFERENCES kb_items(content_id) ON DELETE SET NULL,
    title TEXT NOT NULL CHECK (length(title) BETWEEN 1 AND 200),
    topic TEXT NOT NULL,
    content_text TEXT NOT NULL,
    language TEXT NOT NULL CHECK (language IN ('Darija', 'French', 'MSA')),
    source_type TEXT NOT NULL CHECK (source_type IN ('Clinical guideline', 'Peer-reviewed article', 'Institutional website', 'Expert consensus', 'Patient association')),
    source_reference TEXT NOT NULL,
    evidence_level TEXT CHECK (evidence_level IN ('High', 'Moderate', 'Low', 'Expert opinion')),
    local_applicability TEXT CHECK (local_applicability IN ('Applies', 'Needs adaptation', 'Not applicable')),
    content_version TEXT NOT NULL CHECK (content_version GLOB 'v[0-9]*.[0-9]*'),
    approval_status TEXT NOT NULL DEFAULT 'Draft' CHECK (approval_status IN ('Draft', 'Clinically approved', 'Retired')),
    review_date TEXT,
    expiry_date TEXT,
    is_fictional INTEGER NOT NULL DEFAULT 1 CHECK (is_fictional IN (0, 1)),
    CHECK (approval_status <> 'Clinically approved' OR review_date IS NOT NULL),
    CHECK (expiry_date IS NULL OR review_date IS NULL OR expiry_date > review_date)
);

CREATE TABLE organisations (
    org_id TEXT PRIMARY KEY NOT NULL CHECK (org_id GLOB 'ORG-[0-9][0-9][0-9]'),
    org_name TEXT NOT NULL CHECK (length(org_name) BETWEEN 1 AND 200),
    service_type TEXT NOT NULL CHECK (service_type IN ('Oncology', 'Psychological', 'Legal', 'Fertility', 'Financial', 'Peer support', 'Prostheses & wigs', 'Transport', 'CNAM assistance')),
    governorate TEXT NOT NULL CHECK (governorate GLOB 'TN-[0-9][0-9]'),
    contact_phone TEXT,
    address TEXT,
    cost TEXT CHECK (cost IN ('Free', 'Covered by CNAM', 'Paid', 'Unknown')),
    verified_date TEXT NOT NULL,
    verification_method TEXT CHECK (verification_method IN ('Phone call', 'Site visit', 'Partner confirmation'))
);

CREATE TABLE organisation_languages (
    org_id TEXT NOT NULL REFERENCES organisations(org_id) ON DELETE CASCADE,
    language TEXT NOT NULL CHECK (language IN ('Darija', 'French', 'MSA')),
    PRIMARY KEY (org_id, language)
);

CREATE TABLE chat_sessions (
    session_id TEXT PRIMARY KEY NOT NULL,
    user_pid TEXT NOT NULL REFERENCES users(user_pid) ON DELETE CASCADE,
    channel TEXT NOT NULL DEFAULT 'App' CHECK (channel IN ('App', 'Web', 'WhatsApp', 'Voice')),
    started_at TEXT NOT NULL,
    ended_at TEXT,
    CHECK (ended_at IS NULL OR ended_at >= started_at)
);

CREATE TABLE messages (
    message_id TEXT PRIMARY KEY NOT NULL,
    session_id TEXT NOT NULL REFERENCES chat_sessions(session_id) ON DELETE CASCADE,
    content_id TEXT REFERENCES kb_items(content_id) ON DELETE SET NULL,
    sender_role TEXT NOT NULL CHECK (sender_role IN ('User', 'AI', 'Human reviewer')),
    message_timestamp TEXT NOT NULL,
    message_text TEXT NOT NULL CHECK (length(trim(message_text)) > 0),
    detected_language TEXT CHECK (detected_language IN ('Darija', 'French', 'MSA', 'Mixed')),
    topic_tag TEXT,
    distress_flag INTEGER NOT NULL DEFAULT 0 CHECK (distress_flag IN (0, 1)),
    rephrase_flag INTEGER NOT NULL DEFAULT 0 CHECK (rephrase_flag IN (0, 1)),
    error_flag INTEGER NOT NULL DEFAULT 0 CHECK (error_flag IN (0, 1)),
    response_latency_ms INTEGER CHECK (response_latency_ms >= 0),
    kb_version TEXT CHECK (kb_version GLOB 'KB-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]'),
    user_feedback INTEGER CHECK (user_feedback BETWEEN 1 AND 5)
);

CREATE TABLE safety_events (
    event_id TEXT PRIMARY KEY NOT NULL,
    user_pid TEXT REFERENCES users(user_pid) ON DELETE SET NULL,
    session_id TEXT REFERENCES chat_sessions(session_id) ON DELETE SET NULL,
    reviewer_id TEXT REFERENCES staff(staff_id) ON DELETE RESTRICT,
    org_id TEXT REFERENCES organisations(org_id) ON DELETE SET NULL,
    trigger_type TEXT NOT NULL CHECK (trigger_type IN ('Distress keyword', 'Self-harm mention', 'Medical emergency', 'Dosage request', 'Diagnosis request', 'Human requested', 'Reviewer flag')),
    severity TEXT NOT NULL CHECK (severity IN ('Low', 'Medium', 'High', 'Critical')),
    action_taken TEXT CHECK (action_taken IN ('Resource shared', 'Human contacted', 'Referral', 'None needed')),
    event_status TEXT NOT NULL DEFAULT 'Open' CHECK (event_status IN ('Open', 'In progress', 'Resolved', 'Closed')),
    flagged_at TEXT NOT NULL,
    resolved_at TEXT,
    referral_outcome TEXT CHECK (referral_outcome IN ('Contacted', 'Attended', 'Declined', 'Unknown', 'N/A')),
    CHECK (resolved_at IS NULL OR resolved_at >= flagged_at)
);

CREATE TABLE game_modules (
    module_id TEXT PRIMARY KEY NOT NULL CHECK (module_id GLOB 'GM-[0-9][0-9]'),
    title TEXT NOT NULL,
    topic TEXT NOT NULL,
    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1))
);

CREATE TABLE game_attempts (
    attempt_id TEXT PRIMARY KEY NOT NULL,
    user_pid TEXT NOT NULL REFERENCES users(user_pid) ON DELETE CASCADE,
    module_id TEXT NOT NULL REFERENCES game_modules(module_id) ON DELETE RESTRICT,
    attempt_number INTEGER NOT NULL CHECK (attempt_number >= 1),
    pre_score INTEGER CHECK (pre_score BETWEEN 0 AND 100),
    quiz_score INTEGER CHECK (quiz_score BETWEEN 0 AND 100),
    completion_status TEXT NOT NULL CHECK (completion_status IN ('Started', 'Completed')),
    completed_at TEXT,
    UNIQUE (user_pid, module_id, attempt_number),
    CHECK (completion_status <> 'Completed' OR completed_at IS NOT NULL)
);

CREATE TABLE survey_responses (
    response_id TEXT PRIMARY KEY NOT NULL,
    user_pid TEXT NOT NULL REFERENCES users(user_pid) ON DELETE CASCADE,
    consent_id TEXT NOT NULL REFERENCES consents(consent_id) ON DELETE RESTRICT,
    survey_wave TEXT NOT NULL CHECK (survey_wave GLOB 'W[0-9]*'),
    survey_date TEXT NOT NULL,
    time_since_diagnosis_band TEXT CHECK (time_since_diagnosis_band IN ('<1 yr', '1–2 yrs', '3–5 yrs', '>5 yrs')),
    quality_of_life_score REAL,
    chifaa_satisfaction INTEGER CHECK (chifaa_satisfaction BETWEEN 1 AND 5),
    open_comments TEXT
);

CREATE TABLE survey_unmet_needs (
    response_id TEXT NOT NULL REFERENCES survey_responses(response_id) ON DELETE CASCADE,
    need TEXT NOT NULL CHECK (need IN ('Information', 'Emotional', 'Financial', 'Legal', 'Fertility', 'Practical', 'Other')),
    PRIMARY KEY (response_id, need)
);

CREATE TABLE survey_support_sources (
    response_id TEXT NOT NULL REFERENCES survey_responses(response_id) ON DELETE CASCADE,
    source TEXT NOT NULL CHECK (source IN ('Family', 'Peers', 'Association', 'Clinician', 'Online', 'CHIFAA')),
    PRIMARY KEY (response_id, source)
);

CREATE TABLE data_requests (
    request_id TEXT PRIMARY KEY NOT NULL CHECK (request_id GLOB 'REQ-[0-9][0-9][0-9]'),
    user_pid TEXT REFERENCES users(user_pid) ON DELETE SET NULL,
    handled_by TEXT REFERENCES staff(staff_id) ON DELETE RESTRICT,
    record_type TEXT NOT NULL CHECK (record_type IN ('Access request', 'Rectification', 'Deletion request', 'Consent withdrawal', 'Incident / breach')),
    received_at TEXT NOT NULL,
    closed_at TEXT,
    outcome TEXT,
    inpdp_notified INTEGER NOT NULL DEFAULT 0 CHECK (inpdp_notified IN (0, 1)),
    CHECK (closed_at IS NULL OR closed_at >= received_at)
);

CREATE TABLE access_log (
    log_id TEXT PRIMARY KEY NOT NULL,
    staff_id TEXT NOT NULL REFERENCES staff(staff_id) ON DELETE RESTRICT,
    table_name TEXT NOT NULL,
    record_ref TEXT NOT NULL,
    action TEXT NOT NULL CHECK (action IN ('Read', 'Export', 'Edit', 'Delete')),
    purpose TEXT NOT NULL CHECK (purpose IN ('Safety follow-up', 'Support', 'Analysis', 'Maintenance', 'Audit', 'Other')),
    accessed_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ','now'))
);

CREATE TRIGGER trg_access_log_no_update BEFORE UPDATE ON access_log BEGIN SELECT RAISE(ABORT, 'access_log est en ajout seul'); END;

CREATE TRIGGER trg_access_log_no_delete BEFORE DELETE ON access_log BEGIN SELECT RAISE(ABORT, 'access_log est en ajout seul'); END;

CREATE TRIGGER trg_staff_no_delete BEFORE DELETE ON staff BEGIN SELECT RAISE(ABORT, 'Un compte du personnel se désactive, il ne se supprime pas'); END;

CREATE INDEX idx_consents_user ON consents(user_pid, consent_purpose);

CREATE INDEX idx_checkins_user ON health_checkins(user_pid, checkin_date);

CREATE INDEX idx_sessions_user ON chat_sessions(user_pid);

CREATE INDEX idx_messages_session ON messages(session_id, message_timestamp);

CREATE INDEX idx_kb_topic_lang ON kb_items(topic, language, approval_status);

CREATE INDEX idx_org_search ON organisations(governorate, service_type);

CREATE INDEX idx_safety_status ON safety_events(event_status, severity);

CREATE INDEX idx_attempts_user ON game_attempts(user_pid, module_id);
