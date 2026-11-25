package com.example.chifaa.companion;

/**
 * Listes de mots-clés du compagnon (texte déjà normalisé : minuscules, sans accents).
 *
 * ⚠ LISTE PROVISOIRE : le cahier des charges précise que la liste des mots-clés
 * de détresse est FOURNIE PAR L'ENCADRANT. Remplacez DISTRESS_CRITICAL et
 * DISTRESS_HIGH par cette liste dès que vous l'avez reçue.
 */
public final class CompanionKeywords {

    private CompanionKeywords() { }

    // ===================================================== Détresse (provisoire)

    /** Idées suicidaires / auto-agression : gravité "Critical". */
    public static final String[] DISTRESS_CRITICAL = {
            // français
            "suicide", "me suicider", "me tuer", "en finir", "mettre fin a mes jours",
            "plus envie de vivre", "envie de mourir", "je veux mourir", "me faire du mal",
            // arabe standard
            "انتحار", "انتحر", "اقتل نفسي", "انهي حياتي", "اريد ان اموت",
            // darija (graphie arabe et latine)
            "نقتل روحي", "نموت روحي", "ما عادش نحب نعيش", "نحب نموت",
            "nmout", "n9atel rou7i", "nheb nmout"
    };

    /** Détresse émotionnelle forte : gravité "High". */
    public static final String[] DISTRESS_HIGH = {
            "desespoir", "desesperee", "je n'en peux plus", "je n en peux plus", "plus la force",
            "personne ne m'aide", "toute seule face", "je craque",
            "يأس", "لم اعد احتمل", "تعبت من كل شيء",
            "ما عادش نجم", "تعبت برشا", "ma3adech nejem"
    };

    // ================================================= Limites médicales

    /** Demandes de posologie : jamais de réponse, orientation vers l'équipe soignante. */
    public static final String[] DOSAGE = {
            "posologie", "dose", "dosage", "combien de mg", " mg", "milligramme",
            "combien de comprime", "combien de cachet", "quel medicament prendre",
            "dois-je prendre", "je peux prendre", "augmenter mon traitement", "arreter mon traitement",
            "جرعة", "كم حبة", "قداش من حبة", "دواء نشرب"
    };

    /** Demandes de diagnostic : jamais de réponse, orientation vers l'équipe soignante. */
    public static final String[] DIAGNOSIS = {
            "est-ce que j'ai", "est ce que j ai", "ai-je un cancer", "ai je un cancer",
            "est-ce un cancer", "c'est un cancer", "est-ce grave", "est ce grave",
            "est-ce une recidive", "suis-je malade", "diagnostiquer", "j'ai une boule",
            "هل عندي", "هل لدي سرطان", "شنوة عندي", "عندي سرطان"
    };

    // ======================================================= Recherche

    /** Mots vides ignorés lors de la recherche dans les fiches. */
    public static final String[] STOPWORDS = {
            "les", "des", "une", "est", "que", "qui", "quoi", "pour", "dans", "avec", "sur", "pas",
            "mon", "mes", "ma", "ton", "son", "ses", "aux", "par", "plus", "comment", "pourquoi",
            "quand", "quel", "quelle", "quels", "quelles", "faire", "avoir", "etre", "suis", "j'ai",
            "cest", "c'est", "elle", "ils", "nous", "vous", "moi", "bonjour", "merci", "svp",
            "beaucoup", "tres", "trop", "peut", "peux", "veux", "voudrais", "sais", "savoir",
            "هل", "ماذا", "كيف", "متى", "على", "عن", "في", "من", "الى", "هذا", "هذه",
            "شنوة", "كيفاش", "علاش", "برشا", "باش"
    };

    /**
     * Synonymes -> sujet de la bibliothèque. Une question qui contient un de ces mots
     * donne un bonus aux fiches de ce sujet. Format : {mot, code du sujet}.
     */
    public static final String[][] TOPIC_SYNONYMS = {
            {"bebe", "Fertility"}, {"grossesse", "Fertility"},
            {"enceinte", "Fertility"}, {"fertilite", "Fertility"}, {"خصوبة", "Fertility"},
            {"حمل", "Fertility"}, {"cheveux", "Side effects"}, {"perruque", "Side effects"},
            {"fatigue", "Side effects"}, {"fatiguee", "Side effects"}, {"nausee", "Nutrition"},
            {"vomir", "Nutrition"}, {"bras", "Side effects"}, {"تعب", "Side effects"},
            {"شعر", "Side effects"}, {"manger", "Nutrition"}, {"alimentation", "Nutrition"},
            {"regime", "Nutrition"}, {"ماكلة", "Nutrition"}, {"تغذية", "Nutrition"},
            {"peur", "Emotional wellbeing"}, {"enfants", "Emotional wellbeing"}, {"angoisse", "Emotional wellbeing"},
            {"stress", "Emotional wellbeing"}, {"triste", "Emotional wellbeing"},
            {"قلق", "Emotional wellbeing"}, {"travail", "Daily life"}, {"sport", "Daily life"},
            {"marche", "Daily life"}, {"cnam", "Rights & CNAM"}, {"rembourse", "Rights & CNAM"},
            {"argent", "Rights & CNAM"}, {"كنام", "Rights & CNAM"}, {"الكنام", "Rights & CNAM"},
            {"radiotherapie", "Treatment"},
            {"operation", "Treatment"}, {"chirurgie", "Treatment"}, {"biopsie", "Diagnosis"},
            {"resultat", "Diagnosis"}, {"annonce", "Diagnosis"}
    };

    /** Marqueurs de darija (pour détecter la langue de la question). */
    public static final String[] DARIJA_MARKERS = {
            "شنوة", "كيفاش", "علاش", "برشا", "نحب", "باش", "ماعادش", "ما عادش", "توة", "ياسر", "نجم", "متاع",
            "chnowa", "kifech", "barcha", "nheb", "bech", "taw", "yesser", "mte3", "3la"
    };
}
