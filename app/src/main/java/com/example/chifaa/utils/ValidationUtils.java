package com.example.chifaa.utils;

import java.util.regex.Pattern;

/**
 * Contrôle de saisie centralisé (Java pur, testé dans ValidationUtilsTest).
 *
 * Chaque méthode renvoie un code d'erreur (enum) et non un texte :
 * l'écran choisit ensuite le message dans strings.xml (traduisible).
 */
public final class ValidationUtils {

    private ValidationUtils() { }

    // =========================================================== Téléphone
    public enum PhoneResult { OK, EMPTY, INVALID_CHARS, WRONG_LENGTH, BAD_PREFIX }

    /** Premiers chiffres possibles d'un numéro tunisien (mobiles et fixes). */
    private static final String VALID_FIRST_DIGITS = "234579";

    /**
     * Normalise un numéro saisi : retire espaces, tirets, points, parenthèses
     * et l'indicatif +216 / 00216. "+216 98 123 456" -> "98123456".
     */
    public static String normalizePhone(String raw) {
        if (raw == null) return "";
        String s = raw.replaceAll("[\\s\\-.()]", "");
        if (s.startsWith("+216")) {
            s = s.substring(4);
        } else if (s.startsWith("00216")) {
            s = s.substring(5);
        }
        return s;
    }

    public static PhoneResult validatePhone(String raw) {
        String phone = normalizePhone(raw);
        if (phone.isEmpty()) return PhoneResult.EMPTY;
        if (!phone.matches("\\d+")) return PhoneResult.INVALID_CHARS;
        if (phone.length() != 8) return PhoneResult.WRONG_LENGTH;
        if (VALID_FIRST_DIGITS.indexOf(phone.charAt(0)) < 0) return PhoneResult.BAD_PREFIX;
        return PhoneResult.OK;
    }

    /** "98123456" -> "98 123 456" (affichage). */
    public static String formatPhone(String phone8) {
        if (phone8 == null || phone8.length() != 8) return phone8;
        return phone8.substring(0, 2) + " " + phone8.substring(2, 5) + " " + phone8.substring(5);
    }

    // ============================================================ Surnom
    public enum NameResult { OK, EMPTY_OPTIONAL, TOO_SHORT, TOO_LONG, INVALID_CHARS }

    public static final int NAME_MIN = 2;
    public static final int NAME_MAX = 30;

    /** Lettres (toutes langues, accents, arabe), espaces, tirets, apostrophes. */
    private static final Pattern NAME_PATTERN = Pattern.compile("^\\p{L}[\\p{L} '\\-]*$");

    /** Le surnom est facultatif : une saisie vide est acceptée. */
    public static NameResult validateDisplayName(String raw) {
        String name = raw == null ? "" : raw.trim().replaceAll("\\s+", " ");
        if (name.isEmpty()) return NameResult.EMPTY_OPTIONAL;
        if (name.length() < NAME_MIN) return NameResult.TOO_SHORT;
        if (name.length() > NAME_MAX) return NameResult.TOO_LONG;
        if (!NAME_PATTERN.matcher(name).matches()) return NameResult.INVALID_CHARS;
        return NameResult.OK;
    }

    // ======================================================= Code SMS (OTP)
    public enum OtpResult { OK, EMPTY, WRONG_FORMAT }

    public static final int OTP_LENGTH = 6;

    public static OtpResult validateOtpFormat(String raw) {
        String code = raw == null ? "" : raw.trim();
        if (code.isEmpty()) return OtpResult.EMPTY;
        if (code.length() != OTP_LENGTH || !code.matches("\\d+")) return OtpResult.WRONG_FORMAT;
        return OtpResult.OK;
    }

    // ================================================ Année du diagnostic (M3)
    public enum YearResult { OK, EMPTY, NOT_A_NUMBER, OUT_OF_RANGE }

    public static final int MIN_DIAGNOSIS_YEAR = 1950;

    public static YearResult validateDiagnosisYear(String raw, int currentYear) {
        String s = raw == null ? "" : raw.trim();
        if (s.isEmpty()) return YearResult.EMPTY;
        if (!s.matches("\\d{4}")) return YearResult.NOT_A_NUMBER;
        int year = Integer.parseInt(s);
        if (year < MIN_DIAGNOSIS_YEAR || year > currentYear) return YearResult.OUT_OF_RANGE;
        return YearResult.OK;
    }

    /**
     * Cohérence entre l'année et l'ancienneté déclarées (tolérance d'un an,
     * car l'utilisatrice ne connaît pas toujours le mois exact).
     * @param band code AppConstants.DIAGNOSIS_BANDS, ou null si non renseigné
     */
    public static boolean isYearConsistentWithBand(int year, String band, int currentYear) {
        if (band == null) return true;
        int years = currentYear - year;
        switch (band) {
            case "<1 yr":   return years <= 1;
            case "1–2 yrs": return years <= 3;
            case "3–5 yrs": return years >= 2 && years <= 6;
            case ">5 yrs":  return years >= 5;
            default:        return true;
        }
    }

    // =================================================== Date d'un bilan (M3)
    public enum DateResult { OK, EMPTY, INVALID, FUTURE, TOO_OLD }

    /** Un bilan ne peut pas être daté dans le futur, ni de plus d'un an. */
    public static DateResult validateCheckinDate(String isoDate, java.time.LocalDate today) {
        if (isoDate == null || isoDate.trim().isEmpty()) return DateResult.EMPTY;
        java.time.LocalDate date;
        try {
            date = java.time.LocalDate.parse(isoDate.trim());
        } catch (Exception e) {
            return DateResult.INVALID;
        }
        if (date.isAfter(today)) return DateResult.FUTURE;
        if (date.isBefore(today.minusYears(1))) return DateResult.TOO_OLD;
        return DateResult.OK;
    }

    // ================================================== Recherche (M4)
    public static final int SEARCH_MIN = 2;

    /** Vrai si le mot-clé est assez long pour lancer une recherche (vide = pas de filtre). */
    public static boolean isSearchUsable(String raw) {
        String s = raw == null ? "" : raw.trim();
        return s.isEmpty() || s.length() >= SEARCH_MIN;
    }

    // ===================================================== Annuaire (M6)
    public static final int VERIFICATION_MAX_MONTHS = 12;

    /** Vrai si la fiche n'a pas été vérifiée depuis plus de 12 mois (ou date invalide). */
    public static boolean isVerificationOutdated(String verifiedIso, java.time.LocalDate today) {
        try {
            java.time.LocalDate verified = java.time.LocalDate.parse(verifiedIso);
            return verified.isBefore(today.minusMonths(VERIFICATION_MAX_MONTHS));
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Numéro à composer pour l'intent ACTION_DIAL : "98123456" -> "+21698123456".
     * Renvoie null si le numéro est absent ou invalide (le bouton d'appel est alors désactivé).
     */
    public static String toDialNumber(String rawPhone) {
        if (validatePhone(rawPhone) != PhoneResult.OK) return null;
        return "+216" + normalizePhone(rawPhone);
    }

    // ===================================================== Listes fermées
    /** Vrai si l'index choisi dans une liste déroulante est valide. */
    public static boolean isValidChoice(int index, String[] values) {
        return index >= 0 && index < values.length;
    }
}
