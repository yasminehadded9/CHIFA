package com.example.chifaa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chifaa.R;
import com.example.chifaa.adapters.KbAdapter;
import com.example.chifaa.database.KbDao;
import com.example.chifaa.models.KbItem;
import com.example.chifaa.utils.AppConstants;
import com.example.chifaa.utils.ReadHistory;
import com.example.chifaa.utils.SessionManager;
import com.example.chifaa.utils.UiUtils;
import com.example.chifaa.utils.ValidationUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Set;

/**
 * Bibliothèque de fiches validées (module M4).
 * Filtres combinables : mot-clé + sujet + langue + "déjà consultées".
 * La liste se met à jour à chaque changement de filtre.
 */
public class LibraryActivity extends AppCompatActivity implements KbAdapter.OnItemClickListener {

    /** Sujet IMPOSÉ (ex. "Fertility" depuis l'Espace santé) : la liste ne montre que ce sujet. */
    public static final String EXTRA_TOPIC = "topic";

    private KbDao dao;
    private ReadHistory history;

    private TextInputLayout tilSearch;
    private TextInputEditText etSearch;
    private ChipGroup chipsTopics, chipsLanguages;
    private Chip chipViewed;
    private TextView tvCount;
    private RecyclerView rv;
    private View emptyState;

    // Filtres en cours (null = tous)
    private String topicFilter = null;
    private String languageFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        dao = new KbDao(this);
        history = new ReadHistory(this, new SessionManager(this).getUserPid());

        tilSearch = findViewById(R.id.tilSearch);
        etSearch = findViewById(R.id.etSearch);
        chipsTopics = findViewById(R.id.chipsTopics);
        chipsLanguages = findViewById(R.id.chipsLanguages);
        chipViewed = findViewById(R.id.chipViewed);
        tvCount = findViewById(R.id.tvCount);
        rv = findViewById(R.id.rvItems);
        emptyState = findViewById(R.id.emptyState);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // Sujet imposé ? (règle M3 : "l'application lui propose les contenus correspondants")
        topicFilter = getIntent().getStringExtra(EXTRA_TOPIC);
        boolean topicLocked = topicFilter != null;

        if (topicLocked) {
            // Écran dédié : titre du sujet, pas de filtre de sujet (on ne peut pas en sortir)
            if (AppConstants.TOPIC_FERTILITY.equals(topicFilter)) {
                UiUtils.setupHeader(this, 0, R.string.library_fertility_title, R.string.library_fertility_subtitle);
            } else {
                UiUtils.setupHeader(this, 0, R.string.library_title, R.string.library_subtitle);
            }
            findViewById(R.id.scrollTopics).setVisibility(View.GONE);
        } else {
            UiUtils.setupHeader(this, 0, R.string.library_title, R.string.library_subtitle);
            creerFiltres(chipsTopics, R.string.library_filter_all, R.array.kb_topics, AppConstants.KB_TOPICS, null);
        }
        creerFiltres(chipsLanguages, R.string.library_filter_all_languages, R.array.content_languages,
                AppConstants.CONTENT_LANGUAGES, null);

        chipsTopics.setOnCheckedStateChangeListener((group, checkedIds) -> {
            topicFilter = codeChoisi(group);
            rafraichir();
        });
        chipsLanguages.setOnCheckedStateChangeListener((group, checkedIds) -> {
            languageFilter = codeChoisi(group);
            rafraichir();
        });
        chipViewed.setOnCheckedChangeListener((button, checked) -> rafraichir());

        // Recherche en direct, avec contrôle de la longueur minimale
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                rafraichir();
            }
        });
    }

    /** Rafraîchi au retour d'une fiche (badge "consultée"). */
    @Override
    protected void onResume() {
        super.onResume();
        rafraichir();
    }

    /**
     * Crée une puce "Tous" + une puce par valeur. Le tag de chaque puce contient
     * le code de la base (null pour "Tous").
     */
    private void creerFiltres(ChipGroup group, int allLabel, int labelsArray, String[] codes, String preselected) {
        Chip all = creerPuce(group, getString(allLabel), null);
        all.setChecked(preselected == null);

        String[] labels = getResources().getStringArray(labelsArray);
        for (int i = 0; i < codes.length; i++) {
            Chip chip = creerPuce(group, labels[i], codes[i]);
            chip.setChecked(codes[i].equals(preselected));
        }
    }

    private Chip creerPuce(ChipGroup group, String text, String code) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.item_filter_chip, group, false);
        chip.setId(View.generateViewId());
        chip.setText(text);
        chip.setTag(code);
        chip.setCheckedIconVisible(false);
        group.addView(chip);
        return chip;
    }

    private String codeChoisi(ChipGroup group) {
        Chip checked = group.findViewById(group.getCheckedChipId());
        return checked == null ? null : (String) checked.getTag();
    }

    /** Relance la recherche avec les filtres actuels. */
    private void rafraichir() {
        String keyword = etSearch.getText() == null ? "" : etSearch.getText().toString();

        // Contrôle de saisie : 1 caractère ne suffit pas (trop de résultats sans intérêt)
        if (!ValidationUtils.isSearchUsable(keyword)) {
            tilSearch.setError(getString(R.string.library_search_too_short));
            return;
        }
        tilSearch.setError(null);

        Set<String> viewed = history.getViewed();
        List<KbItem> items = dao.search(keyword, topicFilter, languageFilter,
                chipViewed.isChecked() ? viewed : null);

        rv.setAdapter(new KbAdapter(items, viewed, this));
        tvCount.setText(getString(R.string.library_count_fmt, items.size()));
        emptyState.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        rv.setVisibility(items.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onItemClick(KbItem item) {
        Intent intent = new Intent(this, ArticleActivity.class);
        intent.putExtra(ArticleActivity.EXTRA_CONTENT_ID, item.contentId);
        startActivity(intent);
    }

    /** Aucune fiche trouvée : on oriente vers l'annuaire des services. */
    public void ouvrirAnnuaire(View v) {
        startActivity(new Intent(this, DirectoryActivity.class));
    }
}
