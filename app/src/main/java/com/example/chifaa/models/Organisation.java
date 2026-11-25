package com.example.chifaa.models;

import java.util.ArrayList;
import java.util.List;

/** Un service de l'annuaire (organisations + organisation_languages). */
public class Organisation {

    public String orgId;
    public String name;
    public String serviceType;    // code AppConstants.SERVICE_TYPES
    public String governorate;    // code ISO, ex. "TN-61"
    public String phone;          // 8 chiffres, ou null
    public String address;
    public String cost;           // code AppConstants.COSTS
    public String verifiedDate;   // "YYYY-MM-DD"
    public String verificationMethod;
    public List<String> languages = new ArrayList<>(); // codes AppConstants.CONTENT_LANGUAGES
}
