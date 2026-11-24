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
 * Traces d'exécution : console + fichier reports/logs/execution.log.
 */
public final class Log {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Path LOG_FILE = Paths.get(ConfigReader.get("reports.dir"), "logs", "execution.log");

    private Log() {
    }

    public static void info(String message) {
        write("INFO ", message);
    }

    public static void step(String message) {
        write("ETAPE", message);
    }

    public static void error(String message) {
        write("ERREUR", message);
    }

    private static synchronized void write(String level, String message) {
        String line = String.format("%s [%s] %s", LocalDateTime.now().format(FMT), level, message);
        System.out.println(line);
        try {
            Files.createDirectories(LOG_FILE.getParent());
            Files.writeString(LOG_FILE, line + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Impossible d'écrire le log : " + e.getMessage());
        }
    }
}
