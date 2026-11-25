# CHIFAA : application mobile (Android · Java · SQLite)

Compagnon pour les femmes touchées par le cancer du sein. Projet étudiant : **toutes les données sont fictives**.

## Étape 7 : compagnon conversationnel (module M5, version de base)

| Règle | Où c'est fait |
|---|---|
| Répond en cherchant dans les fiches validées (mots-clés + sujet) | `CompanionEngine.bestMatch()` (Java pur, testé) |
| Aucune fiche : il le dit et propose l'annuaire | réponse « no-match » + bouton « Ouvrir l'annuaire » |
| Aucun diagnostic, aucune posologie : message type vers l'équipe soignante | `CompanionKeywords.DOSAGE / DIAGNOSIS` |
| Détresse : ressources d'aide immédiates + événement de sécurité | carte « Vous n'êtes pas seule » + `ChatDao.addSafetyEvent()` (Open, Critical/High) |
| Historique : sessions et messages avec date, langue détectée, sujet | `chat_sessions`, `messages` ; écran `ChatHistoryActivity` |
| Avis : note de 1 à 5 sur chaque réponse | étoiles → `messages.user_feedback` |

Ordre de traitement : **détresse → diagnostic/posologie → recherche de fiche**.

**Langue de la réponse = langue de la question** (`CompanionEngine.replyLanguage`, `LocalizedText`) :
français → `values/`, arabe standard → `values-ar/`, darija → `values-ar-rTN/` (arabe de Tunisie).
Question mélangée : langue préférée du profil. La fiche dans la langue de la question est préférée ;
sinon la réponse précise dans quelle langue la fiche existe.
Langue détectée : alphabet arabe/latin + marqueurs de darija (`Darija`, `French`, `MSA`, `Mixed`).

⚠ **À compléter avec l'encadrant** : la liste des mots-clés de détresse (`CompanionKeywords`, actuellement
provisoire) et les numéros d'aide (`chat_distress_resource`, `chat_distress_emergency` dans `strings.xml`).
Aucun numéro n'a été inventé.

`CompanionEngineTest` : 13 tests (dont le parcours « conversation avec détresse »).

## Étape 6 : annuaire de services (module M6)

| Règle | Où c'est fait |
|---|---|
| Recherche par gouvernorat (préselection : celui du profil) et par type de service (9 types) | `DirectoryActivity`, `DirectoryDao.search()` |
| Fiche : nom, adresse, téléphone, langues, coût, date de dernière vérification | `OrgDetailActivity` |
| Bouton d'appel direct | intent implicite `ACTION_DIAL` (comme le TP « Intents implicites »), sans permission |
| Itinéraire | intent implicite `geo:0,0?q=adresse` (comme `testmaps()` du TP) |
| Consultation hors ligne | services stockés dans SQLite |
| Fiche non vérifiée depuis plus de 12 mois | badge orange + avertissement (`ValidationUtils.isVerificationOutdated`) |

30 services **fictifs** (`DirectorySeedData`, numéros `70 000 1xx`), langues dans la table d'association
`organisation_languages` (lecture avec `GROUP_CONCAT`). Base passée en **version 3** (comptes conservés).

## Étape 5 : bibliothèque de fiches validées (module M4)

| Règle | Où c'est fait |
|---|---|
| Seules les fiches « Clinically approved », non expirées et non remplacées sont visibles | `KbDao.VISIBLE` (condition SQL unique) |
| Filtres sujet + langue + recherche par mot-clé (requête paramétrée) | `LibraryActivity`, `KbDao.search()` |
| Fiche détaillée : titre, texte, source, date de revue, niveau de preuve | `ArticleActivity` |
| Lecture hors ligne des fiches consultées | toutes les fiches sont dans SQLite ; `ReadHistory` mémorise les fiches lues |
| Contenu fictif signalé (« Contenu fictif – non validé ») | `kb_items.is_fictional` + bandeau |

24 fiches de démonstration (`KbSeedData`) : 21 visibles (16 FR, 3 MSA, 2 Darija) et 3 qui doivent rester invisibles
(brouillon KB-0022, expirée KB-0023, retirée KB-0024) pour tester la règle de visibilité.
Base passée en **version 2** : `onUpgrade()` ajoute les fiches **sans effacer les comptes existants**.

## Étape 4 : Espace santé (module M3)

| Règle | Où c'est fait |
|---|---|
| Accessible uniquement avec le consentement santé | tuile masquée + écran bloqué + `HealthDao.requireConsent()` sur chaque lecture/écriture |
| Mon parcours, chaque champ propose « Je préfère ne pas répondre » | `JourneyActivity` (code « Prefer not to say » ou NULL selon la colonne) |
| Bilan : effets ressentis (cases à cocher) + date | `CheckinActivity` (calendrier, puces, « Aucun effet » exclusif) |
| Historique en liste + graphique simple | `HealthActivity` + `SideEffectsChartView` (dessiné avec Canvas) |
| Fertilité : contenus proposés si l'utilisatrice le souhaite | carte « Voir les fiches fertilité » → bibliothèque filtrée |

Contrôles : année entre 1950 et l'année en cours, cohérence année / ancienneté, date de bilan ni future
ni de plus d'un an, un seul bilan par date, au moins un effet ou « Aucun effet ». 16 tests unitaires.

## Étape 3 : tableau de bord et profil (module M2)

| Fonction | Où c'est fait |
|---|---|
| Tableau de bord : salutation, carte « Compagnon », grille des modules | `HomeActivity`, `DashboardAdapter` (RecyclerView en grille) |
| Tuile « Espace santé » masquée sans consentement santé | `HomeActivity.afficherTuiles()` + `AccountDao.hasConsent()` |
| Modifier tranche d'âge, gouvernorat, langue d'échange, surnom | `ProfileActivity` → `AccountDao.updateProfile()` (transaction) |
| Bouton « Enregistrer » actif seulement s'il y a une modification | `ProfileActivity.aDesModifications()` |
| Confirmation si on quitte sans enregistrer | `OnBackPressedCallback` |
| Interface en français ou en arabe, de droite à gauche | `LocaleUtils` + `res/values-ar/` (157 textes) + `xml/locales_config.xml` |

Deux langues différentes : la **langue de l'interface** (menus, fr/ar) et la **langue préférée** de la table `users`
(Darija, Français, MSA, Mélange), utilisée par le compagnon.

## Étape 2 : inscription et connexion (module M1)

Parcours d'inscription : Âge → 3 consentements → Profil → Téléphone → Code SMS → Espace connecté.
Connexion : Téléphone → Code SMS → Espace connecté.

| Règle du cahier des charges | Où c'est fait |
|---|---|
| Moins de 18 ans : inscription bloquée + message d'orientation | `SignupAgeActivity` |
| Un écran par finalité, version du texte, date (UTC), mode « In-app » | `ConsentActivity` → table `consents` |
| Téléphone chiffré, jamais dans le profil | `CryptoUtils` (AES-GCM, clé dans l'Android Keystore) → `identities` |
| Retrouver un compte sans déchiffrer | empreinte SHA-256 `phone_hash` |
| Pseudonyme UUID utilisé partout | `AccountDao.createAccount()` |
| Code SMS simulé | `OtpActivity` : 6 chiffres, 5 min, 3 essais, renvoi après 30 s |
| Rien n'est écrit si l'inscription est abandonnée | tout est enregistré en une transaction après le code |

Contrôle de saisie : `ValidationUtils` (testé par `ValidationUtilsTest`, 12 tests) + messages dans `strings.xml`.

## Étape 1 : les fondations

| Élément | Contenu |
|---|---|
| Design system | Palette prune/framboise/vert d'eau, typographie, boutons, cartes (`res/values`) |
| Écrans | Splash animé → Onboarding (3 pages, affiché une seule fois) → Accueil |
| Base SQLite | 21 tables, contraintes CHECK, clés étrangères, 3 déclencheurs, 8 index |
| Utilitaires | `AppConstants` (listes fermées), `DateUtils` (ISO 8601 UTC), `SessionManager` |

## Ouvrir le projet
1. Décompresser le ZIP.
2. Android Studio → **File › Open** → choisir le dossier `CHIFAA`.
3. Laisser Gradle se synchroniser (installer le SDK 36 si Android Studio le propose).
4. Lancer sur un émulateur ou un téléphone (Android 8.0 minimum).

Configuration : AGP 9.2.1 · Gradle 9.4.1 · compileSdk 36 · minSdk 26 · Java 11 · JDK 17 (fourni avec Android Studio).

## Vérifier la base de données
- **Logcat** : filtrer sur `CHIFAA` → `Base SQLite prête : 21 tables`.
- **App Inspection › Database Inspector** : ouvrir `chifaa.db` pour voir les 21 tables.
- Le schéma complet et lisible est dans `docs/schema_chifaa_sqlite.sql` (utile pour le rapport / MLD).

## Revoir l'onboarding
L'onboarding n'apparaît qu'au premier lancement. Pour le revoir :
**Paramètres du téléphone › Applications › CHIFAA › Stockage › Effacer les données**
(cela supprime aussi la base, qui sera recréée).

## Structure
```
app/src/main/java/com/example/chifaa/
├── activities/   Splash, Onboarding, Welcome, SignupAge, Consent, SignupProfile,
│                 Phone (inscription + connexion), Otp, Home (tableau de bord), Profile,
│                 Health, Journey, Checkin, Library, Article, Directory, OrgDetail,
│                 Chat, ChatHistory
├── adapters/     OnboardingAdapter, DashboardAdapter, CheckinAdapter, KbAdapter, OrgAdapter, ChatAdapter
├── companion/    CompanionEngine (analyse des questions), CompanionKeywords
├── database/     DatabaseHelper (v3), DbSchema, DbSeeder, KbSeedData, DirectorySeedData,
│                 AccountDao, HealthDao, KbDao, DirectoryDao, ChatDao
├── models/       OnboardingItem, SignupData, UserProfile, DashboardTile,
│                 HealthProfile, Checkin, KbItem, Organisation, ChatMessage, ChatSessionSummary
├── utils/        AppConstants, CryptoUtils, DateUtils, DirectoryUtils, LabelUtils, LocaleUtils, PasswordUtils,
│                 ReadHistory, SessionManager, UiUtils, ValidationUtils
└── views/        SideEffectsChartView (graphique Canvas)
```

## Choix de conception (à reprendre dans le rapport)
- **Champs à choix multiples** (`treatment_types`, `side_effects_reported`, `unmet_needs`, `support_sources`, langues des services) :
  tables d'association à clé primaire composée. SQLite n'a pas de type tableau, et une table d'association
  interdit les doublons, permet un CHECK sur chaque valeur et facilite les statistiques (`GROUP BY`).
- **Listes fermées** : les valeurs autorisées sont définies une seule fois dans `AppConstants`
  et servent à la fois aux CHECK SQL et aux écrans. L'interface et la base ne peuvent donc pas diverger.
- **Suppression d'un compte** : `CASCADE` sur les données personnelles, `SET NULL` sur les preuves
  (consentements, événements de sécurité, demandes), qui sont conservées sans lien vers la personne.
- **`access_log`** en ajout seul et **`staff`** jamais supprimé : garantis par des déclencheurs SQLite.
- **Dates** : texte ISO 8601 en UTC (`2026-11-25T11:35:00Z`).
- Quatre listes du catalogue n'étaient pas détaillées dans le résumé (étapes du parcours AT-007, effets
  secondaires AT-073, origines d'alerte AT-022, types de source AT-044). Des valeurs provisoires sont dans
  `AppConstants` ; il suffit de les remplacer par celles du catalogue.
