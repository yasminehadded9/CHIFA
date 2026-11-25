package com.example.chifaa.utils;

import android.content.Context;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Conversion "code stocké en base" -> "libellé affiché" (français ou arabe).
 * Exemple : side_effect "Fatigue" -> R.array.side_effects[0] -> "Fatigue" / "التعب".
 */
public final class LabelUtils {

    private LabelUtils() { }

    /** Libellé d'un code, ou le code lui-même s'il est inconnu. */
    public static String label(Context context, int arrayRes, String[] codes, String code) {
        int index = AppConstants.indexOf(codes, code);
        String[] labels = context.getResources().getStringArray(arrayRes);
        return (index >= 0 && index < labels.length) ? labels[index] : code;
    }

    /** "2026-11-25" -> date longue dans la langue de l'interface. */
    public static String longDate(String isoDate) {
        return formatIso(isoDate, DateFormat.getDateInstance(DateFormat.LONG));
    }

    /** "2026-11-25" -> date moyenne ("25 nov. 2026"). */
    public static String mediumDate(String isoDate) {
        return formatIso(isoDate, DateFormat.getDateInstance(DateFormat.MEDIUM));
    }

    /** "2026-11-25" -> "25/11" (axe du graphique). */
    public static String shortDayMonth(String isoDate) {
        return formatIso(isoDate, new SimpleDateFormat("dd/MM", Locale.getDefault()));
    }

    /** "2026-11-25T13:32:00Z" (UTC) -> "14:32" à l'heure locale du téléphone. */
    public static String timeOfDay(String isoDateTime) {
        return formatUtc(isoDateTime, DateFormat.getTimeInstance(DateFormat.SHORT));
    }

    /** "2026-11-25T13:32:00Z" (UTC) -> "25 nov. 2026" à l'heure locale. */
    public static String mediumDateTimeUtc(String isoDateTime) {
        return formatUtc(isoDateTime, DateFormat.getDateInstance(DateFormat.MEDIUM));
    }

    private static String formatUtc(String isoDateTime, DateFormat output) {
        if (isoDateTime == null) return "";
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            in.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            return output.format(in.parse(isoDateTime));
        } catch (Exception e) {
            return isoDateTime;
        }
    }

    private static String formatIso(String isoDate, DateFormat output) {
        if (isoDate == null) return "";
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDate);
            return output.format(date);
        } catch (Exception e) {
            return isoDate;
        }
    }
}
