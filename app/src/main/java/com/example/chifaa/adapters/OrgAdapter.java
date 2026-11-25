package com.example.chifaa.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chifaa.R;
import com.example.chifaa.models.Organisation;
import com.example.chifaa.utils.AppConstants;
import com.example.chifaa.utils.DirectoryUtils;
import com.example.chifaa.utils.LabelUtils;
import com.example.chifaa.utils.ValidationUtils;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/** Liste des services de l'annuaire, avec bouton d'appel direct sur chaque carte. */
public class OrgAdapter extends RecyclerView.Adapter<OrgAdapter.Holder> {

    public interface OnOrgClickListener {
        void onOrgClick(Organisation org);
    }

    private final List<Organisation> orgs;
    private final OnOrgClickListener listener;
    private final LocalDate today = LocalDate.now(ZoneOffset.UTC);

    public OrgAdapter(List<Organisation> orgs, OnOrgClickListener listener) {
        this.orgs = orgs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_org, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        final Organisation org = orgs.get(position);
        final Context ctx = h.itemView.getContext();

        h.ivType.setImageResource(DirectoryUtils.iconFor(org.serviceType));
        h.tvType.setText(LabelUtils.label(ctx, R.array.service_types, AppConstants.SERVICE_TYPES, org.serviceType));
        h.tvName.setText(org.name);
        h.tvPlace.setText(lieuEtLangues(ctx, org));
        h.tvCost.setText(LabelUtils.label(ctx, R.array.costs, AppConstants.COSTS, org.cost));

        // Vérification : verte si récente, orange si plus de 12 mois
        boolean outdated = ValidationUtils.isVerificationOutdated(org.verifiedDate, today);
        String date = LabelUtils.mediumDate(org.verifiedDate);
        h.tvVerified.setText(ctx.getString(outdated ? R.string.directory_outdated_fmt
                : R.string.directory_verified_fmt, date));
        h.tvVerified.setTextColor(ContextCompat.getColor(ctx,
                outdated ? R.color.state_warning : R.color.state_success));

        // Appel direct (désactivé si pas de numéro valide)
        boolean canCall = ValidationUtils.toDialNumber(org.phone) != null;
        h.btnCall.setEnabled(canCall);
        h.btnCall.setText(canCall ? R.string.directory_call : R.string.directory_no_phone);
        h.btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DirectoryUtils.dial(ctx, org.phone);
            }
        });

        View.OnClickListener open = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onOrgClick(org);
            }
        };
        h.btnDetails.setOnClickListener(open);
        h.itemView.setOnClickListener(open);
    }

    /** "Sfax · Français, Arabe standard" */
    static String lieuEtLangues(Context ctx, Organisation org) {
        StringBuilder sb = new StringBuilder(LabelUtils.label(ctx, R.array.governorates,
                AppConstants.GOVERNORATE_CODES, org.governorate));
        for (int i = 0; i < org.languages.size(); i++) {
            sb.append(i == 0 ? " · " : ", ");
            sb.append(LabelUtils.label(ctx, R.array.content_languages,
                    AppConstants.CONTENT_LANGUAGES, org.languages.get(i)));
        }
        return sb.toString();
    }

    @Override
    public int getItemCount() {
        return orgs.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView ivType;
        final TextView tvType, tvName, tvPlace, tvCost, tvVerified;
        final MaterialButton btnCall, btnDetails;

        Holder(@NonNull View itemView) {
            super(itemView);
            ivType = itemView.findViewById(R.id.ivType);
            tvType = itemView.findViewById(R.id.tvType);
            tvName = itemView.findViewById(R.id.tvName);
            tvPlace = itemView.findViewById(R.id.tvPlace);
            tvCost = itemView.findViewById(R.id.tvCost);
            tvVerified = itemView.findViewById(R.id.tvVerified);
            btnCall = itemView.findViewById(R.id.btnCall);
            btnDetails = itemView.findViewById(R.id.btnDetails);
        }
    }
}
