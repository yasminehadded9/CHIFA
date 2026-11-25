package com.example.chifaa.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.example.chifaa.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Graphique en barres simple, dessiné avec Canvas (sans bibliothèque externe) :
 * nombre d'effets ressentis par bilan. Utilisé dans l'Espace santé.
 */
public class SideEffectsChartView extends View {

    private final List<String> labels = new ArrayList<>();
    private final List<Integer> values = new ArrayList<>();

    private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint emptyBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final float density;

    public SideEffectsChartView(Context context) {
        this(context, null);
    }

    public SideEffectsChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;

        barPaint.setColor(ContextCompat.getColor(context, R.color.brand_raspberry));
        emptyBarPaint.setColor(ContextCompat.getColor(context, R.color.brand_teal));

        labelPaint.setColor(ContextCompat.getColor(context, R.color.text_secondary));
        labelPaint.setTextSize(11 * getResources().getDisplayMetrics().scaledDensity);
        labelPaint.setTextAlign(Paint.Align.CENTER);

        valuePaint.setColor(ContextCompat.getColor(context, R.color.text_primary));
        valuePaint.setTextSize(12 * getResources().getDisplayMetrics().scaledDensity);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setFakeBoldText(true);

        gridPaint.setColor(ContextCompat.getColor(context, R.color.divider));
        gridPaint.setStrokeWidth(density);
    }

    /** Données dans l'ordre chronologique (du plus ancien au plus récent). */
    public void setData(List<String> newLabels, List<Integer> newValues) {
        labels.clear();
        labels.addAll(newLabels);
        values.clear();
        values.addAll(newValues);
        setContentDescription(buildDescription());
        invalidate(); // redessiner
    }

    /** Description lue par les lecteurs d'écran (accessibilité). */
    private String buildDescription() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < labels.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(labels.get(i)).append(" : ").append(values.get(i));
        }
        return sb.toString();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int n = values.size();
        if (n == 0) return;

        float labelZone = 22 * density;
        float valueZone = 18 * density;
        float chartTop = getPaddingTop() + valueZone;
        float chartBottom = getHeight() - getPaddingBottom() - labelZone;
        float chartHeight = chartBottom - chartTop;
        float left = getPaddingLeft();
        float width = getWidth() - getPaddingLeft() - getPaddingRight();

        int max = 1;
        for (int v : values) max = Math.max(max, v);

        // Ligne de base
        canvas.drawLine(left, chartBottom, left + width, chartBottom, gridPaint);

        float slot = width / n;
        float barWidth = Math.min(slot * 0.55f, 36 * density);
        float radius = 8 * density;
        boolean rtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;

        for (int i = 0; i < n; i++) {
            int index = rtl ? n - 1 - i : i; // en arabe, le plus récent est à gauche
            int value = values.get(index);
            float cx = left + slot * i + slot / 2f;

            // Une barre "aucun effet" reste visible (petite, couleur verte)
            float barHeight = value == 0 ? 6 * density : chartHeight * value / max;
            rect.set(cx - barWidth / 2f, chartBottom - barHeight, cx + barWidth / 2f, chartBottom);
            canvas.drawRoundRect(rect, radius, radius, value == 0 ? emptyBarPaint : barPaint);

            canvas.drawText(String.valueOf(value), cx, chartBottom - barHeight - 6 * density, valuePaint);
            canvas.drawText(labels.get(index), cx, chartBottom + 16 * density, labelPaint);
        }
    }
}
