package com.affariyet.utils;

import com.affariyet.config.ConfigReader;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Preuves en cas d'échec : capture PNG + code HTML de la page au moment de l'échec.
 */
public final class ScreenshotUtils {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private ScreenshotUtils() {
    }

    /** @return chemin de la capture PNG, ou null si elle n'a pas pu être prise */
    public static Path capture(WebDriver driver, String testName) {
        if (driver == null) {
            return null;
        }
        String base = testName.replaceAll("[^A-Za-z0-9_-]", "_") + "_" + LocalDateTime.now().format(FMT);
        Path dir = Paths.get(ConfigReader.get("screenshots.dir"));
        try {
            Files.createDirectories(dir);
            Path png = dir.resolve(base + ".png");
            Files.write(png, ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
            // Le HTML permet de vérifier les sélecteurs exacts au moment de l'échec.
            Files.writeString(dir.resolve(base + ".html"), driver.getPageSource(), StandardCharsets.UTF_8);
            Log.info("Capture d'écran enregistrée : " + png.toAbsolutePath());
            return png;
        } catch (IOException | RuntimeException e) {
            Log.error("Capture impossible : " + e.getMessage());
            return null;
        }
    }
}
