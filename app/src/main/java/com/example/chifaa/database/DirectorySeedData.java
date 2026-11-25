package com.example.chifaa.database;

/**
 * Services de DÉMONSTRATION de l'annuaire (module M6).
 *
 * ⚠ Données FICTIVES (contrainte éthique du projet) : noms, adresses et numéros
 * sont inventés pour tester l'application. Les numéros suivent le format
 * "70 000 1xx" et ne doivent pas être utilisés en dehors de la démonstration.
 * L'équipe CHIFAA fournira les vrais services vérifiés.
 *
 * Colonnes : org_id, org_name, service_type, governorate, contact_phone, address,
 *            cost, verified_date, verification_method, langues (séparées par ",")
 *
 * Quelques services ont une vérification de plus de 12 mois : l'application
 * affiche alors "Information à confirmer".
 */
public final class DirectorySeedData {

    private DirectorySeedData() { }

    public static final String[][] ITEMS = {
            // ------------------------------------------------------------ Tunis (TN-11)
            {"ORG-001", "Service d'oncologie – Hôpital de démonstration", "Oncology", "TN-11",
                    "70000101", "Rue de la Démo, Tunis", "Covered by CNAM", "2026-09-12", "Site visit", "French,MSA"},
            {"ORG-002", "Écoute et soutien psychologique – Espace Démo", "Psychological", "TN-11",
                    "70000102", "Avenue Fictive 12, Tunis", "Free", "2026-10-03", "Phone call", "Darija,French"},
            {"ORG-003", "Permanence juridique – Association fictive", "Legal", "TN-11",
                    "70000103", "Rue Exemple 4, Tunis", "Free", "2026-07-20", "Partner confirmation", "MSA,French"},
            {"ORG-004", "Atelier perruques et prothèses – Boutique Démo", "Prostheses & wigs", "TN-11",
                    "70000104", "Place Imaginaire, Tunis", "Paid", "2026-08-15", "Site visit", "Darija,French"},
            {"ORG-005", "Guichet d'aide CNAM – Bureau de démonstration", "CNAM assistance", "TN-11",
                    "70000105", "Boulevard Fictif, Tunis", "Free", "2025-05-10", "Phone call", "Darija,MSA"},

            // ----------------------------------------------------------- Ariana (TN-12)
            {"ORG-006", "Consultation de fertilité – Clinique Démo", "Fertility", "TN-12",
                    "70000106", "Cité Exemple, Ariana", "Paid", "2026-06-01", "Site visit", "French"},
            {"ORG-007", "Groupe de parole entre pairs – Ariana", "Peer support", "TN-12",
                    "70000107", "Maison des associations (démo), Ariana", "Free", "2026-09-28", "Partner confirmation", "Darija"},

            // -------------------------------------------------------- Ben Arous (TN-13)
            {"ORG-008", "Transport solidaire vers les soins – Ben Arous", "Transport", "TN-13",
                    "70000108", "Rue Fictive 9, Ben Arous", "Free", "2026-10-11", "Phone call", "Darija"},
            {"ORG-009", "Aide financière ponctuelle – Fonds Démo", "Financial", "TN-13",
                    "70000109", "Avenue Démo 22, Ben Arous", "Free", "2025-03-14", "Partner confirmation", "Darija,MSA"},

            // ---------------------------------------------------------- Nabeul (TN-21)
            {"ORG-010", "Soutien psychologique – Centre régional (démo)", "Psychological", "TN-21",
                    "70000110", "Route Exemple, Nabeul", "Covered by CNAM", "2026-05-19", "Site visit", "Darija,French"},
            {"ORG-011", "Association d'entraide – Cap Bon (démo)", "Peer support", "TN-21",
                    "70000111", "Rue Imaginaire 3, Nabeul", "Free", "2026-08-30", "Phone call", "Darija"},

            // --------------------------------------------------------- Bizerte (TN-23)
            {"ORG-012", "Unité d'oncologie – Hôpital régional (démo)", "Oncology", "TN-23",
                    "70000112", "Corniche Fictive, Bizerte", "Covered by CNAM", "2026-04-22", "Site visit", "MSA,French"},

            // ------------------------------------------------------------ Jendouba (TN-32)
            {"ORG-013", "Transport vers Tunis pour les séances – Démo", "Transport", "TN-32",
                    "70000113", "Gare routière (démo), Jendouba", "Paid", "2026-09-02", "Phone call", "Darija"},

            // -------------------------------------------------------------- Le Kef (TN-33)
            {"ORG-014", "Accompagnement CNAM – Le Kef (démo)", "CNAM assistance", "TN-33",
                    "70000114", "Avenue Démo, Le Kef", "Free", "2026-03-08", "Partner confirmation", "Darija,MSA"},

            // ---------------------------------------------------------- Kairouan (TN-41)
            {"ORG-015", "Service d'oncologie – Kairouan (démo)", "Oncology", "TN-41",
                    "70000115", "Rue Exemple, Kairouan", "Covered by CNAM", "2026-06-25", "Site visit", "MSA"},
            {"ORG-016", "Écoute psychologique par téléphone – Démo", "Psychological", "TN-41",
                    "70000116", "Service à distance", "Free", "2026-10-20", "Phone call", "Darija,MSA,French"},

            // ------------------------------------------------------------ Sousse (TN-51)
            {"ORG-017", "Centre d'oncologie du Sahel (démo)", "Oncology", "TN-51",
                    "70000117", "Boulevard Fictif, Sousse", "Covered by CNAM", "2026-09-09", "Site visit", "French,MSA"},
            {"ORG-018", "Préservation de la fertilité – Sousse (démo)", "Fertility", "TN-51",
                    "70000118", "Clinique Exemple, Sousse", "Paid", "2026-02-17", "Site visit", "French"},
            {"ORG-019", "Conseil juridique gratuit – Sousse (démo)", "Legal", "TN-51",
                    "70000119", "Rue Imaginaire 15, Sousse", "Free", "2025-01-30", "Phone call", "MSA,French"},

            // ---------------------------------------------------------- Monastir (TN-52)
            {"ORG-020", "Perruques solidaires – Monastir (démo)", "Prostheses & wigs", "TN-52",
                    "70000120", "Avenue Démo 7, Monastir", "Free", "2026-07-07", "Partner confirmation", "Darija,French"},

            // -------------------------------------------------------------- Sfax (TN-61)
            {"ORG-021", "Service d'oncologie – CHU de démonstration, Sfax", "Oncology", "TN-61",
                    "70000121", "Route Fictive km 1, Sfax", "Covered by CNAM", "2026-10-05", "Site visit", "MSA,French"},
            {"ORG-022", "Soutien psychologique – Sfax (démo)", "Psychological", "TN-61",
                    "70000122", "Rue Exemple 18, Sfax", "Covered by CNAM", "2026-08-21", "Phone call", "Darija,French"},
            {"ORG-023", "Groupe de femmes en rémission – Sfax (démo)", "Peer support", "TN-61",
                    "70000123", "Maison de la culture (démo), Sfax", "Free", "2026-09-14", "Partner confirmation", "Darija"},
            {"ORG-024", "Aide aux frais de traitement – Sfax (démo)", "Financial", "TN-61",
                    "70000124", "Avenue Imaginaire, Sfax", "Free", "2026-06-30", "Partner confirmation", "Darija,MSA"},
            {"ORG-025", "Guichet CNAM – Sfax (démo)", "CNAM assistance", "TN-61",
                    "70000125", "Boulevard Démo, Sfax", "Free", "2026-05-12", "Phone call", "Darija,MSA"},

            // ------------------------------------------------------------- Gafsa (TN-71)
            {"ORG-026", "Navette vers Sfax pour les soins – Gafsa (démo)", "Transport", "TN-71",
                    "70000126", "Place Fictive, Gafsa", "Free", "2026-04-04", "Phone call", "Darija"},

            // ------------------------------------------------------------- Gabès (TN-81)
            {"ORG-027", "Assistance juridique – Gabès (démo)", "Legal", "TN-81",
                    "70000127", "Rue Exemple 2, Gabès", "Unknown", "2026-01-15", "Partner confirmation", "MSA"},
            {"ORG-028", "Prothèses mammaires externes – Gabès (démo)", "Prostheses & wigs", "TN-81",
                    "70000128", "Avenue Démo, Gabès", "Paid", "2026-07-28", "Site visit", "Darija,French"},

            // ----------------------------------------------------------- Médenine (TN-82)
            {"ORG-029", "Fonds d'entraide du Sud (démo)", "Financial", "TN-82",
                    "70000129", "Rue Imaginaire, Médenine", "Free", "2026-08-08", "Phone call", "Darija"},
            {"ORG-030", "Consultation fertilité itinérante (démo)", "Fertility", "TN-82",
                    null, "Dates communiquées par l'équipe soignante", "Unknown", "2025-02-20", "Partner confirmation", "French,MSA"},
    };
}
