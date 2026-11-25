package com.example.chifaa.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Parcours de soins déclaré (tables health_profiles + health_profile_treatments).
 * Une valeur null signifie "Je préfère ne pas répondre" (ou non renseigné).
 */
public class HealthProfile {

    public String userPid;
    public String cancerType;          // CANCER_TYPES (contient déjà "Prefer not to say")
    public String carePhase;           // CARE_PHASES ou null
    public String diagnosisPeriodBand; // DIAGNOSIS_BANDS ou null
    public Integer diagnosisYear;      // null = pas de réponse
    public String facilityType;        // FACILITY_TYPES (contient "Prefer not to say")
    public Boolean fertilityInterest;  // true / false / null (pas de réponse)
    public List<String> treatments = new ArrayList<>();
    public String updatedAt;
}
