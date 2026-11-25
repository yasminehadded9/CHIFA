package com.example.chifaa.utils;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hachage des mots de passe du personnel (table staff), utilisé par le back-office.
 * PBKDF2-HMAC-SHA256, 120 000 itérations, sel aléatoire de 16 octets.
 * Format stocké : "pbkdf2$<itérations>$<sel base64>$<hash base64>".
 * (Le cahier des charges cite bcrypt/Argon2 ; PBKDF2 est l'équivalent disponible
 * nativement en Java sur Android, sans bibliothèque externe.)
 */
public final class PasswordUtils {

    private PasswordUtils() { }

    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;

    public static String hash(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS);
        return "pbkdf2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt)
                + "$" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verify(String password, String stored) {
        try {
            String[] parts = stored.split("\\$");
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = pbkdf2(password.toCharArray(), salt, iterations);
            // Comparaison en temps constant
            int diff = expected.length ^ actual.length;
            for (int i = 0; i < Math.min(expected.length, actual.length); i++) {
                diff |= expected[i] ^ actual[i];
            }
            return diff == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        try {
            KeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_LENGTH);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("PBKDF2 indisponible", e);
        }
    }
}
