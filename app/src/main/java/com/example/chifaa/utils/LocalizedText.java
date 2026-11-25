package com.example.chifaa.utils;

import android.content.Context;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * Textes du compagnon dans la langue de la QUESTION (et non celle de l'interface).
 *
 * On crée un Context configuré sur une autre langue ; Android choisit alors
 * automatiquement le bon dossier de ressources :
 *   French -> res/values/          (français)
 *   MSA    -> res/values-ar/       (arabe standard)
 *   Darija -> res/values-ar-rTN/   (arabe de Tunisie = darija ; repli sur values-ar)
 */
public final class LocalizedText {

    private LocalizedText() { }

    public static Context forLanguage(Context base, String language) {
        Locale locale;
        if ("Darija".equals(language)) {
            locale = new Locale("ar", "TN");
        } else if ("MSA".equals(language)) {
            locale = new Locale("ar");
        } else if ("French".equals(language)) {
            locale = new Locale("fr");
        } else {
            return base; // langue inconnue : langue de l'interface
        }
        Configuration config = new Configuration(base.getResources().getConfiguration());
        config.setLocale(locale);
        return base.createConfigurationContext(config);
    }
}
