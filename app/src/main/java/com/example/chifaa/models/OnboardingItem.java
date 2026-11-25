package com.example.chifaa.models;

/** Une page de présentation : icône, titre, description et couleur d'accent. */
public class OnboardingItem {

    private final int iconRes;
    private final int titleRes;
    private final int descriptionRes;
    private final int colorRes;

    public OnboardingItem(int iconRes, int titleRes, int descriptionRes, int colorRes) {
        this.iconRes = iconRes;
        this.titleRes = titleRes;
        this.descriptionRes = descriptionRes;
        this.colorRes = colorRes;
    }

    public int getIconRes() { return iconRes; }

    public int getTitleRes() { return titleRes; }

    public int getDescriptionRes() { return descriptionRes; }

    public int getColorRes() { return colorRes; }
}
