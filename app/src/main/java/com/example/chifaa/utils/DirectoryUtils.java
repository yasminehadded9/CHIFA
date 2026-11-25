package com.example.chifaa.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.example.chifaa.R;

/** Outils de l'annuaire partagés par la liste et la fiche d'un service. */
public final class DirectoryUtils {

    private DirectoryUtils() { }

    /** Icône associée à chaque type de service. */
    public static int iconFor(String serviceType) {
        if (serviceType == null) return R.drawable.ic_place;
        switch (serviceType) {
            case "Oncology":          return R.drawable.ic_health;
            case "Psychological":     return R.drawable.ic_chat;
            case "Legal":             return R.drawable.ic_shield;
            case "Fertility":         return R.drawable.ic_favorite;
            case "Financial":         return R.drawable.ic_wallet;
            case "Peer support":      return R.drawable.ic_groups;
            case "Prostheses & wigs": return R.drawable.ic_person;
            case "Transport":         return R.drawable.ic_car;
            case "CNAM assistance":   return R.drawable.ic_info;
            default:                  return R.drawable.ic_place;
        }
    }

    /**
     * Appel direct : intent implicite ACTION_DIAL (comme dans le TP "Intents implicites").
     * ACTION_DIAL ouvre le clavier du téléphone avec le numéro pré-rempli ;
     * l'utilisatrice confirme l'appel. Aucune permission CALL_PHONE n'est nécessaire.
     */
    public static void dial(Context context, String rawPhone) {
        String number = ValidationUtils.toDialNumber(rawPhone);
        if (number == null) {
            Toast.makeText(context, R.string.directory_no_phone, Toast.LENGTH_SHORT).show();
            return;
        }
        Uri numero = Uri.parse("tel:" + number);
        Intent intent = new Intent(Intent.ACTION_DIAL, numero);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.directory_dial_error, Toast.LENGTH_SHORT).show();
        }
    }

    /** Itinéraire : intent implicite "geo:" (comme testmaps() du TP), recherche par adresse. */
    public static void openMap(Context context, String address) {
        Uri adresse = Uri.parse("geo:0,0?q=" + Uri.encode(address));
        Intent intent = new Intent(Intent.ACTION_VIEW, adresse);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.org_maps_error, Toast.LENGTH_SHORT).show();
        }
    }
}
