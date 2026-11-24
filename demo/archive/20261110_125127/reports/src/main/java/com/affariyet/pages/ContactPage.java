package com.affariyet.pages;

import com.affariyet.utils.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;

/**
 * Contact : /contactez-nous (body id="contact").
 * Les tests remplissent le formulaire mais ne cliquent JAMAIS sur « Envoyer ».
 */
public class ContactPage extends BasePage {

    private static final By FORM = By.cssSelector("form[action$='contactez-nous']");
    private static final By SUBJECT = By.id("id_contact");
    private static final By EMAIL = By.cssSelector("form[action$='contactez-nous'] input[name='from']");
    private static final By MESSAGE = By.id("contactform-message");
    private static final By SUBMIT = By.cssSelector("form[action$='contactez-nous'] input[name='submitMessage']");
    private static final By CONSENT = By.cssSelector("form[action$='contactez-nous'] input[name='psgdpr_consent_checkbox']");
    private static final By CONTACT_INFO = By.cssSelector(".contact-rich");

    public ContactPage(WebDriver driver) {
        super(driver);
        wait.visible(FORM);
    }

    public boolean areFieldsDisplayed() {
        return isDisplayed(SUBJECT) && isDisplayed(EMAIL) && isDisplayed(MESSAGE);
    }

    public List<String> getSubjects() {
        List<String> subjects = new ArrayList<>();
        for (WebElement option : new Select(driver.findElement(SUBJECT)).getOptions()) {
            subjects.add(normalize(option.getText()));
        }
        return subjects;
    }

    public String getContactInfoText() {
        return text(CONTACT_INFO);
    }

    public void fillForm(String email, String message) {
        Log.step("Remplissage du formulaire de contact (sans envoi)");
        type(EMAIL, email);
        type(MESSAGE, message);
    }

    public boolean isSubmitEnabled() {
        return driver.findElement(SUBMIT).isEnabled();
    }

    /** La case est stylisée par le thème : on la coche par JavaScript (comportement vérifié lors de l'inspection n°2). */
    public void acceptConsent() {
        Log.step("Cochage de la case de consentement");
        WebElement box = driver.findElement(CONSENT);
        if (!box.isSelected()) {
            js("arguments[0].click();", box);
        }
        wait.raw().until(d -> d.findElement(SUBMIT).isEnabled());
    }

    public boolean isConsentChecked() {
        return driver.findElement(CONSENT).isSelected();
    }
}
