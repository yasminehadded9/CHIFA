package com.example.chifaa.utils;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chifaa.R;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputLayout;

/** Petites fonctions d'interface partagées par les écrans. */
public final class UiUtils {

    private UiUtils() { }

    public static final int SIGNUP_TOTAL_STEPS = 5;

    /**
     * Configure l'en-tête commun (layout_step_header.xml).
     * @param step 0 pour masquer la progression (ex. écran de connexion)
     */
    public static void setupHeader(final AppCompatActivity activity, int step,
                                   int titleRes, int subtitleRes) {
        TextView tvStep = activity.findViewById(R.id.tvHeaderStep);
        LinearProgressIndicator progress = activity.findViewById(R.id.progressSteps);
        if (step > 0) {
            tvStep.setText(activity.getString(R.string.signup_step, step, SIGNUP_TOTAL_STEPS));
            progress.setMax(SIGNUP_TOTAL_STEPS);
            progress.setProgressCompat(step, false);
        } else {
            tvStep.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
        }
        ((TextView) activity.findViewById(R.id.tvHeaderTitle)).setText(titleRes);
        ((TextView) activity.findViewById(R.id.tvHeaderSubtitle)).setText(subtitleRes);

        // Flèche retour = même comportement que le bouton retour du téléphone
        activity.findViewById(R.id.btnHeaderBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    /** Petite secousse horizontale pour signaler une erreur. */
    public static void shake(View view) {
        view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.shake));
    }

    /** Efface le message d'erreur d'un champ dès que l'utilisatrice corrige sa saisie. */
    public static void clearErrorOnInput(final TextInputLayout layout) {
        if (layout.getEditText() == null) return;
        layout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (layout.getError() != null) {
                    layout.setError(null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    /** Affiche une erreur sous un champ, avec secousse et focus. */
    public static void showFieldError(TextInputLayout layout, String message) {
        layout.setError(message);
        shake(layout);
        layout.requestFocus();
    }

    public static void hideKeyboard(Activity activity) {
        View focus = activity.getCurrentFocus();
        if (focus != null) {
            InputMethodManager imm = (InputMethodManager)
                    activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }
    }
}
