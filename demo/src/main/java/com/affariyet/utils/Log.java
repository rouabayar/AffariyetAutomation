package com.affariyet.utils;

import com.affariyet.config.ConfigReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Traces d'exécution.
 *  - Console : heure + niveau + [TCxx] + message (lisible pendant la démonstration).
 *  - Fichier reports/logs/execution.log : date complète + tout le détail (y compris detail()).
 */
public final class Log {

    private static final DateTimeFormatter FILE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter CONSOLE_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Path LOG_FILE = Paths.get(ConfigReader.get("reports.dir"), "logs", "execution.log");

    /** Identifiant du test en cours (ex. « TC03 »), ajouté devant chaque message. */
    private static final ThreadLocal<String> CURRENT_TEST = new ThreadLocal<>();

    private Log() {
    }

    public static void setCurrentTest(String testId) {
        CURRENT_TEST.set(testId);
    }

    public static void clearCurrentTest() {
        CURRENT_TEST.remove();
    }

    public static void info(String message) {
        write("INFO ", message, true);
    }

    public static void step(String message) {
        write("ETAPE", message, true);
    }

    public static void error(String message) {
        write("ERREUR", message, true);
    }

    /**
     * Avertissement : affiché ([AVERT]) et enregistré dans {@link Avertissements} pour le bilan global
     * et le rapport HTML. Ne change PAS le statut du test.
     */
    public static void warn(String message) {
        Avertissements.ajouter(CURRENT_TEST.get(), message);
        write("AVERT", message, true);
    }

    /** Détail technique écrit uniquement dans execution.log (traces complètes, messages secondaires). */
    public static void detail(String message) {
        write("DETAIL", message, false);
    }

    /** Ligne sans préfixe de test (séparateurs, bilan global). */
    public static void banner(String message) {
        String previous = CURRENT_TEST.get();
        CURRENT_TEST.remove();
        write("INFO ", message, true);
        if (previous != null) {
            CURRENT_TEST.set(previous);
        }
    }

    private static synchronized void write(String level, String message, boolean console) {
        LocalDateTime now = LocalDateTime.now();
        String test = CURRENT_TEST.get();
        String body = (test == null ? "" : "[" + test + "] ") + message;
        if (console) {
            System.out.println(now.format(CONSOLE_FMT) + " [" + level + "] " + body);
        }
        try {
            Files.createDirectories(LOG_FILE.getParent());
            Files.writeString(LOG_FILE, now.format(FILE_FMT) + " [" + level + "] " + body + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Impossible d'écrire le log : " + e.getMessage());
        }
    }
}
