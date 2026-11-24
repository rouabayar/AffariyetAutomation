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

/**
 * Création du navigateur selon config.properties.
 * Selenium Manager (inclus dans Selenium 4.27) télécharge automatiquement le bon driver.
 */
public final class DriverFactory {

    private DriverFactory() {
    }

    public static WebDriver createDriver() {
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
        Log.info("Navigateur démarré : " + browser + (headless ? " (headless)" : ""));
        return driver;
    }
}
