package com.affariyet.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Avertissements relevés pendant l'exécution (ex. barre Pace.js encore active après le chargement d'une page).
 *
 * Un avertissement N'EST PAS un échec : il ne modifie jamais le statut TestNG d'un test.
 * Il est affiché dans la console ([AVERT]), dans execution.log, dans le bilan global
 * et dans reports/rapport-execution.html (carte « Avertissements », comptée à part).
 */
public final class Avertissements {

    /** Clé utilisée pour un avertissement émis en dehors d'un test (aucun identifiant courant). */
    public static final String HORS_TEST = "(hors test)";

    private static final Map<String, List<String>> PAR_TEST = new LinkedHashMap<>();

    private Avertissements() {
    }

    public static synchronized void ajouter(String testId, String message) {
        PAR_TEST.computeIfAbsent(testId == null ? HORS_TEST : testId, k -> new ArrayList<>()).add(message);
    }

    /** Avertissements d'un test (liste vide s'il n'y en a aucun). */
    public static synchronized List<String> pour(String testId) {
        List<String> l = PAR_TEST.get(testId);
        return l == null ? Collections.emptyList() : new ArrayList<>(l);
    }

    /** Tous les avertissements, dans l'ordre d'apparition des tests. */
    public static synchronized Map<String, List<String>> tous() {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        PAR_TEST.forEach((k, v) -> copy.put(k, new ArrayList<>(v)));
        return copy;
    }

    public static synchronized int total() {
        return PAR_TEST.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Identifiant d'un test, identique partout (préfixe des logs, bilan, rapport HTML) :
     * « TCxx » si la description @Test commence par TCxx, sinon le nom de la méthode.
     */
    public static String identifiant(String description, String methodName) {
        if (description != null && description.matches("TC\\d{2}.*")) {
            return description.substring(0, 4);
        }
        return methodName;
    }
}
