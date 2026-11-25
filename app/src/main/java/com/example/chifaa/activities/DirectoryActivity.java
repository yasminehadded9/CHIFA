package com.example.chifaa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chifaa.R;
import com.example.chifaa.adapters.OrgAdapter;
import com.example.chifaa.database.AccountDao;
import com.example.chifaa.database.DirectoryDao;
import com.example.chifaa.models.Organisation;
import com.example.chifaa.models.UserProfile;
import com.example.chifaa.utils.AppConstants;
import com.example.chifaa.utils.SessionManager;
import com.example.chifaa.utils.UiUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.List;

/**
 * Annuaire des services (module M6) : recherche par gouvernorat et par type.
 * Par défaut, on affiche le gouvernorat de l'utilisatrice (renseigné dans son profil).
 */
public class DirectoryActivity extends AppCompatActivity implements OrgAdapter.OnOrgClickListener {

    /** Type de service présélectionné (ex. depuis le compagnon à l'étape 7). */
    public static final String EXTRA_SERVICE_TYPE = "service_type";

    private DirectoryDao dao;
    private MaterialAutoCompleteTextView acGovernorate;
    private ChipGroup chipsTypes;
    private TextView tvCount, tvEmptyText;
    private RecyclerView rv;
    private View emptyState, btnShowAll;

    // Filtres en cours (null = tous)
    private String governorateFilter = null;
    private String typeFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directory);
        UiUtils.setupHeader(this, 0, R.string.directory_title, R.string.directory_subtitle);

        dao = new DirectoryDao(this);
        acGovernorate = findViewById(R.id.acGovernorate);
        chipsTypes = findViewById(R.id.chipsTypes);
        tvCount = findViewById(R.id.tvCount);
        tvEmptyText = findViewById(R.id.tvEmptyText);
        rv = findViewById(R.id.rvOrgs);
        emptyState = findViewById(R.id.emptyState);
        btnShowAll = findViewById(R.id.btnShowAll);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // 1. Gouvernorat : "Tous" + les 24 ; présélection = gouvernorat du profil
        String[] labels = getResources().getStringArray(R.array.governorates);
        final String[] items = new String[labels.length + 1];
        items[0] = getString(R.string.directory_all_governorates);
        System.arraycopy(labels, 0, items, 1, labels.length);
        acGovernorate.setSimpleItems(items);

        governorateFilter = gouvernoratDuProfil();
        int selected = AppConstants.indexOf(AppConstants.GOVERNORATE_CODES, governorateFilter);
        acGovernorate.setText(items[selected + 1], false); // +1 à cause de "Tous"

        acGovernorate.setOnItemClickListener((parent, view, position, id) -> {
            governorateFilter = position == 0 ? null : AppConstants.GOVERNORATE_CODES[position - 1];
            rafraichir();
        });

        // 2. Type de service : "Tous" + 9 types
        typeFilter = getIntent().getStringExtra(EXTRA_SERVICE_TYPE);
        creerPuce(getString(R.string.directory_all_types), null).setChecked(typeFilter == null);
        String[] typeLabels = getResources().getStringArray(R.array.service_types);
        for (int i = 0; i < AppConstants.SERVICE_TYPES.length; i++) {
            Chip chip = creerPuce(typeLabels[i], AppConstants.SERVICE_TYPES[i]);
            chip.setChecked(AppConstants.SERVICE_TYPES[i].equals(typeFilter));
        }
        chipsTypes.setOnCheckedStateChangeListener((group, checkedIds) -> {
            Chip checked = group.findViewById(group.getCheckedChipId());
            typeFilter = checked == null ? null : (String) checked.getTag();
            rafraichir();
        });

        rafraichir();
    }

    private Chip creerPuce(String text, String code) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_filter_chip, chipsTypes, false);
        chip.setId(View.generateViewId());
        chip.setText(text);
        chip.setTag(code);
        chip.setCheckedIconVisible(false);
        chipsTypes.addView(chip);
        return chip;
    }

    /** Gouvernorat enregistré dans le profil (ou null si introuvable). */
    private String gouvernoratDuProfil() {
        String pid = new SessionManager(this).getUserPid();
        if (pid == null) return null;
        UserProfile profile = new AccountDao(this).getProfile(pid);
        return profile == null ? null : profile.governorate;
    }

    private void rafraichir() {
        List<Organisation> orgs = dao.search(governorateFilter, typeFilter);
        rv.setAdapter(new OrgAdapter(orgs, this));
        tvCount.setText(getString(R.string.directory_count_fmt, orgs.size()));

        boolean empty = orgs.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rv.setVisibility(empty ? View.GONE : View.VISIBLE);

        if (empty) {
            // On indique combien de services de ce type existent ailleurs
            int elsewhere = governorateFilter == null ? 0 : dao.countAll(typeFilter);
            if (elsewhere > 0) {
                tvEmptyText.setText(getString(R.string.directory_empty_text) + "\n"
                        + getString(R.string.directory_empty_elsewhere_fmt, elsewhere));
                btnShowAll.setVisibility(View.VISIBLE);
            } else {
                tvEmptyText.setText(R.string.directory_empty_text);
                btnShowAll.setVisibility(View.GONE);
            }
        }
    }

    /** Bouton de l'état vide : élargir à toute la Tunisie. */
    public void voirTousGouvernorats(View v) {
        governorateFilter = null;
        acGovernorate.setText(getString(R.string.directory_all_governorates), false);
        rafraichir();
    }

    @Override
    public void onOrgClick(Organisation org) {
        Intent intent = new Intent(this, OrgDetailActivity.class);
        intent.putExtra(OrgDetailActivity.EXTRA_ORG_ID, org.orgId);
        startActivity(intent);
    }
}
