package com.affariyet.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Inscription : /authentification?create_account=1
 * Formulaire #form_customer fourni par un module « One Page Checkout » (vérifié lors de l'inspection n°2).
 * Le formulaire n'est JAMAIS envoyé par les tests (aucun compte créé).
 */
public class RegistrationPage extends BasePage {

    private static final By FORM = By.id("form_customer");
    private static final By FIRSTNAME = By.id("customer_firstname");
    private static final By LASTNAME = By.id("customer_lastname");
    private static final By EMAIL = By.id("customer_email");
    private static final By SAVE_BUTTON = By.id("btn_save_customer");
    private static final By REQUIRED_FIELDS = By.cssSelector("#form_customer input.required");

    public RegistrationPage(WebDriver driver) {
        super(driver);
        wait.visible(FORM);
    }

    public boolean isFormDisplayed() {
        return isDisplayed(FORM);
    }

    public boolean areIdentityFieldsDisplayed() {
        return isDisplayed(FIRSTNAME) && isDisplayed(LASTNAME) && isDisplayed(EMAIL);
    }

    public boolean isSaveButtonDisplayed() {
        return isDisplayed(SAVE_BUTTON);
    }

    /** id des champs marqués obligatoires (classe CSS « required »). */
    public List<String> getRequiredFieldIds() {
        List<String> ids = new ArrayList<>();
        for (WebElement e : driver.findElements(REQUIRED_FIELDS)) {
            ids.add(e.getDomAttribute("id"));
        }
        return ids;
    }
}
