package com.example.chifaa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.chifaa.R;
import com.example.chifaa.adapters.OnboardingAdapter;
import com.example.chifaa.models.OnboardingItem;
import com.example.chifaa.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Présentation de l'application en 3 pages (affichée une seule fois).
 * Les boutons appellent passer() et suivant() via android:onClick (comme en TP).
 */
public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private LinearLayout dotsLayout;
    private MaterialButton btnNext;
    private TextView tvStep;
    private final List<OnboardingItem> pages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);
        btnNext = findViewById(R.id.btnNext);
        tvStep = findViewById(R.id.tvStep);

        // Les 3 pages : icône, titre, texte, couleur d'accent
        pages.add(new OnboardingItem(R.drawable.ic_chat,
                R.string.onb_title_1, R.string.onb_desc_1, R.color.brand_raspberry));
        pages.add(new OnboardingItem(R.drawable.ic_health,
                R.string.onb_title_2, R.string.onb_desc_2, R.color.brand_teal));
        pages.add(new OnboardingItem(R.drawable.ic_lock,
                R.string.onb_title_3, R.string.onb_desc_3, R.color.brand_plum));

        viewPager.setAdapter(new OnboardingAdapter(pages));
        creerIndicateurs();
        mettreAJour(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                mettreAJour(position);
            }
        });
    }

    /** Bouton "Suivant" : page suivante, ou fin de l'onboarding sur la dernière page. */
    public void suivant(View v) {
        int current = viewPager.getCurrentItem();
        if (current < pages.size() - 1) {
            viewPager.setCurrentItem(current + 1, true);
        } else {
            terminer();
        }
    }

    /** Bouton "Passer". */
    public void passer(View v) {
        terminer();
    }

    private void terminer() {
        new SessionManager(this).setOnboardingDone(true);
        startActivity(new Intent(this, WelcomeActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    /** Crée un petit indicateur (point) par page. */
    private void creerIndicateurs() {
        dotsLayout.removeAllViews();
        for (int i = 0; i < pages.size(); i++) {
            View dot = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(8), dp(8));
            params.setMargins(dp(4), 0, dp(4), 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.bg_dot_inactive);
            dotsLayout.addView(dot);
        }
    }

    /** Met à jour les points, le texte "Étape x sur 3" et le bouton principal. */
    private void mettreAJour(int position) {
        for (int i = 0; i < dotsLayout.getChildCount(); i++) {
            View dot = dotsLayout.getChildAt(i);
            boolean active = (i == position);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
            params.width = active ? dp(24) : dp(8);   // le point actif s'allonge
            dot.setLayoutParams(params);
            dot.setBackgroundResource(active ? R.drawable.bg_dot_active : R.drawable.bg_dot_inactive);
        }

        tvStep.setText(getString(R.string.onb_step, position + 1, pages.size()));

        boolean last = (position == pages.size() - 1);
        btnNext.setText(last ? R.string.onb_start : R.string.onb_next);
        // "Passer" n'a plus de sens sur la dernière page
        findViewById(R.id.btnSkip).setVisibility(last ? View.INVISIBLE : View.VISIBLE);
    }

    /** Conversion dp -> pixels. */
    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
