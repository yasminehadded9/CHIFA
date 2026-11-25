package com.example.chifaa.companion;

import com.example.chifaa.models.KbItem;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * "Cerveau" du compagnon, version de base demandée par le cahier des charges (M5) :
 * il ne génère pas de texte, il CHERCHE dans les fiches validées.
 *
 * Ordre de traitement d'une question (le plus important d'abord) :
 *   1. détresse            -> ressources d'aide immédiates + événement de sécurité ;
 *   2. diagnostic/posologie -> message type, orientation vers l'équipe soignante ;
 *   3. recherche de fiche   -> extrait de la meilleure fiche, ou orientation vers l'annuaire.
 *
 * Classe en Java pur (aucune dépendance Android) : testée par CompanionEngineTest.
 */
public final class CompanionEngine {

    private CompanionEngine() { }

    public enum Kind { DISTRESS, DOSAGE, DIAGNOSIS, ANSWER, NO_MATCH }

    /** Résultat de l'analyse d'une question. */
    public static class Result {
        public Kind kind;
        public String severity;        // "Critical" / "High" si détresse
        public String language;        // Darija, French, MSA, Mixed
        public KbItem item;            // fiche trouvée (Kind.ANSWER)
        public String excerpt;         // extrait de la fiche
        public String topic;           // sujet (topic_tag du message)
    }

    /** Score minimal pour considérer qu'une fiche répond à la question. */
    static final int MIN_SCORE = 3;
    static final int EXCERPT_MAX = 240;

    // ================================================================ Analyse

    public static Result analyze(String question, List<KbItem> items) {
        Result r = new Result();
        String text = normalize(question);
        r.language = detectLanguage(question);

        // 1. Détresse (prioritaire sur tout le reste)
        if (containsAny(text, CompanionKeywords.DISTRESS_CRITICAL)) {
            r.kind = Kind.DISTRESS;
            r.severity = "Critical";
            r.topic = "distress";
            return r;
        }
        if (containsAny(text, CompanionKeywords.DISTRESS_HIGH)) {
            r.kind = Kind.DISTRESS;
            r.severity = "High";
            r.topic = "distress";
            return r;
        }

        // 2. Limites médicales : aucune posologie, aucun diagnostic
        if (containsAny(text, CompanionKeywords.DOSAGE)) {
            r.kind = Kind.DOSAGE;
            r.topic = "medical-limit";
            return r;
        }
        if (containsAny(text, CompanionKeywords.DIAGNOSIS)) {
            r.kind = Kind.DIAGNOSIS;
            r.topic = "medical-limit";
            return r;
        }

        // 3. Recherche de la meilleure fiche
        KbItem best = bestMatch(question, r.language, items);
        if (best == null) {
            r.kind = Kind.NO_MATCH;
            r.topic = "no-match";
            return r;
        }
        r.kind = Kind.ANSWER;
        r.item = best;
        r.topic = best.topic;
        r.excerpt = excerpt(best.contentText);
        return r;
    }

    // ============================================================== Recherche

    /** Meilleure fiche pour la question, ou null si aucune n'atteint MIN_SCORE. */
    public static KbItem bestMatch(String question, String language, List<KbItem> items) {
        List<String> words = keywords(question);
        Set<String> topics = topicsFromSynonyms(question);
        if (words.isEmpty() && topics.isEmpty()) return null;

        KbItem best = null;
        int bestScore = 0;
        for (KbItem item : items) {
            int score = score(item, words, topics);
            if (score < MIN_SCORE) continue;
            // À score égal ou proche, on préfère la langue de la question,
            // puis l'autre forme d'arabe (darija <-> arabe standard), même alphabet
            if (item.language != null && item.language.equals(language)) {
                score += 2;
            } else if (isArabic(item.language) && isArabic(language)) {
                score += 1;
            }
            if (score > bestScore) {
                bestScore = score;
                best = item;
            }
        }
        return best;
    }

    static int score(KbItem item, List<String> words, Set<String> topics) {
        String title = normalize(item.title);
        String content = normalize(item.contentText);
        int score = 0;
        for (String w : words) {
            String stem = stem(w);
            if (title.contains(stem)) score += 3;
            else if (content.contains(stem)) score += 1;
        }
        if (topics.contains(item.topic)) score += 3;
        return score;
    }

    /** Mots significatifs de la question (sans mots vides ni mots trop courts). */
    public static List<String> keywords(String question) {
        Set<String> stop = new HashSet<>();
        for (String s : CompanionKeywords.STOPWORDS) stop.add(normalize(s).trim());

        List<String> result = new ArrayList<>();
        for (String token : normalize(question).split("[^\\p{L}\\p{N}]+")) {
            if (token.length() >= 3 && !stop.contains(token) && !result.contains(token)) {
                result.add(token);
            }
        }
        return result;
    }

    static Set<String> topicsFromSynonyms(String question) {
        String text = normalize(question);
        Set<String> topics = new HashSet<>();
        for (String[] pair : CompanionKeywords.TOPIC_SYNONYMS) {
            if (text.contains(normalize(pair[0]).trim())) topics.add(pair[1]);
        }
        return topics;
    }

    private static boolean isArabic(String language) {
        return "MSA".equals(language) || "Darija".equals(language);
    }

    /** Racine simple : "nausees" -> "nause", "cheveux" -> "cheve" (gère pluriels et accords). */
    static String stem(String word) {
        return word.length() > 5 ? word.substring(0, 5) : word;
    }

    /** Début de la fiche, coupé proprement à la fin d'une phrase si possible. */
    public static String excerpt(String content) {
        if (content == null) return "";
        if (content.length() <= EXCERPT_MAX) return content;
        int cut = content.lastIndexOf('.', EXCERPT_MAX);
        return cut > 80 ? content.substring(0, cut + 1) : content.substring(0, EXCERPT_MAX) + "…";
    }

    // ================================================================ Langue

    /** Détection simple : alphabet + marqueurs de darija. */
    public static String detectLanguage(String text) {
        if (text == null) return "French";
        boolean arabic = text.matches("(?s).*[\\u0600-\\u06FF].*");
        boolean latin = text.matches("(?s).*[A-Za-zÀ-ÿ].*");
        String norm = normalize(text);
        boolean darija = containsAny(norm, CompanionKeywords.DARIJA_MARKERS);

        if (arabic && latin) return "Mixed";
        if (arabic) return darija ? "Darija" : "MSA";
        return darija ? "Darija" : "French"; // darija écrite en lettres latines ("chnowa")
    }

    /**
     * Langue de la RÉPONSE : celle de la question.
     * Question "Mixed" (arabe + latin) : on suit la langue préférée du profil,
     * et si elle est aussi "Mixed", on répond en darija (le mélange le plus courant en Tunisie).
     */
    public static String replyLanguage(String detected, String preferred) {
        if ("French".equals(detected) || "MSA".equals(detected) || "Darija".equals(detected)) {
            return detected;
        }
        if ("French".equals(preferred) || "MSA".equals(preferred) || "Darija".equals(preferred)) {
            return preferred;
        }
        return "Darija";
    }

    // =========================================================== Outils texte

    /**
     * Minuscules, sans accents ni signes diacritiques arabes, formes d'alif unifiées,
     * entouré d'espaces (pour reconnaître les mots en début/fin de phrase).
     */
    public static String normalize(String text) {
        if (text == null) return " ";
        String s = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")             // accents français + harakat arabes
                .replaceAll("[أإآ]", "ا")
                .replace('ى', 'ي')
                .replace('ة', 'ه')
                .replace('’', '\'')
                .replaceAll("\\s+", " ");
        return " " + s.trim() + " ";
    }

    /**
     * Vrai si le texte contient un des mots-clés.
     *  - mots-clés latins : ils doivent commencer un mot (" dose" trouve "doses",
     *    mais "mg" ne se déclenche pas au milieu d'un autre mot) ;
     *  - mots-clés arabes : recherche simple, car l'arabe colle souvent des préfixes
     *    au mot ("والانتحار" contient "انتحار").
     */
    static boolean containsAny(String normalizedText, String[] keywords) {
        for (String k : keywords) {
            String nk = normalize(k).trim();
            if (nk.isEmpty()) continue;
            boolean arabicKeyword = nk.matches("(?s).*[\\u0600-\\u06FF].*");
            if (arabicKeyword ? normalizedText.contains(nk) : normalizedText.contains(" " + nk)) {
                return true;
            }
        }
        return false;
    }
}
