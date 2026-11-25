package com.example.chifaa.models;

import java.util.ArrayList;
import java.util.List;

/** Un bilan daté (health_checkins) et ses effets secondaires (checkin_side_effects). */
public class Checkin {

    public String checkinId;
    public String date;                              // "YYYY-MM-DD"
    public List<String> sideEffects = new ArrayList<>(); // codes AppConstants.SIDE_EFFECTS ; vide = aucun effet
}
