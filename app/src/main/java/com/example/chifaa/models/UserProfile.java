package com.example.chifaa.models;

/** Profil de l'utilisatrice (table users + surnom déchiffré de identities). */
public class UserProfile {

    public String userPid;
    public String ageBand;            // code, ex. "30–39"
    public String governorate;        // code ISO, ex. "TN-31"
    public String preferredLanguage;  // code, ex. "French"
    public String registrationDate;   // "YYYY-MM-DD"
    public String displayName;        // null si non renseigné
}
