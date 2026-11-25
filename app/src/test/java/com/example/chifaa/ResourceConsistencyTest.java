package com.example.chifaa;

import static org.junit.Assert.assertEquals;

import com.example.chifaa.utils.AppConstants;

import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Vérifie que chaque liste de libellés (arrays.xml, français ET arabe) correspond
 * bien à sa liste de codes (AppConstants) : même nombre d'éléments, et même
 * position quand le libellé est identique au code (ex. "Darija", "Fatigue").
 *
 * Ce test aurait détecté le décalage des langues des fiches (Français <-> Darija).
 */
public class ResourceConsistencyTest {

    private static final String FR = "src/main/res/values/arrays.xml";
    private static final String AR = "src/main/res/values-ar/arrays.xml";

    /** Lit les <item> d'un <string-array name="...">. */
    private static List<String> items(String path, String arrayName) throws Exception {
        String xml = new String(Files.readAllBytes(new File(path).toPath()), StandardCharsets.UTF_8);
        Matcher block = Pattern.compile("<string-array name=\"" + arrayName + "\">(.*?)</string-array>",
                Pattern.DOTALL).matcher(xml);
        List<String> result = new ArrayList<>();
        if (block.find()) {
            Matcher item = Pattern.compile("<item>(.*?)</item>").matcher(block.group(1));
            while (item.find()) result.add(item.group(1));
        }
        return result;
    }

    /**
     * @param extra 1 si la liste affichée contient en plus "Je préfère ne pas répondre"
     *              (enregistré comme NULL), sinon 0.
     */
    private static void check(String arrayName, String[] codes, int extra) throws Exception {
        List<String> fr = items(FR, arrayName);
        List<String> ar = items(AR, arrayName);
        assertEquals(arrayName + " (français)", codes.length + extra, fr.size());
        assertEquals(arrayName + " (arabe)", codes.length + extra, ar.size());
        // Si un libellé français est identique à un code, ils doivent être à la même position
        for (int i = 0; i < codes.length; i++) {
            int labelIndex = fr.indexOf(codes[i]);
            if (labelIndex >= 0) {
                assertEquals(arrayName + " : position de « " + codes[i] + " »", i, labelIndex);
            }
        }
    }

    @Test
    public void listesAlignees() throws Exception {
        check("age_bands", AppConstants.AGE_BANDS, 0);
        check("governorates", AppConstants.GOVERNORATE_CODES, 0);
        check("languages", AppConstants.LANGUAGES, 0);
        check("cancer_types", AppConstants.CANCER_TYPES, 0);
        check("care_phases", AppConstants.CARE_PHASES, 1);
        check("diagnosis_bands", AppConstants.DIAGNOSIS_BANDS, 1);
        check("facility_types", AppConstants.FACILITY_TYPES, 0);
        check("treatment_types", AppConstants.TREATMENT_TYPES, 0);
        check("side_effects", AppConstants.SIDE_EFFECTS, 0);
        check("kb_topics", AppConstants.KB_TOPICS, 0);
        check("content_languages", AppConstants.CONTENT_LANGUAGES, 0);
        check("evidence_levels", AppConstants.EVIDENCE_LEVELS, 0);
        check("source_types", AppConstants.SOURCE_TYPES, 0);
        check("service_types", AppConstants.SERVICE_TYPES, 0);
        check("costs", AppConstants.COSTS, 0);
        check("verification_methods", AppConstants.VERIFICATION_METHODS, 0);
    }
}
