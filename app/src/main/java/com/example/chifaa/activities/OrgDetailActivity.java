package com.example.chifaa.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chifaa.R;
import com.example.chifaa.database.DirectoryDao;
import com.example.chifaa.models.Organisation;
import com.example.chifaa.utils.AppConstants;
import com.example.chifaa.utils.DirectoryUtils;
import com.example.chifaa.utils.LabelUtils;
import com.example.chifaa.utils.UiUtils;
import com.example.chifaa.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * Fiche d'un service (M6) : nom, adresse, téléphone, langues, coût, date de dernière
 * vérification. Boutons : appel direct (ACTION_DIAL) et itinéraire (geo:).
 */
public class OrgDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ORG_ID = "org_id";

    private Organisation org;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_detail);
        UiUtils.setupHeader(this, 0, R.string.org_title, R.string.org_title);
        findViewById(R.id.tvHeaderSubtitle).setVisibility(View.GONE);

        String orgId = getIntent().getStringExtra(EXTRA_ORG_ID);
        org = orgId == null ? null : new DirectoryDao(this).getById(orgId);
        if (org == null) {
            Toast.makeText(this, R.string.org_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        afficher();
    }

    private void afficher() {
        ((ImageView) findViewById(R.id.ivType)).setImageResource(DirectoryUtils.iconFor(org.serviceType));
        ((TextView) findViewById(R.id.tvType)).setText(
                LabelUtils.label(this, R.array.service_types, AppConstants.SERVICE_TYPES, org.serviceType));
        ((TextView) findViewById(R.id.tvName)).setText(org.name);

        boolean outdated = ValidationUtils.isVerificationOutdated(org.verifiedDate, LocalDate.now(ZoneOffset.UTC));
        findViewById(R.id.outdatedWarning).setVisibility(outdated ? View.VISIBLE : View.GONE);

        LinearLayout rows = findViewById(R.id.infoRows);
        rows.removeAllViews();

        String governorate = LabelUtils.label(this, R.array.governorates, AppConstants.GOVERNORATE_CODES, org.governorate);
        String address = org.address == null ? governorate : org.address + " (" + governorate + ")";
        ajouterLigne(rows, R.drawable.ic_place, R.string.org_address, address);

        String phone = ValidationUtils.toDialNumber(org.phone) == null
                ? getString(R.string.directory_no_phone)
                : "+216 " + ValidationUtils.formatPhone(org.phone);
        ajouterLigne(rows, R.drawable.ic_phone, R.string.org_phone, phone);

        StringBuilder languages = new StringBuilder();
        for (String l : org.languages) {
            if (languages.length() > 0) languages.append(", ");
            languages.append(LabelUtils.label(this, R.array.content_languages, AppConstants.CONTENT_LANGUAGES, l));
        }
        ajouterLigne(rows, R.drawable.ic_translate, R.string.org_languages, languages.toString());

        ajouterLigne(rows, R.drawable.ic_wallet, R.string.org_cost,
                LabelUtils.label(this, R.array.costs, AppConstants.COSTS, org.cost));

        String method = LabelUtils.label(this, R.array.verification_methods,
                AppConstants.VERIFICATION_METHODS, org.verificationMethod);
        ajouterLigne(rows, R.drawable.ic_shield, R.string.org_verification,
                getString(R.string.org_verification_fmt, LabelUtils.longDate(org.verifiedDate), method));

        // Le numéro s'affiche de gauche à droite même en arabe
        // (la ligne téléphone est la 2e ligne ajoutée)
        TextView phoneValue = rows.getChildAt(1).findViewById(R.id.tvValue);
        phoneValue.setTextDirection(View.TEXT_DIRECTION_LTR);

        MaterialButton btnCall = findViewById(R.id.btnCall);
        boolean canCall = ValidationUtils.toDialNumber(org.phone) != null;
        btnCall.setEnabled(canCall);
        btnCall.setText(canCall ? R.string.directory_call : R.string.directory_no_phone);
    }

    private void ajouterLigne(LinearLayout parent, int iconRes, int labelRes, String value) {
        View row = getLayoutInflater().inflate(R.layout.item_info_row, parent, false);
        ((ImageView) row.findViewById(R.id.ivIcon)).setImageResource(iconRes);
        ((TextView) row.findViewById(R.id.tvLabel)).setText(labelRes);
        ((TextView) row.findViewById(R.id.tvValue)).setText(value);
        parent.addView(row);
    }

    /** Bouton "Appeler" : ouvre le clavier téléphonique avec le numéro (ACTION_DIAL). */
    public void appeler(View v) {
        DirectoryUtils.dial(this, org.phone);
    }

    /** Bouton "Itinéraire" : ouvre l'application de cartes (geo:). */
    public void itineraire(View v) {
        String governorate = LabelUtils.label(this, R.array.governorates, AppConstants.GOVERNORATE_CODES, org.governorate);
        DirectoryUtils.openMap(this, (org.address == null ? "" : org.address + ", ") + governorate + ", Tunisie");
    }
}
