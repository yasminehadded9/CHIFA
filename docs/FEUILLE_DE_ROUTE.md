# Feuille de route CHIFAA

| Étape | Contenu | Modules |
|---|---|---|
| **1** ✅ | Fondations : design system, Splash, Onboarding, Accueil, schéma SQLite (21 tables) | — |
| **2** ✅ | Inscription : vérification d'âge, 3 consentements séparés, téléphone + code SMS simulé, pseudonyme UUID, téléphone chiffré + empreinte SHA-256, connexion | M1 |
| **3** ✅ | Tableau de bord + Profil (tranche d'âge, gouvernorat, langue) + interface FR/AR | M2 |
| **4** ✅ | Espace santé : parcours, bilans (cases à cocher + historique + graphique simple), fertilité | M3 |
| **5** ✅ | Bibliothèque de fiches validées : filtres, recherche, fiche détaillée, lecture hors ligne | M4 |
| **6** ✅ | Annuaire de services : recherche gouvernorat/type, fiche, appel direct (intent `ACTION_DIAL`) | M6 |
| **7** ✅ | Compagnon conversationnel : recherche dans les fiches, limites médicales, détection de détresse, avis 1–5 | M5 |
| 8 | Mes données : consentements (retrait), export JSON/CSV, demande de suppression | M7 |
| 9 | Back-office : connexion par rôle (mot de passe haché), validation des contenus, file de sécurité, annuaire | B1–B4 |
| 10 | Bonus et finitions : jeu éducatif, enquête, jeu de données fictif | M8, M9 |

Chaque étape livre un ZIP complet du projet, testé avant la suivante.
