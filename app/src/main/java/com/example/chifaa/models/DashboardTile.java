package com.example.chifaa.models;

/** Une tuile du tableau de bord (un module de l'application). */
public class DashboardTile {

    // Identifiants des modules
    public static final int COMPANION = 1;
    public static final int LIBRARY = 2;
    public static final int HEALTH = 3;
    public static final int DIRECTORY = 4;
    public static final int MY_DATA = 5;
    public static final int PROFILE = 6;

    private final int id;
    private final int iconRes;
    private final int titleRes;
    private final int subtitleRes;
    private final int colorRes;
    private final boolean available;   // false = module prévu dans une étape suivante

    public DashboardTile(int id, int iconRes, int titleRes, int subtitleRes,
                         int colorRes, boolean available) {
        this.id = id;
        this.iconRes = iconRes;
        this.titleRes = titleRes;
        this.subtitleRes = subtitleRes;
        this.colorRes = colorRes;
        this.available = available;
    }

    public int getId() { return id; }

    public int getIconRes() { return iconRes; }

    public int getTitleRes() { return titleRes; }

    public int getSubtitleRes() { return subtitleRes; }

    public int getColorRes() { return colorRes; }

    public boolean isAvailable() { return available; }
}
