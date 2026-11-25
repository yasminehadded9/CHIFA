package com.example.chifaa.database;

/**
 * Fiches de DÉMONSTRATION de la bibliothèque (module M4).
 *
 * ⚠ Contenu FICTIF, NON VALIDÉ médicalement (is_fictional = 1), conformément aux
 * contraintes éthiques du projet : il sert uniquement à tester l'application.
 * Les fiches restent générales : aucun diagnostic, aucune posologie.
 *
 * Colonnes : content_id, topic, language, title, content_text, source_type,
 * evidence_level, local_applicability, approval_status, review_date, expiry_date,
 * content_version, superseded_by
 *
 * Les 3 dernières fiches NE DOIVENT PAS apparaître dans l'application :
 * un brouillon, une fiche expirée et une fiche retirée (remplacée).
 */
public final class KbSeedData {

    private KbSeedData() { }

    public static final String REVIEWER = "CR-01";
    public static final String SOURCE_REF = "Document fictif de démonstration – projet CHIFAA (2026)";

    private static final String OK = "Clinically approved";

    public static final String[][] ITEMS = {
            // ------------------------------------------------------------ Français
            {"KB-0001", "Diagnosis", "French", "Comprendre son compte rendu de biopsie",
                    "Le compte rendu de biopsie décrit les cellules prélevées. Il contient souvent des termes techniques : type de tumeur, grade, récepteurs hormonaux. Ces informations aident l'équipe à choisir les traitements. N'hésitez pas à demander à votre médecin de vous expliquer chaque mot, et à noter vos questions avant le rendez-vous.",
                    "Clinical guideline", "High", "Applies", OK, "2026-03-10", "2027-03-10", "v1.0", null},
            {"KB-0002", "Diagnosis", "French", "Les questions à poser lors de l'annonce",
                    "L'annonce du diagnostic est un moment difficile. Il est normal de ne pas tout retenir. Vous pouvez venir accompagnée, demander un second rendez-vous d'explication et poser des questions simples : quelles sont les prochaines étapes ? qui contacter en cas de souci ? Une infirmière d'annonce peut aussi vous accompagner.",
                    "Expert consensus", "Expert opinion", "Applies", OK, "2026-02-01", "2027-02-01", "v1.1", null},
            {"KB-0003", "Treatment", "French", "Les grandes familles de traitements",
                    "Le traitement du cancer du sein peut associer chirurgie, radiothérapie, chimiothérapie, hormonothérapie ou thérapies ciblées. Chaque parcours est personnalisé et décidé en réunion pluridisciplinaire. Seule votre équipe soignante peut vous dire quels traitements sont adaptés à votre situation.",
                    "Clinical guideline", "High", "Applies", OK, "2026-01-15", "2027-01-15", "v2.0", null},
            {"KB-0004", "Treatment", "French", "Se préparer à une séance de radiothérapie",
                    "La radiothérapie se déroule en plusieurs séances courtes et indolores. Portez des vêtements confortables, prévoyez le temps du trajet et signalez toute gêne cutanée à l'équipe. Demandez quels soins de peau sont conseillés dans votre centre avant d'utiliser un produit.",
                    "Institutional website", "Moderate", "Needs adaptation", OK, "2026-04-20", "2027-04-20", "v1.0", null},
            {"KB-0005", "Side effects", "French", "Fatigue : mieux la comprendre",
                    "La fatigue est l'un des effets les plus fréquents pendant et après les traitements. Elle n'est pas toujours soulagée par le repos. Une activité physique douce, adaptée et validée par l'équipe, ainsi qu'une organisation des journées par priorités, peuvent aider. Parlez-en : la fatigue se prend en charge.",
                    "Peer-reviewed article", "High", "Applies", OK, "2026-05-05", "2027-05-05", "v1.0", null},
            {"KB-0006", "Side effects", "French", "Perte des cheveux : s'y préparer",
                    "Certaines chimiothérapies entraînent une chute des cheveux, généralement temporaire. Couper ses cheveux plus courts avant, choisir un foulard ou une perruque, ou demander conseil à une association peut faciliter ce moment. L'annuaire CHIFAA recense des services « prothèses et perruques ».",
                    "Patient association", "Expert opinion", "Applies", OK, "2026-06-12", "2027-06-12", "v1.0", null},
            {"KB-0007", "Side effects", "French", "Lymphœdème : les signes à signaler",
                    "Après une chirurgie des ganglions, le bras peut gonfler : c'est le lymphœdème. Une sensation de lourdeur, une bague ou une manche qui serre sont des signes à signaler rapidement à l'équipe soignante, qui pourra proposer une prise en charge adaptée.",
                    "Clinical guideline", "Moderate", "Applies", OK, "2026-03-28", "2027-03-28", "v1.0", null},
            {"KB-0008", "Nutrition", "French", "Bien manger pendant les traitements",
                    "Une alimentation variée aide à garder des forces. En cas de nausées, de petits repas fréquents sont souvent mieux tolérés. Évitez les régimes restrictifs sans avis médical. Une diététicienne peut vous proposer des conseils adaptés à vos goûts et à vos traitements.",
                    "Institutional website", "Moderate", "Applies", OK, "2026-02-18", "2027-02-18", "v1.0", null},
            {"KB-0009", "Emotional wellbeing", "French", "Gérer l'anxiété au quotidien",
                    "L'inquiétude est une réaction normale face à la maladie. Parler à un proche, rejoindre un groupe de paroles ou consulter un psychologue peut aider. Si l'angoisse devient envahissante ou si vous avez des idées noires, contactez sans attendre votre équipe soignante ou un service d'aide.",
                    "Expert consensus", "Moderate", "Applies", OK, "2026-07-01", "2027-07-01", "v1.0", null},
            {"KB-0010", "Emotional wellbeing", "French", "Parler de la maladie à ses enfants",
                    "Les enfants perçoivent les changements. Leur expliquer avec des mots simples, adaptés à leur âge, les rassure souvent davantage que le silence. Vous pouvez leur dire ce qui va changer dans le quotidien et les inviter à poser leurs questions.",
                    "Patient association", "Expert opinion", "Applies", OK, "2026-05-22", "2027-05-22", "v1.0", null},
            {"KB-0011", "Fertility", "French", "Fertilité et traitements : en parler tôt",
                    "Certains traitements peuvent avoir un effet sur la fertilité. Si vous avez un projet d'enfant, parlez-en avant le début des traitements : des solutions de préservation peuvent parfois être proposées. Votre oncologue peut vous orienter vers une consultation spécialisée.",
                    "Clinical guideline", "High", "Applies", OK, "2026-04-02", "2027-04-02", "v1.0", null},
            {"KB-0012", "Fertility", "French", "Projet de grossesse après un cancer du sein",
                    "Une grossesse après un cancer du sein est parfois possible. Le bon moment dépend des traitements reçus et de votre situation. Cette décision se prépare avec l'équipe soignante, qui évaluera avec vous les conditions les plus sûres.",
                    "Peer-reviewed article", "Moderate", "Needs adaptation", OK, "2026-08-10", "2027-08-10", "v1.0", null},
            {"KB-0013", "Daily life", "French", "Reprendre le travail",
                    "La reprise du travail se prépare : visite avec le médecin du travail, temps partiel thérapeutique, aménagement du poste. Prenez le temps d'en discuter avec votre médecin et, si vous le souhaitez, avec votre employeur.",
                    "Institutional website", "Expert opinion", "Needs adaptation", OK, "2026-06-30", "2027-06-30", "v1.0", null},
            {"KB-0014", "Daily life", "French", "Activité physique adaptée",
                    "Bouger un peu chaque jour, à son rythme, aide à lutter contre la fatigue et améliore le moral. La marche est un bon début. Demandez à votre équipe quelles activités sont adaptées à votre traitement et à votre état de santé.",
                    "Peer-reviewed article", "High", "Applies", OK, "2026-09-05", "2027-09-05", "v1.0", null},
            {"KB-0015", "Rights & CNAM", "French", "Prise en charge par la CNAM",
                    "Les maladies de longue durée peuvent ouvrir droit à une meilleure prise en charge des soins. Les démarches se font avec votre médecin traitant et votre centre CNAM. L'annuaire CHIFAA recense des services d'« aide CNAM » pour vous accompagner.",
                    "Institutional website", "Expert opinion", "Applies", OK, "2026-03-03", "2027-03-03", "v1.0", null},
            {"KB-0016", "Rights & CNAM", "French", "Se faire accompagner dans les démarches",
                    "Assistantes sociales, associations et services juridiques peuvent vous aider pour les papiers, le transport ou les questions financières. N'hésitez pas à demander ce soutien dès le début du parcours.",
                    "Patient association", "Expert opinion", "Applies", OK, "2026-10-01", "2027-10-01", "v1.0", null},

            // ---------------------------------------------------- Arabe standard (MSA)
            {"KB-0017", "Side effects", "MSA", "التعب أثناء العلاج",
                    "التعب من أكثر الآثار شيوعًا أثناء العلاج وبعده، ولا يزول دائمًا بالراحة. قد يساعد النشاط البدني الخفيف الذي يوافق عليه الفريق الطبي وتنظيم اليوم حسب الأولويات. تحدّثي عنه مع فريقك العلاجي.",
                    "Peer-reviewed article", "High", "Applies", OK, "2026-05-05", "2027-05-05", "v1.0", null},
            {"KB-0018", "Fertility", "MSA", "الخصوبة والعلاج",
                    "قد تؤثر بعض العلاجات على الخصوبة. إذا كان لديك مشروع إنجاب، تحدّثي عنه قبل بدء العلاج، فقد تُقترح أحيانًا حلول للحفاظ على الخصوبة. يمكن لطبيبك توجيهك إلى استشارة مختصة.",
                    "Clinical guideline", "High", "Applies", OK, "2026-04-02", "2027-04-02", "v1.0", null},
            {"KB-0019", "Emotional wellbeing", "MSA", "التعامل مع القلق",
                    "القلق ردّ فعل طبيعي أمام المرض. قد يساعدك الحديث مع شخص قريب أو الانضمام إلى مجموعة دعم أو استشارة أخصائي نفسي. إذا أصبح القلق شديدًا، اتصلي بفريقك العلاجي دون انتظار.",
                    "Expert consensus", "Moderate", "Applies", OK, "2026-07-01", "2027-07-01", "v1.0", null},

            // -------------------------------------------------------------- Darija
            {"KB-0020", "Nutrition", "Darija", "الماكلة وقت العلاج",
                    "كول حاجات متنوعة باش تحافظ على صحتك. كان عندك الغثيان، كول شوية شوية وبرشا مرات في النهار. ما تعملش ريجيم من غير ما تستشير الطبيب.",
                    "Institutional website", "Moderate", "Applies", OK, "2026-02-18", "2027-02-18", "v1.0", null},
            {"KB-0021", "Rights & CNAM", "Darija", "الكنام والمساعدة",
                    "الأمراض الطويلة تنجم تعطيك حق في تغطية أحسن للعلاج. اسأل الطبيب متاعك ومركز الكنام. في الدليل متاع شفاء تلقى خدمات تعاونك في الإجراءات.",
                    "Institutional website", "Expert opinion", "Applies", OK, "2026-03-03", "2027-03-03", "v1.0", null},

            // ------------------------------------------ Fiches INVISIBLES (tests du filtre)
            {"KB-0022", "Treatment", "French", "[Brouillon] Nouvelle fiche en cours de relecture",
                    "Cette fiche est un brouillon : elle ne doit pas apparaître dans l'application.",
                    "Clinical guideline", null, null, "Draft", null, null, "v0.1", null},
            {"KB-0023", "Side effects", "French", "[Expirée] Ancienne fiche nausées",
                    "Cette fiche a dépassé sa date de nouvelle revue : elle ne doit pas apparaître.",
                    "Clinical guideline", "Moderate", "Applies", OK, "2024-06-01", "2025-06-01", "v1.0", null},
            {"KB-0024", "Treatment", "French", "[Retirée] Les traitements (ancienne version)",
                    "Ancienne version remplacée par KB-0003 : elle ne doit pas apparaître.",
                    "Clinical guideline", "High", "Applies", "Retired", "2025-01-15", "2026-01-15", "v1.0", "KB-0003"},
    };
}
