package com.affariyet.utils;

import com.affariyet.config.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Création du navigateur selon config.properties.
 * Selenium Manager (inclus dans Selenium 4.27) télécharge automatiquement le bon driver.
 */
public final class DriverFactory {

    /**
     * Avertissement CDP : Selenium 4.27 (version imposée) ne contient pas les modules DevTools de Chrome 156.
     * À chaque démarrage, ces deux composants l'écrivent deux fois sur la sortie d'erreur. Le projet n'utilise
     * aucune fonction DevTools : seuls ces deux composants sont limités aux erreurs graves (SEVERE), et une
     * ligne INFO unique l'indique. Réactivation : -Dselenium.cdp.avertissements=true
     * Les références sont conservées (static final) : un Logger java.util.logging non référencé peut être libéré.
     */
    private static final Logger CDP_VERSION_FINDER = Logger.getLogger("org.openqa.selenium.devtools.CdpVersionFinder");
    private static final Logger CHROMIUM_DRIVER = Logger.getLogger("org.openqa.selenium.chromium.ChromiumDriver");
    private static boolean seleniumLoggingConfigured;

    private DriverFactory() {
    }

    /** Applique (une seule fois) le filtrage ciblé des avertissements CDP, sauf si -Dselenium.cdp.avertissements=true. */
    public static synchronized void configureSeleniumLogging() {
        if (seleniumLoggingConfigured) {
            return;
        }
        seleniumLoggingConfigured = true;
        if (Boolean.getBoolean("selenium.cdp.avertissements")) {
            Log.detail("Avertissements CDP de Selenium conservés (-Dselenium.cdp.avertissements=true)");
            return;
        }
        CDP_VERSION_FINDER.setLevel(Level.SEVERE);
        CHROMIUM_DRIVER.setLevel(Level.SEVERE);
        Log.banner("Note : avertissements « CDP / DevTools » de Selenium 4.27 filtrés (Chrome plus récent que Selenium,"
                + " fonctionnalité non utilisée par le projet). Réactivation : -Dselenium.cdp.avertissements=true");
    }

    public static WebDriver createDriver() {
        configureSeleniumLogging();
        String browser = ConfigReader.get("browser").toLowerCase();
        boolean headless = ConfigReader.getBoolean("headless");
        WebDriver driver;

        switch (browser) {
            case "edge": {
                EdgeOptions options = new EdgeOptions();
                options.addArguments("--lang=fr-FR", "--disable-notifications");
                if (headless) {
                    options.addArguments("--headless=new", "--window-size=1920,1080");
                }
                driver = new EdgeDriver(options);
                break;
            }
            case "firefox": {
                FirefoxOptions options = new FirefoxOptions();
                options.addPreference("intl.accept_languages", "fr-FR");
                if (headless) {
                    options.addArguments("-headless", "--width=1920", "--height=1080");
                }
                driver = new FirefoxDriver(options);
                break;
            }
            case "chrome":
            default: {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--lang=fr-FR", "--disable-notifications");
                if (headless) {
                    options.addArguments("--headless=new", "--window-size=1920,1080");
                }
                driver = new ChromeDriver(options);
                break;
            }
        }

        if (!headless) {
            // Grande fenêtre : certains éléments (ex. total des produits) ne sont visibles qu'en mode « large écran ».
            driver.manage().window().maximize();
        }
        driver.manage().timeouts().implicitlyWait(Duration.ZERO); // on utilise uniquement des attentes explicites
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getInt("page.load.timeout.seconds")));
        Log.detail("Navigateur démarré : " + browser + (headless ? " (headless)" : ""));
        return driver;
    }
}
