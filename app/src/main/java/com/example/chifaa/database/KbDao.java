package com.example.chifaa.database;

import android.content.Context;
import android.database.Cursor;

import com.example.chifaa.models.KbItem;
import com.example.chifaa.utils.AppConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Lecture de la bibliothèque (module M4).
 *
 * RÈGLE : seules les fiches au statut "Clinically approved", non expirées
 * et non remplacées par une nouvelle version sont visibles.
 */
public class KbDao {

    /** Condition SQL de visibilité, réutilisée par toutes les requêtes. */
    static final String VISIBLE =
            "approval_status = '" + AppConstants.APPROVED + "' "
                    + "AND (expiry_date IS NULL OR expiry_date >= date('now')) "
                    + "AND superseded_by IS NULL";

    private static final String COLUMNS = "content_id, title, topic, language, content_text, "
            + "source_type, source_reference, evidence_level, review_date, expiry_date, "
            + "content_version, is_fictional";

    private final DatabaseHelper helper;

    public KbDao(Context context) {
        helper = DatabaseHelper.getInstance(context);
    }

    /**
     * Recherche avec filtres combinables (null = pas de filtre).
     * @param keyword  mot-clé cherché dans le titre et le texte
     * @param topic    code de sujet (AppConstants.KB_TOPICS)
     * @param language code de langue (AppConstants.CONTENT_LANGUAGES)
     * @param onlyIds  limiter à ces fiches (ex. fiches déjà consultées)
     */
    public List<KbItem> search(String keyword, String topic, String language, Collection<String> onlyIds) {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM "
                + DbSchema.T_KB_ITEMS + " WHERE " + VISIBLE);
        List<String> args = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            // Requête paramétrée (?) : protège contre l'injection SQL
            sql.append(" AND (title LIKE ? OR content_text LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            args.add(like);
            args.add(like);
        }
        if (topic != null) {
            sql.append(" AND topic = ?");
            args.add(topic);
        }
        if (language != null) {
            sql.append(" AND language = ?");
            args.add(language);
        }
        if (onlyIds != null) {
            if (onlyIds.isEmpty()) return new ArrayList<>();
            sql.append(" AND content_id IN (");
            int i = 0;
            for (String id : onlyIds) {
                sql.append(i++ == 0 ? "?" : ", ?");
                args.add(id);
            }
            sql.append(")");
        }
        sql.append(" ORDER BY topic, title");

        Cursor c = helper.getReadableDatabase().rawQuery(sql.toString(), args.toArray(new String[0]));
        List<KbItem> items = new ArrayList<>();
        while (c.moveToNext()) {
            items.add(lire(c));
        }
        c.close();
        return items;
    }

    /** Une fiche visible, ou null si elle n'existe pas / n'est plus visible. */
    public KbItem getById(String contentId) {
        Cursor c = helper.getReadableDatabase().rawQuery("SELECT " + COLUMNS + " FROM "
                + DbSchema.T_KB_ITEMS + " WHERE content_id = ? AND " + VISIBLE, new String[]{contentId});
        KbItem item = c.moveToFirst() ? lire(c) : null;
        c.close();
        return item;
    }

    private KbItem lire(Cursor c) {
        KbItem k = new KbItem();
        k.contentId = c.getString(0);
        k.title = c.getString(1);
        k.topic = c.getString(2);
        k.language = c.getString(3);
        k.contentText = c.getString(4);
        k.sourceType = c.getString(5);
        k.sourceReference = c.getString(6);
        k.evidenceLevel = c.getString(7);
        k.reviewDate = c.getString(8);
        k.expiryDate = c.getString(9);
        k.contentVersion = c.getString(10);
        k.fictional = c.getInt(11) == 1;
        return k;
    }
}
