package com.example.chifaa.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Préférences locales (SharedPreferences) :
 *  - onboarding déjà vu ou non ;
 *  - session : user_pid (pseudonyme) de l'utilisatrice connectée.
 * Aucune donnée identifiante (téléphone, nom) n'est stockée ici.
 */
public class SessionManager {

    private static final String PREFS_NAME = "chifaa_prefs";
    private static final String KEY_ONBOARDING_DONE = "onboarding_done";
    private static final String KEY_USER_PID = "user_pid";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // ------------------------------------------------------------ Onboarding
    public boolean isOnboardingDone() {
        return prefs.getBoolean(KEY_ONBOARDING_DONE, false);
    }

    public void setOnboardingDone(boolean done) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, done).apply();
    }

    // --------------------------------------------------------------- Session
    public void login(String userPid) {
        prefs.edit().putString(KEY_USER_PID, userPid).apply();
    }

    public void logout() {
        prefs.edit().remove(KEY_USER_PID).apply();
    }

    public boolean isLoggedIn() {
        return getUserPid() != null;
    }

    public String getUserPid() {
        return prefs.getString(KEY_USER_PID, null);
    }
}
