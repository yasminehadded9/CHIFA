package com.example.chifaa.utils;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

/**
 * Langue de l'INTERFACE (français ou arabe).
 *
 * À ne pas confondre avec "preferred_language" (table users), qui est la langue
 * dans laquelle l'utilisatrice préfère échanger avec le compagnon (Darija, MSA...).
 *
 * AppCompatDelegate.setApplicationLocales() :
 *  - charge les textes de res/values-ar/ quand on choisit "ar" ;
 *  - inverse automatiquement la mise en page (droite à gauche) ;
 *  - mémorise le choix (service AppLocalesMetadataHolderService du manifeste).
 */
public final class LocaleUtils {

    private LocaleUtils() { }

    public static final String FRENCH = "fr";
    public static final String ARABIC = "ar";

    public static void apply(String languageTag) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag));
    }

    /** Langue actuelle de l'interface ("fr" par défaut). */
    public static String current() {
        LocaleListCompat locales = AppCompatDelegate.getApplicationLocales();
        if (!locales.isEmpty() && locales.get(0) != null
                && ARABIC.equals(locales.get(0).getLanguage())) {
            return ARABIC;
        }
        return FRENCH;
    }
}
