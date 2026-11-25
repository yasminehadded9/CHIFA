package com.example.chifaa.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Dates au format ISO 8601 en UTC, comme l'exige le cahier des charges.
 *  - nowUtc()   -> "2026-11-25T11:35:00Z"  (colonnes *_at, horodatages)
 *  - todayUtc() -> "2026-11-25"            (colonnes *_date)
 */
public final class DateUtils {

    private DateUtils() { }

    public static String nowUtc() {
        return format("yyyy-MM-dd'T'HH:mm:ss'Z'", new Date());
    }

    public static String todayUtc() {
        return format("yyyy-MM-dd", new Date());
    }

    private static String format(String pattern, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }
}
