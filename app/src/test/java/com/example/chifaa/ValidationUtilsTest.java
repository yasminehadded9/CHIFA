package com.example.chifaa;

import static org.junit.Assert.assertEquals;

import com.example.chifaa.utils.ValidationUtils;
import com.example.chifaa.utils.ValidationUtils.NameResult;
import com.example.chifaa.utils.ValidationUtils.OtpResult;
import com.example.chifaa.utils.ValidationUtils.PhoneResult;

import org.junit.Test;

/**
 * Tests du contrôle de saisie (clic droit sur la classe > Run 'ValidationUtilsTest').
 * Ils s'exécutent sur l'ordinateur, sans émulateur.
 */
public class ValidationUtilsTest {

    // ------------------------------------------------------------ Téléphone
    @Test
    public void telephoneValide() {
        assertEquals(PhoneResult.OK, ValidationUtils.validatePhone("98123456"));
        assertEquals(PhoneResult.OK, ValidationUtils.validatePhone("98 123 456"));
        assertEquals(PhoneResult.OK, ValidationUtils.validatePhone("+216 98 123 456"));
        assertEquals(PhoneResult.OK, ValidationUtils.validatePhone("0021622123456"));
        assertEquals(PhoneResult.OK, ValidationUtils.validatePhone("71-123-456"));
    }

    @Test
    public void telephoneVide() {
        assertEquals(PhoneResult.EMPTY, ValidationUtils.validatePhone(""));
        assertEquals(PhoneResult.EMPTY, ValidationUtils.validatePhone("   "));
        assertEquals(PhoneResult.EMPTY, ValidationUtils.validatePhone(null));
    }

    @Test
    public void telephoneMauvaiseLongueur() {
        assertEquals(PhoneResult.WRONG_LENGTH, ValidationUtils.validatePhone("9812345"));
        assertEquals(PhoneResult.WRONG_LENGTH, ValidationUtils.validatePhone("981234567"));
    }

    @Test
    public void telephoneMauvaisPrefixe() {
        assertEquals(PhoneResult.BAD_PREFIX, ValidationUtils.validatePhone("18123456"));
        assertEquals(PhoneResult.BAD_PREFIX, ValidationUtils.validatePhone("08123456"));
    }

    @Test
    public void telephoneCaracteresInterdits() {
        assertEquals(PhoneResult.INVALID_CHARS, ValidationUtils.validatePhone("98a23456"));
    }

    @Test
    public void formatageTelephone() {
        assertEquals("98 123 456", ValidationUtils.formatPhone("98123456"));
    }

    // --------------------------------------------------------------- Surnom
    @Test
    public void surnomFacultatif() {
        assertEquals(NameResult.EMPTY_OPTIONAL, ValidationUtils.validateDisplayName(""));
        assertEquals(NameResult.EMPTY_OPTIONAL, ValidationUtils.validateDisplayName("   "));
    }

    @Test
    public void surnomValide() {
        assertEquals(NameResult.OK, ValidationUtils.validateDisplayName("Amira"));
        assertEquals(NameResult.OK, ValidationUtils.validateDisplayName("Lina-Rose"));
        assertEquals(NameResult.OK, ValidationUtils.validateDisplayName("Héla"));
        assertEquals(NameResult.OK, ValidationUtils.validateDisplayName("أمل"));
    }

    @Test
    public void surnomInvalide() {
        assertEquals(NameResult.TOO_SHORT, ValidationUtils.validateDisplayName("A"));
        assertEquals(NameResult.TOO_LONG,
                ValidationUtils.validateDisplayName("Abcdefghijklmnopqrstuvwxyzabcde"));
        assertEquals(NameResult.INVALID_CHARS, ValidationUtils.validateDisplayName("Amira2"));
        assertEquals(NameResult.INVALID_CHARS, ValidationUtils.validateDisplayName("-Amira"));
    }

    // ------------------------------------------------------ Année diagnostic
    @Test
    public void anneeDiagnostic() {
        assertEquals(ValidationUtils.YearResult.OK, ValidationUtils.validateDiagnosisYear("2024", 2026));
        assertEquals(ValidationUtils.YearResult.EMPTY, ValidationUtils.validateDiagnosisYear("", 2026));
        assertEquals(ValidationUtils.YearResult.NOT_A_NUMBER, ValidationUtils.validateDiagnosisYear("24", 2026));
        assertEquals(ValidationUtils.YearResult.OUT_OF_RANGE, ValidationUtils.validateDiagnosisYear("2030", 2026));
        assertEquals(ValidationUtils.YearResult.OUT_OF_RANGE, ValidationUtils.validateDiagnosisYear("1900", 2026));
    }

    @Test
    public void coherenceAnneeAnciennete() {
        assertEquals(true, ValidationUtils.isYearConsistentWithBand(2026, "<1 yr", 2026));
        assertEquals(false, ValidationUtils.isYearConsistentWithBand(2015, "<1 yr", 2026));
        assertEquals(true, ValidationUtils.isYearConsistentWithBand(2015, ">5 yrs", 2026));
        assertEquals(false, ValidationUtils.isYearConsistentWithBand(2025, ">5 yrs", 2026));
        assertEquals(true, ValidationUtils.isYearConsistentWithBand(2010, null, 2026));
    }

    // -------------------------------------------------------------- Bilans
    @Test
    public void dateBilan() {
        java.time.LocalDate today = java.time.LocalDate.of(2026, 11, 25);
        assertEquals(ValidationUtils.DateResult.OK, ValidationUtils.validateCheckinDate("2026-11-25", today));
        assertEquals(ValidationUtils.DateResult.FUTURE, ValidationUtils.validateCheckinDate("2026-11-26", today));
        assertEquals(ValidationUtils.DateResult.TOO_OLD, ValidationUtils.validateCheckinDate("2025-01-01", today));
        assertEquals(ValidationUtils.DateResult.EMPTY, ValidationUtils.validateCheckinDate("", today));
        assertEquals(ValidationUtils.DateResult.INVALID, ValidationUtils.validateCheckinDate("25/11/2026", today));
    }

    @Test
    public void rechercheMinimum() {
        assertEquals(true, ValidationUtils.isSearchUsable(""));
        assertEquals(false, ValidationUtils.isSearchUsable("a"));
        assertEquals(true, ValidationUtils.isSearchUsable("fa"));
    }

    // ------------------------------------------------------------- Annuaire
    @Test
    public void verificationAnnuaire() {
        java.time.LocalDate today = java.time.LocalDate.of(2026, 11, 25);
        assertEquals(false, ValidationUtils.isVerificationOutdated("2026-09-12", today));
        assertEquals(false, ValidationUtils.isVerificationOutdated("2025-11-25", today)); // pile 12 mois
        assertEquals(true, ValidationUtils.isVerificationOutdated("2025-05-10", today));
        assertEquals(true, ValidationUtils.isVerificationOutdated("pas une date", today));
    }

    @Test
    public void numeroAppel() {
        assertEquals("+21670000101", ValidationUtils.toDialNumber("70000101"));
        assertEquals("+21698123456", ValidationUtils.toDialNumber("98 123 456"));
        assertEquals(null, ValidationUtils.toDialNumber(null));
        assertEquals(null, ValidationUtils.toDialNumber("123"));
    }

    // ------------------------------------------------------------------ Code
    @Test
    public void codeSms() {
        assertEquals(OtpResult.OK, ValidationUtils.validateOtpFormat("123456"));
        assertEquals(OtpResult.EMPTY, ValidationUtils.validateOtpFormat(""));
        assertEquals(OtpResult.WRONG_FORMAT, ValidationUtils.validateOtpFormat("12345"));
        assertEquals(OtpResult.WRONG_FORMAT, ValidationUtils.validateOtpFormat("12a456"));
    }
}
