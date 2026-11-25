package com.example.chifaa.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chifaa.R;
import com.example.chifaa.models.KbItem;
import com.example.chifaa.utils.AppConstants;
import com.example.chifaa.utils.LabelUtils;

import java.util.List;
import java.util.Set;

/** Liste des fiches de la bibliothèque. */
public class KbAdapter extends RecyclerView.Adapter<KbAdapter.Holder> {

    public interface OnItemClickListener {
        void onItemClick(KbItem item);
    }

    private final List<KbItem> items;
    private final Set<String> viewedIds;
    private final OnItemClickListener listener;

    public KbAdapter(List<KbItem> items, Set<String> viewedIds, OnItemClickListener listener) {
        this.items = items;
        this.viewedIds = viewedIds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_kb, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        final KbItem item = items.get(position);
        Context ctx = h.itemView.getContext();

        h.tvTopic.setText(LabelUtils.label(ctx, R.array.kb_topics, AppConstants.KB_TOPICS, item.topic));
        h.tvLanguage.setText(LabelUtils.label(ctx, R.array.content_languages,
                AppConstants.CONTENT_LANGUAGES, item.language));
        h.tvTitle.setText(item.title);
        h.tvExcerpt.setText(item.contentText);

        if (item.evidenceLevel != null) {
            h.tvEvidence.setVisibility(View.VISIBLE);
            h.tvEvidence.setText(ctx.getString(R.string.library_evidence_fmt, LabelUtils.label(ctx,
                    R.array.evidence_levels, AppConstants.EVIDENCE_LEVELS, item.evidenceLevel)));
        } else {
            h.tvEvidence.setVisibility(View.GONE);
        }

        h.ivViewed.setVisibility(viewedIds.contains(item.contentId) ? View.VISIBLE : View.GONE);
        h.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView tvTopic, tvLanguage, tvTitle, tvExcerpt, tvEvidence;
        final ImageView ivViewed;

        Holder(@NonNull View itemView) {
            super(itemView);
            tvTopic = itemView.findViewById(R.id.tvTopic);
            tvLanguage = itemView.findViewById(R.id.tvLanguage);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvExcerpt = itemView.findViewById(R.id.tvExcerpt);
            tvEvidence = itemView.findViewById(R.id.tvEvidence);
            ivViewed = itemView.findViewById(R.id.ivViewed);
        }
    }
}
