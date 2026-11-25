package com.example.chifaa.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Fiches déjà consultées par l'utilisatrice (pour le filtre "Déjà consultées"
 * et le badge "disponible hors ligne").
 *
 * Toute la bibliothèque est stockée dans la base SQLite du téléphone : une fiche
 * consultée reste donc lisible sans réseau. On mémorise seulement les identifiants
 * (KB-0001...), par utilisatrice, sans aucune donnée personnelle.
 */
public class ReadHistory {

    private static final String PREFS = "chifaa_read_history";
    private final SharedPreferences prefs;
    private final String key;

    public ReadHistory(Context context, String userPid) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        key = "viewed_" + userPid;
    }

    public Set<String> getViewed() {
        // Copie : l'ensemble renvoyé par SharedPreferences ne doit pas être modifié
        return new HashSet<>(prefs.getStringSet(key, new HashSet<String>()));
    }

    public boolean isViewed(String contentId) {
        return getViewed().contains(contentId);
    }

    public void markViewed(String contentId) {
        Set<String> viewed = getViewed();
        if (viewed.add(contentId)) {
            prefs.edit().putStringSet(key, viewed).apply();
        }
    }
}
