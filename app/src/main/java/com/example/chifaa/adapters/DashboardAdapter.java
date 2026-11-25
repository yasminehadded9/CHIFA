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
import com.example.chifaa.models.DashboardTile;

import java.util.List;

/** Affiche les tuiles du tableau de bord dans une grille (RecyclerView + GridLayoutManager). */
public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.TileHolder> {

    /** L'activité est prévenue du clic sur une tuile. */
    public interface OnTileClickListener {
        void onTileClick(DashboardTile tile);
    }

    private final List<DashboardTile> tiles;
    private final OnTileClickListener listener;

    public DashboardAdapter(List<DashboardTile> tiles, OnTileClickListener listener) {
        this.tiles = tiles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TileHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_tile, parent, false);
        return new TileHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TileHolder holder, int position) {
        final DashboardTile tile = tiles.get(position);
        int color = ContextCompat.getColor(holder.itemView.getContext(), tile.getColorRes());

        holder.ivIcon.setImageResource(tile.getIconRes());
        holder.ivIcon.setImageTintList(ColorStateList.valueOf(color));
        holder.iconBg.setBackgroundTintList(
                ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 30)));
        holder.tvTitle.setText(tile.getTitleRes());
        holder.tvSubtitle.setText(tile.getSubtitleRes());
        holder.tvSoon.setVisibility(tile.isAvailable() ? View.GONE : View.VISIBLE);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onTileClick(tile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tiles.size();
    }

    static class TileHolder extends RecyclerView.ViewHolder {
        final View iconBg;
        final ImageView ivIcon;
        final TextView tvTitle, tvSubtitle, tvSoon;

        TileHolder(@NonNull View itemView) {
            super(itemView);
            iconBg = itemView.findViewById(R.id.tileIconBg);
            ivIcon = itemView.findViewById(R.id.tileIcon);
            tvTitle = itemView.findViewById(R.id.tileTitle);
            tvSubtitle = itemView.findViewById(R.id.tileSubtitle);
            tvSoon = itemView.findViewById(R.id.tileSoon);
        }
    }
}
