package com.affariyet.pages;

import com.affariyet.config.ConfigReader;
import com.affariyet.utils.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/** Connexion : /authentification?back=my-account (body id="authentication"). */
public class LoginPage extends BasePage {

    private static final By FORM = By.id("login-form");
    private static final By EMAIL = By.id("field-email");
    private static final By PASSWORD = By.id("field-password");
    private static final By SUBMIT = By.id("submit-login");
    private static final By ERROR = By.cssSelector("li.alert-danger");
    private static final By REGISTER_LINK = By.cssSelector(".no-account a");

    public LoginPage(WebDriver driver) {
        super(driver);
        wait.visible(FORM);
    }

    public static LoginPage open(WebDriver driver) {
        driver.get(ConfigReader.baseUrl() + "authentification?back=my-account");
        return new LoginPage(driver);
    }

    /** Saisit les identifiants puis attend le rechargement de la page (succès ou échec). */
    public void login(String email, String password) {
        Log.step("Connexion avec l'e-mail : " + mask(email)); // e-mail masqué, mot de passe jamais écrit
        type(EMAIL, email);
        type(PASSWORD, password);
        WebElement oldForm = driver.findElement(FORM);
        click(SUBMIT);
        wait.staleness(oldForm);
        wait.pageReady();
    }

    /** Masque une adresse e-mail dans les traces : r.bayar@exemple.tn -> r***@exemple.tn */
    static String mask(String email) {
        int at = email == null ? -1 : email.indexOf('@');
        return at <= 0 ? "***" : email.charAt(0) + "***" + email.substring(at);
    }

    /** Clique sur « Connexion » sans rien saisir (la validation HTML5 doit bloquer l'envoi). */
    public void submitEmpty() {
        Log.step("Clic sur « Connexion » avec des champs vides");
        driver.findElement(SUBMIT).click();
    }

    public boolean isFormDisplayed() {
        return isDisplayed(FORM);
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(ERROR);
    }

    public String getErrorMessage() {
        return text(ERROR);
    }

    public boolean isEmailRequired() {
        return driver.findElement(EMAIL).getDomAttribute("required") != null;
    }

    public boolean isPasswordRequired() {
        return driver.findElement(PASSWORD).getDomAttribute("required") != null;
    }

    /** Résultat de la validation HTML5 du navigateur pour le champ e-mail. */
    public boolean isEmailFieldValid() {
        return Boolean.TRUE.equals(js("return arguments[0].checkValidity();", driver.findElement(EMAIL)));
    }

    public String getEmailValidationMessage() {
        Object msg = js("return arguments[0].validationMessage;", driver.findElement(EMAIL));
        return msg == null ? "" : msg.toString();
    }

    public RegistrationPage goToRegistration() {
        Log.step("Clic sur « Pas de compte ? Créez-en un »");
        clickAndWaitNavigation(REGISTER_LINK);
        return new RegistrationPage(driver);
    }
}
