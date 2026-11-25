package com.example.chifaa;

import static org.junit.Assert.assertEquals;

import com.example.chifaa.companion.CompanionEngine;
import com.example.chifaa.companion.CompanionEngine.Kind;
import com.example.chifaa.database.KbSeedData;
import com.example.chifaa.models.KbItem;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests du compagnon (parcours "conversation avec détresse" exigé par le cahier
 * des charges) sur les vraies fiches de démonstration visibles.
 */
public class CompanionEngineTest {

    /** Les 21 fiches visibles de KbSeedData (approuvées, non expirées, non remplacées). */
    private static List<KbItem> fiches() {
        List<KbItem> items = new ArrayList<>();
        for (String[] r : KbSeedData.ITEMS) {
            boolean visible = "Clinically approved".equals(r[8]) && r[12] == null
                    && r[10] != null && r[10].compareTo("2026-01-01") > 0;
            if (!visible) continue;
            KbItem k = new KbItem();
            k.contentId = r[0];
            k.topic = r[1];
            k.language = r[2];
            k.title = r[3];
            k.contentText = r[4];
            items.add(k);
        }
        return items;
    }

    private static CompanionEngine.Result ask(String question) {
        return CompanionEngine.analyze(question, fiches());
    }

    // ------------------------------------------------------------- Détresse
    @Test
    public void detresseCritiqueFrancais() {
        CompanionEngine.Result r = ask("Je n'ai plus envie de vivre");
        assertEquals(Kind.DISTRESS, r.kind);
        assertEquals("Critical", r.severity);
    }

    @Test
    public void detresseArabeEtDarija() {
        assertEquals(Kind.DISTRESS, ask("أفكر في الانتحار").kind);
        assertEquals(Kind.DISTRESS, ask("تعبت برشا ما عادش نجم").kind);
    }

    @Test
    public void detressePrioritaireSurLeReste() {
        // Même si la phrase parle aussi de dose, la détresse passe en premier
        assertEquals(Kind.DISTRESS, ask("Quelle dose pour en finir ?").kind);
    }

    // ---------------------------------------------------- Limites médicales
    @Test
    public void posologieRefusee() {
        assertEquals(Kind.DOSAGE, ask("Quelle est la posologie de mon traitement ?").kind);
        assertEquals(Kind.DOSAGE, ask("Combien de mg je dois prendre ?").kind);
    }

    @Test
    public void diagnosticRefuse() {
        assertEquals(Kind.DIAGNOSIS, ask("J'ai une boule au sein, est-ce que j'ai un cancer ?").kind);
        assertEquals(Kind.DIAGNOSIS, ask("هل عندي سرطان؟").kind);
    }

    // ------------------------------------------------------------ Réponses
    @Test
    public void reponseFatigue() {
        CompanionEngine.Result r = ask("Je suis très fatiguée, comment gérer la fatigue ?");
        assertEquals(Kind.ANSWER, r.kind);
        assertEquals("KB-0005", r.item.contentId);
    }

    @Test
    public void reponseCheveux() {
        CompanionEngine.Result r = ask("Mes cheveux vont tomber, que faire ?");
        assertEquals(Kind.ANSWER, r.kind);
        assertEquals("KB-0006", r.item.contentId);
    }

    @Test
    public void reponseFertiliteEnArabe() {
        CompanionEngine.Result r = ask("الخصوبة والعلاج");
        assertEquals(Kind.ANSWER, r.kind);
        assertEquals("KB-0018", r.item.contentId); // la fiche dans la langue de la question
    }

    @Test
    public void aucuneFiche() {
        assertEquals(Kind.NO_MATCH, ask("Quelle est la météo demain à Sousse ?").kind);
    }

    // ------------------------------------------------- Langue de la réponse
    @Test
    public void reponseDansLaLangueDeLaQuestion() {
        assertEquals("French", CompanionEngine.replyLanguage("French", "Darija"));
        assertEquals("MSA", CompanionEngine.replyLanguage("MSA", "French"));
        assertEquals("Darija", CompanionEngine.replyLanguage("Darija", "French"));
        // Question mélangée : on suit la langue préférée du profil...
        assertEquals("French", CompanionEngine.replyLanguage("Mixed", "French"));
        // ... et la darija si le profil est lui aussi "Mixed"
        assertEquals("Darija", CompanionEngine.replyLanguage("Mixed", "Mixed"));
    }

    @Test
    public void detresseEnArabeDetecteeEnArabe() {
        CompanionEngine.Result r = ask("تعبت من كل شيء");
        assertEquals(Kind.DISTRESS, r.kind);
        assertEquals("MSA", r.language);
    }

    @Test
    public void ficheArabePrefereeAuFrancaisPourLaDarija() {
        // Pas de fiche "fatigue" en darija : on prend l'arabe standard plutôt que le français
        CompanionEngine.Result r = ask("التعب");
        assertEquals(Kind.ANSWER, r.kind);
        assertEquals("KB-0017", r.item.contentId);
    }

    // ---------------------------------------------------------------- Langue
    @Test
    public void detectionLangue() {
        assertEquals("French", CompanionEngine.detectLanguage("Bonjour, j'ai une question"));
        assertEquals("MSA", CompanionEngine.detectLanguage("ما هي الآثار الجانبية للعلاج؟"));
        assertEquals("Darija", CompanionEngine.detectLanguage("شنوة نعمل كيف نتعب برشا"));
        assertEquals("Darija", CompanionEngine.detectLanguage("chnowa na3mel"));
        assertEquals("Mixed", CompanionEngine.detectLanguage("fatigue برشا"));
    }
}
