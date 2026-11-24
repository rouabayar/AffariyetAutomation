package com.affariyet.utils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Règles de conformité du contenu affiché (critères de test, et non résultats attendus codés en dur).
 * Le texte analysé est toujours celui lu sur la page au moment de l'exécution.
 */
public final class ContentChecks {

    /**
     * Mots d'interface anglais sans équivalent identique en français
     * (« Message », par exemple, est volontairement exclu pour éviter les faux échecs).
     */
    private static final List<String> ENGLISH_UI_TERMS = List.of(
            "optional", "required", "choose file", "browse", "upload", "send", "submit", "subject");

    /** Mots caractéristiques du texte de remplissage « Lorem Ipsum ». */
    private static final Set<String> LOREM_IPSUM_WORDS = Set.of(
            "lorem", "ipsum", "dolor", "consectetur", "adipiscing", "eiusmod", "tempor", "incididunt",
            "consequat", "occaecat", "cupidatat", "deserunt", "aliquip", "fugiat", "exercitation",
            "ullamco", "laboris", "nostrud", "reprehenderit", "voluptate", "officia", "mollit");

    /** Seuil : au moins deux mots caractéristiques pour conclure à un texte de remplissage. */
    public static final int LOREM_IPSUM_THRESHOLD = 2;

    private ContentChecks() {
    }

    /** Termes d'interface anglais trouvés dans le texte (comparaison sans casse, mots entiers). */
    public static List<String> findEnglishUiTerms(String text) {
        List<String> found = new ArrayList<>();
        if (text == null) {
            return found;
        }
        String lower = text.toLowerCase(Locale.ROOT);
        for (String term : ENGLISH_UI_TERMS) {
            Pattern p = Pattern.compile("(?<!\\p{L})" + Pattern.quote(term) + "(?!\\p{L})");
            if (p.matcher(lower).find()) {
                found.add(term);
            }
        }
        return found;
    }

    /** Mots caractéristiques du Lorem Ipsum trouvés dans le texte (sans doublon, dans l'ordre d'apparition). */
    public static List<String> findLoremIpsumWords(String text) {
        Set<String> found = new LinkedHashSet<>();
        if (text == null) {
            return new ArrayList<>(found);
        }
        for (String word : text.toLowerCase(Locale.ROOT).split("[^\\p{L}]+")) {
            if (LOREM_IPSUM_WORDS.contains(word)) {
                found.add(word);
            }
        }
        return new ArrayList<>(found);
    }

    public static boolean isLoremIpsum(String text) {
        return findLoremIpsumWords(text).size() >= LOREM_IPSUM_THRESHOLD;
    }
}
