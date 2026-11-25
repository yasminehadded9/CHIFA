package com.example.chifaa.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chifaa.R;
import com.example.chifaa.database.HealthDao;
import com.example.chifaa.models.HealthProfile;
import com.example.chifaa.utils.AppConstants;
import com.example.chifaa.utils.SessionManager;
import com.example.chifaa.utils.UiUtils;
import com.example.chifaa.utils.ValidationUtils;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Mon parcours (M3). Chaque question propose "Je préfère ne pas répondre" :
 *  - type de cancer, structure : ce choix est un code de la base ("Prefer not to say") ;
 *  - étape, ancienneté, année, fertilité : ce choix est enregistré comme NULL.
 * Chaque question doit recevoir une réponse (éventuellement "je préfère ne pas répondre").
 */
public class JourneyActivity extends AppCompatActivity {

    private HealthDao dao;
    private String userPid;

    private TextInputLayout tilCancer, tilPhase, tilBand, tilYear, tilFacility;
    private TextInputEditText etYear;
    private MaterialCheckBox cbYearSkip;
    private ChipGroup chipsTreatments;
    private RadioGroup rgFertility;
    private TextView tvTreatmentsError, tvFertilityError;

    // Positions choisies (-1 = pas de réponse)
    private int cancerIndex = -1, phaseIndex = -1, bandIndex = -1, facilityIndex = -1;

    // Dernière position des listes "étape" et "ancienneté" = "Je préfère ne pas répondre"
    private static final int PHASE_SKIP = AppConstants.CARE_PHASES.length;
    private static final int BAND_SKIP = AppConstants.DIAGNOSIS_BANDS.length;

    /** Index de "Prefer not to say" dans la liste des traitements (choix exclusif). */
    private static final int TREATMENT_SKIP =
            AppConstants.indexOf(AppConstants.TREATMENT_TYPES, AppConstants.PREFER_NOT_TO_SAY);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);
        UiUtils.setupHeader(this, 0, R.string.journey_title, R.string.journey_subtitle);

        dao = new HealthDao(this);
        userPid = new SessionManager(this).getUserPid();
        if (userPid == null || !dao.hasHealthConsent(userPid)) {
            finish(); // garde-fou : jamais d'accès sans consentement santé
            return;
        }

        tilCancer = findViewById(R.id.tilCancerType);
        tilPhase = findViewById(R.id.tilCarePhase);
        tilBand = findViewById(R.id.tilDiagnosisBand);
        tilYear = findViewById(R.id.tilDiagnosisYear);
        tilFacility = findViewById(R.id.tilFacility);
        etYear = findViewById(R.id.etDiagnosisYear);
        cbYearSkip = findViewById(R.id.cbYearSkip);
        chipsTreatments = findViewById(R.id.chipsTreatments);
        rgFertility = findViewById(R.id.rgFertility);
        tvTreatmentsError = findViewById(R.id.tvTreatmentsError);
        tvFertilityError = findViewById(R.id.tvFertilityError);

        creerPucesTraitements();
        brancherEcouteurs();
        preRemplir(dao.getProfile(userPid));
    }

    // ============================================================ Construction

    private void creerPucesTraitements() {
        String[] labels = getResources().getStringArray(R.array.treatment_types);
        for (int i = 0; i < labels.length; i++) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_filter_chip, chipsTreatments, false);
            chip.setId(View.generateViewId());
            chip.setText(labels[i]);
            chip.setTag(i); // position dans AppConstants.TREATMENT_TYPES
            final int index = i;
            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton button, boolean checked) {
                    if (checked) {
                        tvTreatmentsError.setVisibility(View.GONE);
                        appliquerExclusivite(index);
                    }
                }
            });
            chipsTreatments.addView(chip);
        }
    }

    /** "Je préfère ne pas répondre" ne peut pas être combiné avec un traitement. */
    private void appliquerExclusivite(int checkedIndex) {
        for (int i = 0; i < chipsTreatments.getChildCount(); i++) {
            Chip chip = (Chip) chipsTreatments.getChildAt(i);
            int index = (int) chip.getTag();
            boolean conflict = (checkedIndex == TREATMENT_SKIP) ? index != TREATMENT_SKIP : index == TREATMENT_SKIP;
            if (conflict && chip.isChecked()) {
                chip.setChecked(false);
            }
        }
    }

    private void brancherEcouteurs() {
        ((MaterialAutoCompleteTextView) findViewById(R.id.acCancerType)).setOnItemClickListener(
                (parent, view, position, id) -> { cancerIndex = position; tilCancer.setError(null); });
        ((MaterialAutoCompleteTextView) findViewById(R.id.acCarePhase)).setOnItemClickListener(
                (parent, view, position, id) -> { phaseIndex = position; tilPhase.setError(null); });
        ((MaterialAutoCompleteTextView) findViewById(R.id.acDiagnosisBand)).setOnItemClickListener(
                (parent, view, position, id) -> { bandIndex = position; tilBand.setError(null); tilYear.setError(null); });
        ((MaterialAutoCompleteTextView) findViewById(R.id.acFacility)).setOnItemClickListener(
                (parent, view, position, id) -> { facilityIndex = position; tilFacility.setError(null); });

        etYear.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tilYear.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        // Cocher "je préfère ne pas répondre" vide et désactive le champ année
        cbYearSkip.setOnCheckedChangeListener((button, checked) -> {
            if (checked) etYear.setText("");
            tilYear.setEnabled(!checked);
            tilYear.setError(null);
        });

        rgFertility.setOnCheckedChangeListener((group, checkedId) -> tvFertilityError.setVisibility(View.GONE));
    }

    /** Remplit le formulaire avec le parcours déjà enregistré. */
    private void preRemplir(HealthProfile p) {
        if (p == null) return;

        cancerIndex = AppConstants.indexOf(AppConstants.CANCER_TYPES, p.cancerType);
        phaseIndex = p.carePhase == null ? PHASE_SKIP : AppConstants.indexOf(AppConstants.CARE_PHASES, p.carePhase);
        bandIndex = p.diagnosisPeriodBand == null ? BAND_SKIP
                : AppConstants.indexOf(AppConstants.DIAGNOSIS_BANDS, p.diagnosisPeriodBand);
        facilityIndex = AppConstants.indexOf(AppConstants.FACILITY_TYPES, p.facilityType);

        choisir(R.id.acCancerType, R.array.cancer_types, cancerIndex);
        choisir(R.id.acCarePhase, R.array.care_phases, phaseIndex);
        choisir(R.id.acDiagnosisBand, R.array.diagnosis_bands, bandIndex);
        choisir(R.id.acFacility, R.array.facility_types, facilityIndex);

        if (p.diagnosisYear == null) {
            cbYearSkip.setChecked(true);
        } else {
            etYear.setText(String.valueOf(p.diagnosisYear));
        }

        for (int i = 0; i < chipsTreatments.getChildCount(); i++) {
            Chip chip = (Chip) chipsTreatments.getChildAt(i);
            String code = AppConstants.TREATMENT_TYPES[(int) chip.getTag()];
            chip.setChecked(p.treatments.contains(code));
        }

        if (p.fertilityInterest == null) rgFertility.check(R.id.rbFertilitySkip);
        else rgFertility.check(p.fertilityInterest ? R.id.rbFertilityYes : R.id.rbFertilityNo);
    }

    private void choisir(int viewId, int arrayRes, int index) {
        String[] labels = getResources().getStringArray(arrayRes);
        if (index >= 0 && index < labels.length) {
            ((MaterialAutoCompleteTextView) findViewById(viewId)).setText(labels[index], false);
        }
    }

    // ============================================================ Enregistrement

    public void enregistrer(View v) {
        View firstError = null;
        String choose = getString(R.string.error_choose_or_skip);

        // 1. Listes déroulantes : une réponse obligatoire (y compris "je préfère ne pas répondre")
        if (cancerIndex < 0) { tilCancer.setError(choose); firstError = tilCancer; }
        if (phaseIndex < 0) { tilPhase.setError(choose); if (firstError == null) firstError = tilPhase; }
        if (bandIndex < 0) { tilBand.setError(choose); if (firstError == null) firstError = tilBand; }

        // 2. Année : saisie valide OU case "je préfère ne pas répondre"
        Integer year = null;
        int currentYear = LocalDate.now().getYear();
        if (!cbYearSkip.isChecked()) {
            String raw = etYear.getText() == null ? "" : etYear.getText().toString();
            String yearError = null;
            switch (ValidationUtils.validateDiagnosisYear(raw, currentYear)) {
                case EMPTY:
                    yearError = getString(R.string.error_year_required);
                    break;
                case NOT_A_NUMBER:
                    yearError = getString(R.string.error_year_format);
                    break;
                case OUT_OF_RANGE:
                    yearError = getString(R.string.error_year_range, currentYear);
                    break;
                default:
                    year = Integer.parseInt(raw.trim());
                    String band = (bandIndex >= 0 && bandIndex < BAND_SKIP)
                            ? AppConstants.DIAGNOSIS_BANDS[bandIndex] : null;
                    if (!ValidationUtils.isYearConsistentWithBand(year, band, currentYear)) {
                        yearError = getString(R.string.error_year_band);
                    }
            }
            if (yearError != null) {
                tilYear.setError(yearError);
                if (firstError == null) firstError = tilYear;
            }
        }

        if (facilityIndex < 0) { tilFacility.setError(choose); if (firstError == null) firstError = tilFacility; }

        // 3. Traitements : au moins une puce
        List<String> treatments = new ArrayList<>();
        for (int i = 0; i < chipsTreatments.getChildCount(); i++) {
            Chip chip = (Chip) chipsTreatments.getChildAt(i);
            if (chip.isChecked()) treatments.add(AppConstants.TREATMENT_TYPES[(int) chip.getTag()]);
        }
        if (treatments.isEmpty()) {
            tvTreatmentsError.setText(R.string.error_treatments_required);
            tvTreatmentsError.setVisibility(View.VISIBLE);
            if (firstError == null) firstError = chipsTreatments;
        }

        // 4. Fertilité : un bouton radio doit être choisi
        int fertilityId = rgFertility.getCheckedRadioButtonId();
        if (fertilityId == -1) {
            tvFertilityError.setText(R.string.error_choose_or_skip);
            tvFertilityError.setVisibility(View.VISIBLE);
            if (firstError == null) firstError = rgFertility;
        }

        if (firstError != null) {
            UiUtils.shake(firstError);
            firstError.requestFocus();
            return;
        }

        // 5. Conversion des choix en codes de base, puis enregistrement
        HealthProfile p = new HealthProfile();
        p.userPid = userPid;
        p.cancerType = AppConstants.CANCER_TYPES[cancerIndex];
        p.carePhase = phaseIndex == PHASE_SKIP ? null : AppConstants.CARE_PHASES[phaseIndex];
        p.diagnosisPeriodBand = bandIndex == BAND_SKIP ? null : AppConstants.DIAGNOSIS_BANDS[bandIndex];
        p.diagnosisYear = year;
        p.facilityType = AppConstants.FACILITY_TYPES[facilityIndex];
        p.treatments = treatments;
        p.fertilityInterest = fertilityId == R.id.rbFertilityYes ? Boolean.TRUE
                : fertilityId == R.id.rbFertilityNo ? Boolean.FALSE : null;

        dao.saveProfile(p);
        Toast.makeText(this, R.string.journey_saved, Toast.LENGTH_SHORT).show();
        finish();
    }
}
