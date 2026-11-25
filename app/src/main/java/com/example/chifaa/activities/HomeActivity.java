package com.example.chifaa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chifaa.R;
import com.example.chifaa.adapters.DashboardAdapter;
import com.example.chifaa.database.AccountDao;
import com.example.chifaa.models.DashboardTile;
import com.example.chifaa.utils.AppConstants;
import com.example.chifaa.utils.SessionManager;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Tableau de bord : point d'entrée vers tous les modules.
 * La tuile "Espace santé" n'apparaît QUE si le consentement santé est donné (règle M3).
 */
public class HomeActivity extends AppCompatActivity implements DashboardAdapter.OnTileClickListener {

    public static final String EXTRA_NEW_ACCOUNT = "new_account";

    private SessionManager session;
    private AccountDao dao;
    private String userPid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        session = new SessionManager(this);
        dao = new AccountDao(this);
        userPid = session.getUserPid();

        // Sécurité : session absente ou compte introuvable -> retour à l'accueil
        if (userPid == null || !dao.userExists(userPid)) {
            session.logout();
            ouvrirAccueil();
            return;
        }

        if (getIntent().getBooleanExtra(EXTRA_NEW_ACCOUNT, false)) {
            findViewById(R.id.bannerNewAccount).setVisibility(View.VISIBLE);
        }

        // Date du jour, dans la langue de l'interface
        String today = DateFormat.getDateInstance(DateFormat.FULL).format(new Date());
        ((TextView) findViewById(R.id.tvDate)).setText(today);
    }

    /** Rafraîchi à chaque retour (ex. après modification du profil). */
    @Override
    protected void onResume() {
        super.onResume();
        if (userPid == null) return;
        afficherSalutation();
        afficherTuiles();
    }

    private void afficherSalutation() {
        String name = dao.getDisplayName(userPid);
        TextView tvHello = findViewById(R.id.tvHello);
        TextView tvInitial = findViewById(R.id.tvAvatarInitial);
        View ribbon = findViewById(R.id.ivAvatarRibbon);

        if (name == null) {
            tvHello.setText(R.string.home_hello);
            tvInitial.setVisibility(View.GONE);
            ribbon.setVisibility(View.VISIBLE);
        } else {
            tvHello.setText(getString(R.string.home_hello_name, name));
            tvInitial.setText(name.substring(0, 1).toUpperCase());
            tvInitial.setVisibility(View.VISIBLE);
            ribbon.setVisibility(View.GONE);
        }
    }

    private void afficherTuiles() {
        List<DashboardTile> tiles = new ArrayList<>();
        tiles.add(new DashboardTile(DashboardTile.LIBRARY, R.drawable.ic_book,
                R.string.dash_library, R.string.dash_library_sub, R.color.brand_plum, true));
        if (dao.hasConsent(userPid, AppConstants.CONSENT_HEALTH)) {
            tiles.add(new DashboardTile(DashboardTile.HEALTH, R.drawable.ic_health,
                    R.string.dash_health, R.string.dash_health_sub, R.color.brand_raspberry, true));
        }
        tiles.add(new DashboardTile(DashboardTile.DIRECTORY, R.drawable.ic_place,
                R.string.dash_directory, R.string.dash_directory_sub, R.color.brand_teal, true));
        tiles.add(new DashboardTile(DashboardTile.MY_DATA, R.drawable.ic_lock,
                R.string.dash_my_data, R.string.dash_my_data_sub, R.color.brand_amber, false));
        tiles.add(new DashboardTile(DashboardTile.PROFILE, R.drawable.ic_person,
                R.string.dash_profile, R.string.dash_profile_sub, R.color.brand_plum_light, true));

        RecyclerView rv = findViewById(R.id.rvTiles);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        rv.setAdapter(new DashboardAdapter(tiles, this));
    }

    /** Clic sur une tuile. Les modules non encore développés affichent leur étape. */
    @Override
    public void onTileClick(DashboardTile tile) {
        switch (tile.getId()) {
            case DashboardTile.PROFILE:
                ouvrirProfil(null);
                break;
            case DashboardTile.HEALTH:
                startActivity(new Intent(this, HealthActivity.class));
                break;
            case DashboardTile.LIBRARY:
                startActivity(new Intent(this, LibraryActivity.class));
                break;
            case DashboardTile.DIRECTORY:
                startActivity(new Intent(this, DirectoryActivity.class));
                break;
            case DashboardTile.MY_DATA:
                Toast.makeText(this, R.string.soon_my_data, Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    /** Avatar ou tuile "Mon profil". */
    public void ouvrirProfil(View v) {
        startActivity(new Intent(this, ProfileActivity.class));
    }

    /** Carte "Besoin d'en parler ?". */
    public void ouvrirCompagnon(View v) {
        startActivity(new Intent(this, ChatActivity.class));
    }

    private void ouvrirAccueil() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
