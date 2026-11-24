package com.affariyet.base;

import com.affariyet.pages.HomePage;
import com.affariyet.utils.CsvDataReader;
import com.affariyet.utils.DriverFactory;
import com.affariyet.utils.Log;
import com.affariyet.utils.ScreenshotUtils;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.lang.reflect.Method;
import java.nio.file.Path;

/**
 * Base de tous les tests :
 *  - un navigateur neuf pour chaque test (tests indépendants, panier vide au départ) ;
 *  - capture d'écran + HTML automatiques en cas d'échec ;
 *  - fermeture systématique du navigateur.
 */
public abstract class BaseTest {

    /** Nom de l'attribut TestNG qui transporte le chemin de la capture vers le rapport HTML. */
    public static final String SCREENSHOT_ATTRIBUTE = "screenshot";

    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp(Method method) {
        Log.info("========== DÉBUT : " + method.getName() + " ==========");
        driver = DriverFactory.createDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            if (result.getStatus() == ITestResult.FAILURE) {
                Path png = ScreenshotUtils.capture(driver, result.getMethod().getMethodName());
                if (png != null) {
                    result.setAttribute(SCREENSHOT_ATTRIBUTE, png.toAbsolutePath().toString());
                }
            }
        } finally {
            if (driver != null) {
                driver.quit();
                driver = null;
                Log.info("Navigateur fermé");
            }
            Log.info("========== FIN : " + result.getMethod().getMethodName() + " ==========");
        }
    }

    protected HomePage openHomePage() {
        return new HomePage(driver).open();
    }

    /** Donnée du fichier test-data/testdata.csv. */
    protected String data(String key) {
        return CsvDataReader.get(key);
    }
}
