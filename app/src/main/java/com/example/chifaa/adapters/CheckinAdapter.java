package com.example.chifaa.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chifaa.R;
import com.example.chifaa.models.Checkin;
import com.example.chifaa.utils.AppConstants;
import com.example.chifaa.utils.LabelUtils;

import java.util.List;

/** Historique des bilans (liste). Appui long = demande de suppression. */
public class CheckinAdapter extends RecyclerView.Adapter<CheckinAdapter.Holder> {

    public interface OnCheckinLongClickListener {
        void onCheckinLongClick(Checkin checkin);
    }

    private final List<Checkin> checkins;
    private final OnCheckinLongClickListener listener;

    public CheckinAdapter(List<Checkin> checkins, OnCheckinLongClickListener listener) {
        this.checkins = checkins;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkin, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        final Checkin checkin = checkins.get(position);
        Context ctx = h.itemView.getContext();
        int count = checkin.sideEffects.size();

        h.tvDate.setText(LabelUtils.mediumDate(checkin.date));
        h.tvCount.setText(String.valueOf(count));

        if (count == 0) {
            h.tvEffects.setText(R.string.health_no_effect);
            h.tvCount.setBackgroundTintList(ContextCompat.getColorStateList(ctx, R.color.brand_teal_soft));
            h.tvCount.setTextColor(ContextCompat.getColor(ctx, R.color.brand_teal));
        } else {
            StringBuilder sb = new StringBuilder();
            for (String effect : checkin.sideEffects) {
                if (sb.length() > 0) sb.append(" · ");
                sb.append(LabelUtils.label(ctx, R.array.side_effects, AppConstants.SIDE_EFFECTS, effect));
            }
            h.tvEffects.setText(sb.toString());
            h.tvCount.setBackgroundTintList(ContextCompat.getColorStateList(ctx, R.color.brand_rose_soft));
            h.tvCount.setTextColor(ContextCompat.getColor(ctx, R.color.brand_raspberry));
        }

        h.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onCheckinLongClick(checkin);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return checkins.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView tvCount, tvDate, tvEffects;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvCount = itemView.findViewById(R.id.tvCount);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvEffects = itemView.findViewById(R.id.tvEffects);
        }
    }
}
