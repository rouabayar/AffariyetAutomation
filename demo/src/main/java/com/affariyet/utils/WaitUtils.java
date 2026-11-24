package com.affariyet.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Attentes explicites centralisées (aucun Thread.sleep dans le projet).
 */
public class WaitUtils {

    /** Marqueur posé dans window quand Pace.js a déjà été signalé bloqué : il disparaît avec le document. */
    private static final String PACE_MARKER = "__affariyetPaceSignale";

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final Duration timeout;

    public WaitUtils(WebDriver driver, int timeoutSeconds) {
        this.driver = driver;
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.wait = new WebDriverWait(driver, timeout);
    }

    /**
     * Attente de fin de chargement, en deux étapes (correctif du 10/11/2026 après l'échec de TC04 à 12:52) :
     *
     *  1. BLOQUANT — document.readyState = « complete » en {@code timeout} s, sinon TimeoutException
     *     avec un message explicite (état réel et URL).
     *  2. NON BLOQUANT — barre Pace.js (classe « pace-running » sur body, qui intercepte les clics :
     *     constaté lors de l'inspection n°2). Tant qu'aucun avertissement n'a été émis pour le document
     *     courant, Pace est attendu à CHAQUE appel (comportement inchangé). S'il tourne encore après
     *     {@code timeout} s : un seul avertissement [AVERT] pour ce document, un marqueur est posé dans
     *     window, et les appels suivants sur CE document ne vérifient plus Pace (étape 1 seulement).
     *     Un nouveau document (navigation) efface le marqueur. Les attentes bloquantes des éléments
     *     (cliquable, visible, URL…) restent appliquées par chaque action ; un clic intercepté par Pace
     *     passe par le repli JavaScript tracé de BasePage.click().
     */
    public void pageReady() {
        new WebDriverWait(driver, timeout)
                .withMessage(() -> "la page n'a pas fini de se charger en " + timeout.toSeconds()
                        + " s (document.readyState = " + readyState() + ", URL = " + currentUrl() + ")")
                .until(d -> "complete".equals(readyState()));

        if (paceAlreadyReported()) {
            return;
        }
        try {
            new WebDriverWait(driver, timeout).until(d -> !paceRunning());
        } catch (TimeoutException e) {
            markPaceReported();
            Log.warn("Pace.js toujours actif " + timeout.toSeconds() + " s après le chargement de " + currentUrl()
                    + " — poursuite avec les attentes bloquantes des éléments (Pace n'est plus vérifié sur cette page)");
        }
    }

    private String readyState() {
        try {
            Object s = ((JavascriptExecutor) driver).executeScript("return document.readyState;");
            return String.valueOf(s);
        } catch (WebDriverException e) {
            return "indisponible"; // document en cours de remplacement : on réessaie
        }
    }

    private boolean paceRunning() {
        try {
            return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(
                    "return !!(document.body && document.body.classList.contains('pace-running'));"));
        } catch (WebDriverException e) {
            return true; // état illisible : considéré comme toujours en cours
        }
    }

    private boolean paceAlreadyReported() {
        try {
            return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(
                    "return window." + PACE_MARKER + " === true;"));
        } catch (WebDriverException e) {
            return false;
        }
    }

    private void markPaceReported() {
        try {
            ((JavascriptExecutor) driver).executeScript("window." + PACE_MARKER + " = true;");
        } catch (WebDriverException e) {
            // marqueur non posé : au pire, un second avertissement sera émis pour cette page
        }
    }

    private String currentUrl() {
        try {
            return driver.getCurrentUrl();
        } catch (WebDriverException e) {
            return "indisponible";
        }
    }

    public WebElement visible(By by) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    public WebElement present(By by) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(by));
    }

    public WebElement clickable(By by) {
        return wait.until(ExpectedConditions.elementToBeClickable(by));
    }

    public List<WebElement> allVisible(By by) {
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(by));
    }

    public void urlContains(String fragment) {
        wait.until(ExpectedConditions.urlContains(fragment));
    }

    /** Attend que le paramètre « s » de l'URL, décodé, soit exactement le mot-clé (comparaison NFC). */
    public void searchParameterIs(String keyword) {
        new WebDriverWait(driver, timeout)
                .withMessage(() -> "la page de résultats de la recherche « " + keyword + " » ne s'est pas affichée en "
                        + timeout.toSeconds() + " s (paramètre s lu = « "
                        + UrlUtils.queryParameter(currentUrl(), "s") + " », URL = " + currentUrl() + ")")
                .until(d -> UrlUtils.sameText(UrlUtils.queryParameter(currentUrl(), "s"), keyword));
    }

    public void urlChangesFrom(String previousUrl) {
        wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(previousUrl)));
    }

    public void textPresent(By by, String text) {
        wait.until(ExpectedConditions.textToBePresentInElementLocated(by, text));
    }

    /** Attend qu'un élément disparaisse du DOM (ex. liste rechargée en AJAX). Pas d'erreur si cela n'arrive pas. */
    public boolean staleness(WebElement element) {
        try {
            wait.until(ExpectedConditions.stalenessOf(element));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public WebDriverWait raw() {
        return wait;
    }
}
