package com.example.chifaa.database;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.example.chifaa.utils.PasswordUtils;

import java.util.UUID;

/**
 * Données de démonstration insérées à la création (ou à la mise à jour) de la base.
 * Tout est FICTIF (contrainte éthique du projet).
 */
public final class DbSeeder {

    private DbSeeder() { }

    /** Version 2 de la base : relecteur clinique de démonstration + fiches de la bibliothèque. */
    public static void seedLibrary(SQLiteDatabase db) {
        // 1. Relecteur clinique (clé étrangère kb_items.clinical_reviewer).
        //    Mot de passe aléatoire : compte inutilisable tant que le back-office
        //    (étape 9) ne l'a pas initialisé. Aucun mot de passe dans le code.
        ContentValues staff = new ContentValues();
        staff.put("staff_id", KbSeedData.REVIEWER);
        staff.put("login", "clinicien.demo");
        staff.put("password_hash", PasswordUtils.hash(UUID.randomUUID().toString()));
        staff.put("role", "Clinical reviewer");
        db.insertWithOnConflict(DbSchema.T_STAFF, null, staff, SQLiteDatabase.CONFLICT_IGNORE);

        // 2. Fiches
        for (String[] item : KbSeedData.ITEMS) {
            ContentValues v = new ContentValues();
            v.put("content_id", item[0]);
            v.put("topic", item[1]);
            v.put("language", item[2]);
            v.put("title", item[3]);
            v.put("content_text", item[4]);
            v.put("source_type", item[5]);
            v.put("source_reference", KbSeedData.SOURCE_REF);
            v.put("evidence_level", item[6]);
            v.put("local_applicability", item[7]);
            v.put("approval_status", item[8]);
            v.put("review_date", item[9]);
            v.put("expiry_date", item[10]);
            v.put("content_version", item[11]);
            v.put("superseded_by", item[12]);
            v.put("is_fictional", 1);
            if (item[9] != null) {
                v.put("clinical_reviewer", KbSeedData.REVIEWER);
            }
            db.insertWithOnConflict(DbSchema.T_KB_ITEMS, null, v, SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    /** Version 3 de la base : services de démonstration de l'annuaire (+ langues parlées). */
    public static void seedDirectory(SQLiteDatabase db) {
        for (String[] item : DirectorySeedData.ITEMS) {
            ContentValues v = new ContentValues();
            v.put("org_id", item[0]);
            v.put("org_name", item[1]);
            v.put("service_type", item[2]);
            v.put("governorate", item[3]);
            v.put("contact_phone", item[4]);
            v.put("address", item[5]);
            v.put("cost", item[6]);
            v.put("verified_date", item[7]);
            v.put("verification_method", item[8]);
            db.insertWithOnConflict(DbSchema.T_ORGANISATIONS, null, v, SQLiteDatabase.CONFLICT_IGNORE);

            // Langues parlées : une ligne par langue (table d'association)
            for (String language : item[9].split(",")) {
                ContentValues l = new ContentValues();
                l.put("org_id", item[0]);
                l.put("language", language.trim());
                db.insertWithOnConflict(DbSchema.T_ORG_LANGUAGES, null, l, SQLiteDatabase.CONFLICT_IGNORE);
            }
        }
    }
}
