package com.example.chifaa.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chifaa.R;
import com.example.chifaa.models.OnboardingItem;

import java.util.List;

/** Fournit les pages de l'onboarding au ViewPager2. */
public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.PageHolder> {

    private final List<OnboardingItem> items;

    public OnboardingAdapter(List<OnboardingItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding, parent, false);
        return new PageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder holder, int position) {
        OnboardingItem item = items.get(position);
        int color = ContextCompat.getColor(holder.itemView.getContext(), item.getColorRes());

        holder.ivIcon.setImageResource(item.getIconRes());
        holder.ivIcon.setImageTintList(ColorStateList.valueOf(color));
        // Mêmes teintes que l'icône, mais très transparentes (alpha sur 255)
        holder.circleOuter.setBackgroundTintList(
                ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 20)));
        holder.circleInner.setBackgroundTintList(
                ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 40)));

        holder.tvTitle.setText(item.getTitleRes());
        holder.tvDescription.setText(item.getDescriptionRes());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class PageHolder extends RecyclerView.ViewHolder {
        final View circleOuter;
        final View circleInner;
        final ImageView ivIcon;
        final TextView tvTitle;
        final TextView tvDescription;

        PageHolder(@NonNull View itemView) {
            super(itemView);
            circleOuter = itemView.findViewById(R.id.circleOuter);
            circleInner = itemView.findViewById(R.id.circleInner);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}
