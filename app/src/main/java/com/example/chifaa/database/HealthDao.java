package com.example.chifaa.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.chifaa.models.Checkin;
import com.example.chifaa.models.HealthProfile;
import com.example.chifaa.utils.AppConstants;
import com.example.chifaa.utils.DateUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Accès aux données de santé (module M3).
 *
 * RÈGLE DU CAHIER DES CHARGES : aucune lecture ni écriture de health_profiles
 * ou health_checkins sans consentement santé au statut "Given".
 * Chaque méthode publique vérifie donc ce consentement (garde-fou côté données,
 * en plus de l'écran qui est masqué).
 */
public class HealthDao {

    private final DatabaseHelper helper;

    public HealthDao(Context context) {
        helper = DatabaseHelper.getInstance(context);
    }

    // ======================================================== Consentement santé

    /** Version du consentement santé donné, ou null s'il n'est pas actif. */
    public String getHealthConsentVersion(String userPid) {
        Cursor c = helper.getReadableDatabase().rawQuery("SELECT consent_version FROM "
                        + DbSchema.T_CONSENTS + " WHERE user_pid = ? AND consent_purpose = ? "
                        + "AND consent_status = ? ORDER BY given_at DESC LIMIT 1",
                new String[]{userPid, AppConstants.CONSENT_HEALTH, AppConstants.STATUS_GIVEN});
        String version = c.moveToFirst() ? c.getString(0) : null;
        c.close();
        return version;
    }

    public boolean hasHealthConsent(String userPid) {
        return getHealthConsentVersion(userPid) != null;
    }

    private void requireConsent(String userPid) {
        if (!hasHealthConsent(userPid)) {
            throw new SecurityException("Consentement santé absent : accès refusé");
        }
    }

    // ================================================================ Parcours

    /** Parcours déclaré, ou null s'il n'a jamais été rempli. */
    public HealthProfile getProfile(String userPid) {
        requireConsent(userPid);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT cancer_type, care_phase, diagnosis_period_band, diagnosis_year, "
                + "facility_type, fertility_info_interest, updated_at FROM " + DbSchema.T_HEALTH_PROFILES
                + " WHERE user_pid = ?", new String[]{userPid});
        HealthProfile p = null;
        if (c.moveToFirst()) {
            p = new HealthProfile();
            p.userPid = userPid;
            p.cancerType = c.getString(0);
            p.carePhase = c.getString(1);
            p.diagnosisPeriodBand = c.getString(2);
            p.diagnosisYear = c.isNull(3) ? null : c.getInt(3);
            p.facilityType = c.getString(4);
            p.fertilityInterest = c.isNull(5) ? null : c.getInt(5) == 1;
            p.updatedAt = c.getString(6);
        }
        c.close();

        if (p != null) {
            Cursor t = db.rawQuery("SELECT treatment_type FROM " + DbSchema.T_HEALTH_TREATMENTS
                    + " WHERE user_pid = ?", new String[]{userPid});
            while (t.moveToNext()) {
                p.treatments.add(t.getString(0));
            }
            t.close();
        }
        return p;
    }

    /** Crée ou remplace le parcours, et ses traitements, en une transaction. */
    public void saveProfile(HealthProfile p) {
        String consentVersion = getHealthConsentVersion(p.userPid);
        if (consentVersion == null) {
            throw new SecurityException("Consentement santé absent : enregistrement refusé");
        }

        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues v = new ContentValues();
            v.put("user_pid", p.userPid);
            v.put("cancer_type", p.cancerType);
            v.put("care_phase", p.carePhase);
            v.put("diagnosis_period_band", p.diagnosisPeriodBand);
            v.put("diagnosis_year", p.diagnosisYear);
            v.put("facility_type", p.facilityType);
            v.put("fertility_info_interest", p.fertilityInterest == null ? null : (p.fertilityInterest ? 1 : 0));
            v.put("health_consent_version", consentVersion);
            v.put("updated_at", DateUtils.nowUtc());
            db.insertWithOnConflict(DbSchema.T_HEALTH_PROFILES, null, v, SQLiteDatabase.CONFLICT_REPLACE);

            // Traitements : on remplace la liste complète (table d'association)
            db.delete(DbSchema.T_HEALTH_TREATMENTS, "user_pid = ?", new String[]{p.userPid});
            for (String treatment : p.treatments) {
                ContentValues t = new ContentValues();
                t.put("user_pid", p.userPid);
                t.put("treatment_type", treatment);
                db.insertOrThrow(DbSchema.T_HEALTH_TREATMENTS, null, t);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // ================================================================== Bilans

    public boolean checkinExists(String userPid, String date) {
        requireConsent(userPid);
        Cursor c = helper.getReadableDatabase().rawQuery("SELECT 1 FROM " + DbSchema.T_HEALTH_CHECKINS
                + " WHERE user_pid = ? AND checkin_date = ?", new String[]{userPid, date});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    /** Ajoute un bilan ; une liste d'effets vide signifie "aucun effet ressenti". */
    public void addCheckin(String userPid, String date, List<String> sideEffects) {
        requireConsent(userPid);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        try {
            String checkinId = UUID.randomUUID().toString();
            ContentValues v = new ContentValues();
            v.put("checkin_id", checkinId);
            v.put("user_pid", userPid);
            v.put("checkin_date", date);
            db.insertOrThrow(DbSchema.T_HEALTH_CHECKINS, null, v);

            for (String effect : sideEffects) {
                ContentValues e = new ContentValues();
                e.put("checkin_id", checkinId);
                e.put("side_effect", effect);
                db.insertOrThrow(DbSchema.T_CHECKIN_SIDE_EFFECTS, null, e);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Historique des bilans, du plus récent au plus ancien (une seule requête avec jointure). */
    public List<Checkin> getCheckins(String userPid) {
        requireConsent(userPid);
        Cursor c = helper.getReadableDatabase().rawQuery(
                "SELECT h.checkin_id, h.checkin_date, s.side_effect FROM " + DbSchema.T_HEALTH_CHECKINS + " h "
                        + "LEFT JOIN " + DbSchema.T_CHECKIN_SIDE_EFFECTS + " s ON s.checkin_id = h.checkin_id "
                        + "WHERE h.user_pid = ? ORDER BY h.checkin_date DESC, s.side_effect",
                new String[]{userPid});

        Map<String, Checkin> byId = new LinkedHashMap<>(); // garde l'ordre de la requête
        while (c.moveToNext()) {
            String id = c.getString(0);
            Checkin checkin = byId.get(id);
            if (checkin == null) {
                checkin = new Checkin();
                checkin.checkinId = id;
                checkin.date = c.getString(1);
                byId.put(id, checkin);
            }
            if (!c.isNull(2)) {
                checkin.sideEffects.add(c.getString(2));
            }
        }
        c.close();
        return new ArrayList<>(byId.values());
    }

    public void deleteCheckin(String userPid, String checkinId) {
        requireConsent(userPid);
        // Les effets associés partent avec le bilan (ON DELETE CASCADE)
        helper.getWritableDatabase().delete(DbSchema.T_HEALTH_CHECKINS,
                "checkin_id = ? AND user_pid = ?", new String[]{checkinId, userPid});
    }
}
