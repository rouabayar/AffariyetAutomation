package com.affariyet.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Attentes explicites centralisées (aucun Thread.sleep dans le projet).
 */
public class WaitUtils {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public WaitUtils(WebDriver driver, int timeoutSeconds) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
    }

    /**
     * Page prête = document chargé ET barre de chargement Pace.js terminée.
     * Affariyet ajoute la classe « pace-running » sur <body> pendant le chargement :
     * tant qu'elle est présente, les clics sont interceptés (constaté lors de l'inspection n°2).
     */
    public void pageReady() {
        wait.until(d -> {
            Object ready = ((JavascriptExecutor) d).executeScript(
                    "return document.readyState === 'complete' && "
                            + "!(document.body && document.body.classList.contains('pace-running'));");
            return Boolean.TRUE.equals(ready);
        });
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
