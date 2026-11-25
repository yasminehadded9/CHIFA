package com.example.chifaa.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chifaa.R;
import com.example.chifaa.database.HealthDao;
import com.example.chifaa.utils.AppConstants;
import com.example.chifaa.utils.DateUtils;
import com.example.chifaa.utils.LabelUtils;
import com.example.chifaa.utils.SessionManager;
import com.example.chifaa.utils.UiUtils;
import com.example.chifaa.utils.ValidationUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Nouveau bilan (M3) : date + effets secondaires ressentis.
 * Contrôles : date obligatoire, ni future ni de plus d'un an, un seul bilan par date,
 * au moins un effet coché OU "Aucun effet ressenti" (choix exclusif).
 */
public class CheckinActivity extends AppCompatActivity {

    private HealthDao dao;
    private String userPid;

    private TextInputLayout tilDate;
    private TextInputEditText etDate;
    private ChipGroup chipsEffects;
    private Chip chipNoEffect;
    private TextView tvEffectsError;

    /** Date choisie au format ISO "YYYY-MM-DD" (null = pas encore choisie). */
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkin);
        UiUtils.setupHeader(this, 0, R.string.checkin_title, R.string.checkin_subtitle);

        dao = new HealthDao(this);
        userPid = new SessionManager(this).getUserPid();
        if (userPid == null || !dao.hasHealthConsent(userPid)) {
            finish();
            return;
        }

        tilDate = findViewById(R.id.tilDate);
        etDate = findViewById(R.id.etDate);
        chipsEffects = findViewById(R.id.chipsEffects);
        chipNoEffect = findViewById(R.id.chipNoEffect);
        tvEffectsError = findViewById(R.id.tvEffectsError);

        // Date du jour proposée par défaut
        definirDate(DateUtils.todayUtc());

        creerPucesEffets();
        chipNoEffect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton button, boolean checked) {
                if (checked) {
                    tvEffectsError.setVisibility(View.GONE);
                    // "Aucun effet" décoche tous les effets
                    for (int i = 0; i < chipsEffects.getChildCount(); i++) {
                        ((Chip) chipsEffects.getChildAt(i)).setChecked(false);
                    }
                }
            }
        });
    }

    private void creerPucesEffets() {
        String[] labels = getResources().getStringArray(R.array.side_effects);
        for (int i = 0; i < labels.length; i++) {
            Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_filter_chip, chipsEffects, false);
            chip.setId(View.generateViewId());
            chip.setText(labels[i]);
            chip.setTag(AppConstants.SIDE_EFFECTS[i]); // code stocké en base
            chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton button, boolean checked) {
                    if (checked) {
                        tvEffectsError.setVisibility(View.GONE);
                        chipNoEffect.setChecked(false); // un effet coché annule "aucun effet"
                    }
                }
            });
            chipsEffects.addView(chip);
        }
    }

    // ===================================================================== Date

    /** Ouvre le calendrier : les dates futures sont désactivées. */
    public void choisirDate(View v) {
        CalendarConstraints constraints = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())
                .build();
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(R.string.checkin_date_picker_title)
                .setCalendarConstraints(constraints)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();
        picker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Long>() {
            @Override
            public void onPositiveButtonClick(Long selection) {
                // Le calendrier renvoie minuit UTC : on formate en UTC
                SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                iso.setTimeZone(TimeZone.getTimeZone("UTC"));
                definirDate(iso.format(new Date(selection)));
            }
        });
        picker.show(getSupportFragmentManager(), "checkin_date");
    }

    private void definirDate(String isoDate) {
        selectedDate = isoDate;
        etDate.setText(LabelUtils.longDate(isoDate));
        tilDate.setError(null);
    }

    // ============================================================ Enregistrement

    public void enregistrer(View v) {
        boolean valid = true;

        // 1. Date
        String dateError = null;
        switch (ValidationUtils.validateCheckinDate(selectedDate, LocalDate.now(ZoneOffset.UTC))) {
            case EMPTY:
            case INVALID:
                dateError = getString(R.string.error_checkin_date_empty);
                break;
            case FUTURE:
                dateError = getString(R.string.error_checkin_date_future);
                break;
            case TOO_OLD:
                dateError = getString(R.string.error_checkin_date_old);
                break;
            default:
                if (dao.checkinExists(userPid, selectedDate)) {
                    dateError = getString(R.string.error_checkin_exists);
                }
        }
        if (dateError != null) {
            UiUtils.showFieldError(tilDate, dateError);
            valid = false;
        }

        // 2. Effets
        List<String> effects = new ArrayList<>();
        for (int i = 0; i < chipsEffects.getChildCount(); i++) {
            Chip chip = (Chip) chipsEffects.getChildAt(i);
            if (chip.isChecked()) effects.add((String) chip.getTag());
        }
        if (effects.isEmpty() && !chipNoEffect.isChecked()) {
            tvEffectsError.setText(R.string.error_effects_required);
            tvEffectsError.setVisibility(View.VISIBLE);
            if (valid) UiUtils.shake(chipsEffects);
            valid = false;
        }

        if (!valid) return;

        // 3. Enregistrement (liste vide = "aucun effet ressenti")
        dao.addCheckin(userPid, selectedDate, effects);
        Toast.makeText(this, R.string.checkin_saved, Toast.LENGTH_SHORT).show();
        finish();
    }
}
