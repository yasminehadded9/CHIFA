package com.example.chifaa.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.chifaa.models.SignupData;
import com.example.chifaa.models.UserProfile;
import com.example.chifaa.utils.AppConstants;
import com.example.chifaa.utils.CryptoUtils;
import com.example.chifaa.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Accès aux tables users, identities et consents (module M1).
 * Le numéro de téléphone n'est écrit QUE dans identities, chiffré.
 */
public class AccountDao {

    private final DatabaseHelper helper;

    public AccountDao(Context context) {
        helper = DatabaseHelper.getInstance(context);
    }

    /** Cherche un compte par l'empreinte du numéro. Renvoie user_pid ou null. */
    public String findUserPidByPhone(String phone8) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT user_pid FROM " + DbSchema.T_IDENTITIES
                + " WHERE phone_hash = ?", new String[]{CryptoUtils.sha256Hex(phone8)});
        String pid = null;
        if (c.moveToFirst()) {
            pid = c.getString(0);
        }
        c.close();
        return pid;
    }

    public boolean phoneExists(String phone8) {
        return findUserPidByPhone(phone8) != null;
    }

    /**
     * Crée le compte complet dans UNE transaction : profil + identité chiffrée
     * + un enregistrement par consentement accepté. Tout ou rien.
     * @return le user_pid (UUID pseudonyme) de la nouvelle utilisatrice
     */
    public String createAccount(SignupData data) throws Exception {
        String userPid = UUID.randomUUID().toString();
        String today = DateUtils.todayUtc();

        // Chiffrement AVANT d'ouvrir la transaction
        byte[] phoneEnc = CryptoUtils.encrypt(data.phone);
        byte[] nameEnc = (data.displayName == null || data.displayName.isEmpty())
                ? null : CryptoUtils.encrypt(data.displayName);

        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            // 1. Profil pseudonyme
            ContentValues user = new ContentValues();
            user.put("user_pid", userPid);
            user.put("age_band", data.ageBand);
            user.put("governorate", data.governorate);
            user.put("preferred_language", data.preferredLanguage);
            user.put("access_channel", "App");
            user.put("age_confirmed_18plus", 1);
            user.put("registration_date", today);
            user.put("last_active_date", today);
            db.insertOrThrow(DbSchema.T_USERS, null, user);

            // 2. Identité chiffrée
            ContentValues identity = new ContentValues();
            identity.put("identity_record_id", UUID.randomUUID().toString());
            identity.put("user_pid", userPid);
            identity.put("phone_number_enc", phoneEnc);
            identity.put("phone_hash", CryptoUtils.sha256Hex(data.phone));
            if (nameEnc != null) {
                identity.put("display_name_enc", nameEnc);
            }
            identity.put("created_at", DateUtils.nowUtc());
            db.insertOrThrow(DbSchema.T_IDENTITIES, null, identity);

            // 3. Consentements : un enregistrement par finalité acceptée
            insertConsent(db, userPid, AppConstants.CONSENT_SERVICE, data.serviceConsentAt);
            if (data.hasHealthConsent()) {
                insertConsent(db, userPid, AppConstants.CONSENT_HEALTH, data.healthConsentAt);
            }
            if (data.hasSurveyConsent()) {
                insertConsent(db, userPid, AppConstants.CONSENT_SURVEY, data.surveyConsentAt);
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return userPid;
    }

    private void insertConsent(SQLiteDatabase db, String userPid, String purpose, String givenAt) {
        ContentValues v = new ContentValues();
        v.put("consent_id", nextConsentId(db));
        v.put("user_pid", userPid);
        v.put("consent_purpose", purpose);
        v.put("consent_version", AppConstants.CONSENT_TEXT_VERSION);
        v.put("consent_status", AppConstants.STATUS_GIVEN);
        v.put("given_at", givenAt);
        v.put("capture_method", "In-app");
        db.insertOrThrow(DbSchema.T_CONSENTS, null, v);
    }

    /** Prochain identifiant au format CS-##### (CS-00001, CS-00002...). */
    private String nextConsentId(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT MAX(CAST(substr(consent_id, 4) AS INTEGER)) FROM "
                + DbSchema.T_CONSENTS, null);
        int max = 0;
        if (c.moveToFirst() && !c.isNull(0)) {
            max = c.getInt(0);
        }
        c.close();
        return String.format(Locale.US, "CS-%05d", max + 1);
    }

    /** Met à jour la date de dernière activité (connexion). */
    public void touchLastActive(String userPid) {
        ContentValues v = new ContentValues();
        v.put("last_active_date", DateUtils.todayUtc());
        helper.getWritableDatabase().update(DbSchema.T_USERS, v, "user_pid = ?", new String[]{userPid});
    }

    /** Surnom déchiffré, ou null s'il n'a pas été renseigné. */
    public String getDisplayName(String userPid) {
        Cursor c = helper.getReadableDatabase().rawQuery("SELECT display_name_enc FROM "
                + DbSchema.T_IDENTITIES + " WHERE user_pid = ?", new String[]{userPid});
        String name = null;
        try {
            if (c.moveToFirst() && !c.isNull(0)) {
                name = CryptoUtils.decrypt(c.getBlob(0));
            }
        } catch (Exception e) {
            name = null; // clé perdue (ex. données restaurées) : on n'affiche pas de nom
        } finally {
            c.close();
        }
        return name;
    }

    /** Finalités des consentements actuellement au statut "Given". */
    public List<String> getGivenConsents(String userPid) {
        List<String> purposes = new ArrayList<>();
        Cursor c = helper.getReadableDatabase().rawQuery("SELECT consent_purpose FROM "
                + DbSchema.T_CONSENTS + " WHERE user_pid = ? AND consent_status = ? ORDER BY consent_id",
                new String[]{userPid, AppConstants.STATUS_GIVEN});
        while (c.moveToNext()) {
            purposes.add(c.getString(0));
        }
        c.close();
        return purposes;
    }

    // =================================================================== Profil (M2)

    /** Lit le profil complet, ou null si le compte n'existe pas. */
    public UserProfile getProfile(String userPid) {
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT age_band, governorate, preferred_language, registration_date FROM "
                        + DbSchema.T_USERS + " WHERE user_pid = ?", new String[]{userPid});
        UserProfile profile = null;
        if (c.moveToFirst()) {
            profile = new UserProfile();
            profile.userPid = userPid;
            profile.ageBand = c.getString(0);
            profile.governorate = c.getString(1);
            profile.preferredLanguage = c.getString(2);
            profile.registrationDate = c.getString(3);
        }
        c.close();
        if (profile != null) {
            profile.displayName = getDisplayName(userPid);
        }
        return profile;
    }

    /**
     * Met à jour le profil (users) et le surnom chiffré (identities) en une transaction.
     * @param displayName null ou vide pour effacer le surnom
     */
    public void updateProfile(String userPid, String ageBand, String governorate,
                              String preferredLanguage, String displayName) throws Exception {
        byte[] nameEnc = (displayName == null || displayName.isEmpty())
                ? null : CryptoUtils.encrypt(displayName);

        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues user = new ContentValues();
            user.put("age_band", ageBand);
            user.put("governorate", governorate);
            user.put("preferred_language", preferredLanguage);
            user.put("last_active_date", DateUtils.todayUtc());
            db.update(DbSchema.T_USERS, user, "user_pid = ?", new String[]{userPid});

            ContentValues identity = new ContentValues();
            if (nameEnc == null) {
                identity.putNull("display_name_enc");
            } else {
                identity.put("display_name_enc", nameEnc);
            }
            db.update(DbSchema.T_IDENTITIES, identity, "user_pid = ?", new String[]{userPid});

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Vrai si le consentement de cette finalité est actuellement "Given". */
    public boolean hasConsent(String userPid, String purpose) {
        Cursor c = helper.getReadableDatabase().rawQuery("SELECT 1 FROM " + DbSchema.T_CONSENTS
                        + " WHERE user_pid = ? AND consent_purpose = ? AND consent_status = ?",
                new String[]{userPid, purpose, AppConstants.STATUS_GIVEN});
        boolean given = c.moveToFirst();
        c.close();
        return given;
    }

    /** Vérifie que le profil existe toujours (sécurité si la base a été effacée). */
    public boolean userExists(String userPid) {
        Cursor c = helper.getReadableDatabase().rawQuery("SELECT 1 FROM " + DbSchema.T_USERS
                + " WHERE user_pid = ?", new String[]{userPid});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }
}
