package com.example.chifaa.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chifaa.R;
import com.example.chifaa.models.ChatMessage;
import com.example.chifaa.utils.LabelUtils;
import com.example.chifaa.utils.LocalizedText;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Messages du chat, avec 3 types de vues :
 *  - bulle de l'utilisatrice ;
 *  - bulle du compagnon (bouton "Lire la fiche" / "Ouvrir l'annuaire" + note 1 à 5) ;
 *  - carte "ressources d'aide" (détresse).
 */
public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /** Actions remontées à l'activité. */
    public interface Listener {
        void onOpenCard(String contentId);
        void onOpenDirectory(String serviceType);
        void onRate(ChatMessage message, int rating);
    }

    private static final int TYPE_USER = 0;
    private static final int TYPE_AI = 1;
    private static final int TYPE_DISTRESS = 2;

    private final List<ChatMessage> messages;
    private final Listener listener;

    public ChatAdapter(List<ChatMessage> messages, Listener listener) {
        this.messages = messages;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage m = messages.get(position);
        if (m.isUser()) return TYPE_USER;
        if (ChatMessage.TAG_DISTRESS.equals(m.topicTag)) return TYPE_DISTRESS;
        return TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_USER) {
            return new UserHolder(inflater.inflate(R.layout.item_msg_user, parent, false));
        }
        if (viewType == TYPE_DISTRESS) {
            return new DistressHolder(inflater.inflate(R.layout.item_msg_distress, parent, false));
        }
        return new AiHolder(inflater.inflate(R.layout.item_msg_ai, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final ChatMessage m = messages.get(position);

        if (holder instanceof UserHolder) {
            UserHolder h = (UserHolder) holder;
            h.tvText.setText(m.text);
            h.tvTime.setText(LabelUtils.timeOfDay(m.timestamp));

        } else if (holder instanceof DistressHolder) {
            DistressHolder h = (DistressHolder) holder;
            // Carte rédigée dans la langue de la question
            Context lc = LocalizedText.forLanguage(h.itemView.getContext(), m.language);
            h.tvTitle.setText(lc.getString(R.string.chat_distress_title));
            h.tvText.setText(lc.getString(R.string.chat_distress_text));
            h.tvResource.setText(lc.getString(R.string.chat_distress_resource));
            h.tvEmergency.setText(lc.getString(R.string.chat_distress_emergency));
            h.btnSupport.setText(lc.getString(R.string.chat_distress_services));
            h.btnSupport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onOpenDirectory("Psychological");
                }
            });

        } else {
            bindAi((AiHolder) holder, m);
        }
    }

    private void bindAi(AiHolder h, final ChatMessage m) {
        Context ctx = h.itemView.getContext();
        Context lc = LocalizedText.forLanguage(ctx, m.language); // boutons dans la langue de la réponse
        h.tvText.setText(m.text);
        h.tvTime.setText(m.timestamp == null ? "" : LabelUtils.timeOfDay(m.timestamp));

        // Bouton d'action selon le type de réponse
        if (m.contentId != null) {
            h.btnAction.setVisibility(View.VISIBLE);
            h.btnAction.setText(lc.getString(R.string.chat_read_card));
            h.btnAction.setIconResource(R.drawable.ic_book);
            h.btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onOpenCard(m.contentId);
                }
            });
        } else if (ChatMessage.TAG_NO_MATCH.equals(m.topicTag)) {
            h.btnAction.setVisibility(View.VISIBLE);
            h.btnAction.setText(lc.getString(R.string.chat_open_directory));
            h.btnAction.setIconResource(R.drawable.ic_place);
            h.btnAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onOpenDirectory(null);
                }
            });
        } else {
            h.btnAction.setVisibility(View.GONE);
        }

        // Note de 1 à 5 (pas sur le message d'accueil, qui n'est pas enregistré)
        if (m.messageId == null) {
            h.ratingRow.setVisibility(View.GONE);
            return;
        }
        h.ratingRow.setVisibility(View.VISIBLE);
        h.tvRateLabel.setText(m.feedback == null ? ctx.getString(R.string.chat_rate_question)
                : ctx.getString(R.string.chat_rate_thanks_fmt, m.feedback));

        h.stars.removeAllViews();
        int size = Math.round(28 * ctx.getResources().getDisplayMetrics().density);
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            ImageButton star = new ImageButton(ctx);
            star.setLayoutParams(new LinearLayout.LayoutParams(size, size));
            star.setBackground(null);
            star.setPadding(2, 2, 2, 2);
            boolean filled = m.feedback != null && i <= m.feedback;
            star.setImageResource(filled ? R.drawable.ic_star : R.drawable.ic_star_border);
            star.setColorFilter(ContextCompat.getColor(ctx, filled ? R.color.brand_amber : R.color.text_hint));
            star.setContentDescription(ctx.getString(R.string.chat_rate_star_fmt, i));
            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onRate(m, rating);
                }
            });
            h.stars.addView(star);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ================================================================ Holders

    static class UserHolder extends RecyclerView.ViewHolder {
        final TextView tvText, tvTime;

        UserHolder(@NonNull View v) {
            super(v);
            tvText = v.findViewById(R.id.tvText);
            tvTime = v.findViewById(R.id.tvTime);
        }
    }

    static class AiHolder extends RecyclerView.ViewHolder {
        final TextView tvText, tvTime, tvRateLabel;
        final MaterialButton btnAction;
        final View ratingRow;
        final LinearLayout stars;

        AiHolder(@NonNull View v) {
            super(v);
            tvText = v.findViewById(R.id.tvText);
            tvTime = v.findViewById(R.id.tvTime);
            tvRateLabel = v.findViewById(R.id.tvRateLabel);
            btnAction = v.findViewById(R.id.btnAction);
            ratingRow = v.findViewById(R.id.ratingRow);
            stars = v.findViewById(R.id.stars);
        }
    }

    static class DistressHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle, tvText, tvResource, tvEmergency;
        final MaterialButton btnSupport;

        DistressHolder(@NonNull View v) {
            super(v);
            tvTitle = v.findViewById(R.id.tvDistressTitle);
            tvText = v.findViewById(R.id.tvDistressText);
            tvResource = v.findViewById(R.id.tvDistressResource);
            tvEmergency = v.findViewById(R.id.tvDistressEmergency);
            btnSupport = v.findViewById(R.id.btnSupport);
        }
    }
}
