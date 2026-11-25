package com.example.chifaa.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chifaa.R;
import com.example.chifaa.adapters.CheckinAdapter;
import com.example.chifaa.database.HealthDao;
import com.example.chifaa.models.Checkin;
import com.example.chifaa.models.HealthProfile;
import com.example.chifaa.utils.AppConstants;
import com.example.chifaa.utils.LabelUtils;
import com.example.chifaa.utils.SessionManager;
import com.example.chifaa.utils.UiUtils;
import com.example.chifaa.views.SideEffectsChartView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Espace santé (module M3). Accessible UNIQUEMENT avec le consentement santé.
 * Affiche : résumé du parcours, bilans (graphique + historique), fertilité.
 */
public class HealthActivity extends AppCompatActivity implements CheckinAdapter.OnCheckinLongClickListener {

    private static final int CHART_MAX_BARS = 7;

    private HealthDao dao;
    private String userPid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health);
        UiUtils.setupHeader(this, 0, R.string.health_title, R.string.health_subtitle);

        dao = new HealthDao(this);
        userPid = new SessionManager(this).getUserPid();

        // Garde-fou : sans consentement santé, on n'affiche rien
        if (userPid == null || !dao.hasHealthConsent(userPid)) {
            refuserAcces();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userPid == null || !dao.hasHealthConsent(userPid)) return;
        HealthProfile profile = dao.getProfile(userPid);
        afficherParcours(profile);
        afficherBilans(dao.getCheckins(userPid));
        afficherFertilite(profile);
    }

    private void refuserAcces() {
        new MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.ic_lock)
                .setTitle(R.string.health_no_consent_title)
                .setMessage(R.string.health_no_consent_message)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();
    }

    // ================================================================= Parcours

    private void afficherParcours(HealthProfile p) {
        LinearLayout summary = findViewById(R.id.journeySummary);
        TextView empty = findViewById(R.id.tvJourneyEmpty);
        TextView updated = findViewById(R.id.tvJourneyUpdated);
        MaterialButton btn = findViewById(R.id.btnJourney);

        summary.removeAllViews();
        if (p == null) {
            empty.setVisibility(View.VISIBLE);
            summary.setVisibility(View.GONE);
            updated.setVisibility(View.GONE);
            btn.setText(R.string.health_journey_fill);
            return;
        }

        empty.setVisibility(View.GONE);
        summary.setVisibility(View.VISIBLE);
        btn.setText(R.string.health_journey_edit);

        ajouterLigne(summary, R.string.health_label_cancer,
                libelleOuVide(R.array.cancer_types, AppConstants.CANCER_TYPES, p.cancerType));
        ajouterLigne(summary, R.string.health_label_phase,
                libelleOuVide(R.array.care_phases, AppConstants.CARE_PHASES, p.carePhase));

        String since = libelleOuVide(R.array.diagnosis_bands, AppConstants.DIAGNOSIS_BANDS, p.diagnosisPeriodBand);
        if (p.diagnosisYear != null) {
            since = getString(R.string.health_since_fmt, since, String.valueOf(p.diagnosisYear));
        }
        ajouterLigne(summary, R.string.health_label_since, since);

        StringBuilder treatments = new StringBuilder();
        for (String t : p.treatments) {
            if (treatments.length() > 0) treatments.append(", ");
            treatments.append(LabelUtils.label(this, R.array.treatment_types, AppConstants.TREATMENT_TYPES, t));
        }
        ajouterLigne(summary, R.string.health_label_treatments,
                treatments.length() == 0 ? getString(R.string.health_not_answered) : treatments.toString());

        if (p.updatedAt != null && p.updatedAt.length() >= 10) {
            updated.setText(getString(R.string.health_updated_fmt, LabelUtils.longDate(p.updatedAt.substring(0, 10))));
            updated.setVisibility(View.VISIBLE);
        }
    }

    private String libelleOuVide(int arrayRes, String[] codes, String code) {
        if (code == null) return getString(R.string.health_not_answered);
        return LabelUtils.label(this, arrayRes, codes, code);
    }

    /** Ajoute une ligne "Libellé  Valeur" au résumé. */
    private void ajouterLigne(LinearLayout parent, int labelRes, String value) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_summary_row, parent, false);
        ((TextView) row.findViewById(R.id.tvLabel)).setText(labelRes);
        ((TextView) row.findViewById(R.id.tvValue)).setText(value);
        parent.addView(row);
    }

    // =================================================================== Bilans

    private void afficherBilans(List<Checkin> checkins) {
        boolean empty = checkins.isEmpty();
        findViewById(R.id.tvCheckinsEmpty).setVisibility(empty ? View.VISIBLE : View.GONE);
        findViewById(R.id.chartContainer).setVisibility(empty ? View.GONE : View.VISIBLE);
        findViewById(R.id.tvDeleteHint).setVisibility(empty ? View.GONE : View.VISIBLE);

        RecyclerView rv = findViewById(R.id.rvCheckins);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new CheckinAdapter(checkins, this));

        // Graphique : les 7 derniers bilans, du plus ancien au plus récent
        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        int count = Math.min(CHART_MAX_BARS, checkins.size());
        for (int i = count - 1; i >= 0; i--) {
            Checkin c = checkins.get(i);
            labels.add(LabelUtils.shortDayMonth(c.date));
            values.add(c.sideEffects.size());
        }
        ((SideEffectsChartView) findViewById(R.id.chart)).setData(labels, values);
    }

    @Override
    public void onCheckinLongClick(final Checkin checkin) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.health_delete_title)
                .setMessage(getString(R.string.health_delete_message, LabelUtils.longDate(checkin.date)))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.health_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dao.deleteCheckin(userPid, checkin.checkinId);
                        Toast.makeText(HealthActivity.this, R.string.health_deleted, Toast.LENGTH_SHORT).show();
                        afficherBilans(dao.getCheckins(userPid));
                    }
                })
                .show();
    }

    // ================================================================ Fertilité

    private void afficherFertilite(HealthProfile p) {
        View card = findViewById(R.id.cardFertility);
        TextView text = findViewById(R.id.tvFertility);
        View button = findViewById(R.id.btnFertility);

        if (p != null && Boolean.TRUE.equals(p.fertilityInterest)) {
            card.setVisibility(View.VISIBLE);
            text.setText(R.string.health_fertility_yes);
            button.setVisibility(View.VISIBLE);
        } else if (p == null || p.fertilityInterest == null) {
            // Pas encore de réponse : on informe que l'option existe
            card.setVisibility(View.VISIBLE);
            text.setText(R.string.health_fertility_unknown);
            button.setVisibility(View.GONE);
        } else {
            card.setVisibility(View.GONE); // "Non" : on respecte ce choix
        }
    }

    // =============================================================== Navigation

    public void ouvrirParcours(View v) {
        startActivity(new Intent(this, JourneyActivity.class));
    }

    public void nouveauBilan(View v) {
        startActivity(new Intent(this, CheckinActivity.class));
    }

    public void ouvrirFichesFertilite(View v) {
        Intent intent = new Intent(this, LibraryActivity.class);
        intent.putExtra(LibraryActivity.EXTRA_TOPIC, AppConstants.TOPIC_FERTILITY);
        startActivity(intent);
    }
}
