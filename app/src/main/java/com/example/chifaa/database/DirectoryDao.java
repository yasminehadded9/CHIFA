package com.example.chifaa.database;

import android.content.Context;
import android.database.Cursor;

import com.example.chifaa.models.Organisation;

import java.util.ArrayList;
import java.util.List;

/**
 * Lecture de l'annuaire (module M6). Les données sont dans la base SQLite du
 * téléphone : l'annuaire reste consultable hors ligne.
 */
public class DirectoryDao {

    /**
     * Les langues d'un service sont regroupées en une seule colonne avec GROUP_CONCAT
     * (une seule requête au lieu d'une requête par service).
     */
    private static final String SELECT = "SELECT o.org_id, o.org_name, o.service_type, o.governorate, "
            + "o.contact_phone, o.address, o.cost, o.verified_date, o.verification_method, "
            + "GROUP_CONCAT(l.language) "
            + "FROM " + DbSchema.T_ORGANISATIONS + " o "
            + "LEFT JOIN " + DbSchema.T_ORG_LANGUAGES + " l ON l.org_id = o.org_id ";

    private final DatabaseHelper helper;

    public DirectoryDao(Context context) {
        helper = DatabaseHelper.getInstance(context);
    }

    /**
     * Recherche par gouvernorat et par type de service (null = pas de filtre).
     * Tri : services vérifiés le plus récemment en premier.
     */
    public List<Organisation> search(String governorate, String serviceType) {
        StringBuilder sql = new StringBuilder(SELECT).append("WHERE 1 = 1");
        List<String> args = new ArrayList<>();
        if (governorate != null) {
            sql.append(" AND o.governorate = ?");
            args.add(governorate);
        }
        if (serviceType != null) {
            sql.append(" AND o.service_type = ?");
            args.add(serviceType);
        }
        sql.append(" GROUP BY o.org_id ORDER BY o.verified_date DESC, o.org_name");

        Cursor c = helper.getReadableDatabase().rawQuery(sql.toString(), args.toArray(new String[0]));
        List<Organisation> result = new ArrayList<>();
        while (c.moveToNext()) {
            result.add(lire(c));
        }
        c.close();
        return result;
    }

    /** Nombre de services dans un gouvernorat (pour proposer d'élargir la recherche). */
    public int countAll(String serviceType) {
        return search(null, serviceType).size();
    }

    public Organisation getById(String orgId) {
        Cursor c = helper.getReadableDatabase().rawQuery(SELECT + "WHERE o.org_id = ? GROUP BY o.org_id",
                new String[]{orgId});
        Organisation org = c.moveToFirst() ? lire(c) : null;
        c.close();
        return org;
    }

    private Organisation lire(Cursor c) {
        Organisation o = new Organisation();
        o.orgId = c.getString(0);
        o.name = c.getString(1);
        o.serviceType = c.getString(2);
        o.governorate = c.getString(3);
        o.phone = c.getString(4);
        o.address = c.getString(5);
        o.cost = c.getString(6);
        o.verifiedDate = c.getString(7);
        o.verificationMethod = c.getString(8);
        if (!c.isNull(9)) {
            for (String language : c.getString(9).split(",")) {
                o.languages.add(language);
            }
        }
        return o;
    }
}
