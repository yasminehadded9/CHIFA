package com.example.chifaa.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Point d'accès unique à la base SQLite "chifaa.db".
 *
 * - onConfigure() : active les clés étrangères (désactivées par défaut dans SQLite,
 *                   sans cela ON DELETE CASCADE / SET NULL ne fonctionnent pas).
 * - onCreate()    : crée les 21 tables, les déclencheurs et les index (premier lancement).
 * - onUpgrade()   : appelé quand DATABASE_VERSION augmente ; migrations incrémentales
 *                   qui conservent les comptes existants.
 *
 * On utilise un singleton (getInstance) pour éviter d'ouvrir plusieurs connexions.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "chifaa.db";
    public static final int DATABASE_VERSION = 3;

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String sql : DbSchema.CREATE_TABLES) {
            db.execSQL(sql);
        }
        for (String sql : DbSchema.CREATE_TRIGGERS) {
            db.execSQL(sql);
        }
        for (String sql : DbSchema.INDEXES) {
            db.execSQL(sql);
        }
        DbSeeder.seedLibrary(db);
        DbSeeder.seedDirectory(db);
    }

    /**
     * Migrations INCRÉMENTALES : on garde les comptes déjà créés.
     *   v1 -> v2 : ajout des fiches de démonstration de la bibliothèque ;
     *   v2 -> v3 : ajout des services de démonstration de l'annuaire.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            DbSeeder.seedLibrary(db);
        }
        if (oldVersion < 3) {
            DbSeeder.seedDirectory(db);
        }
    }

    /** Retour à une version plus ancienne (rare, en développement) : on reconstruit tout. */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (String table : DbSchema.DROP_ORDER) {
            db.execSQL("DROP TABLE IF EXISTS " + table);
        }
        onCreate(db);
    }

    /** Nombre de tables de l'application (sert à vérifier que la base est bien créée). */
    public int countTables() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM sqlite_master WHERE type = 'table' "
                        + "AND name NOT LIKE 'sqlite_%' AND name <> 'android_metadata'", null);
        int count = 0;
        if (c.moveToFirst()) {
            count = c.getInt(0);
        }
        c.close();
        return count;
    }
}
