package com.affariyet.tests;

import com.affariyet.base.BaseTest;
import com.affariyet.pages.LoginPage;
import com.affariyet.pages.RegistrationPage;
import com.affariyet.utils.Log;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.List;

/** TC13 – TC16 : connexion et inscription. Aucun compte n'est créé par les tests. */
public class CompteTest extends BaseTest {

    @Test(priority = 13, description = "TC13 - Connexion valide")
    public void tc13_connexionValide() {
        // Aucun vrai mot de passe dans le projet : les identifiants viennent de l'environnement.
        String email = credential("AFFARIYET_EMAIL", "affariyet.email");
        String password = credential("AFFARIYET_PASSWORD", "affariyet.password");
        if (email == null || password == null) {
            throw new SkipException("Identifiants absents : définir AFFARIYET_EMAIL et AFFARIYET_PASSWORD "
                    + "(compte de test personnel) pour exécuter ce test");
        }

        LoginPage login = openHomePage().goToLogin();
        login.login(email, password);

        Assert.assertTrue(login.getCurrentUrl().contains("mon-compte"),
                "Pas de redirection vers « Mon compte » : " + login.getCurrentUrl());
        Assert.assertFalse(login.isFormDisplayed(), "Le formulaire de connexion est toujours affiché");
    }

    @Test(priority = 14, description = "TC14 - Connexion invalide")
    public void tc14_connexionInvalide() {
        LoginPage login = openHomePage().goToLogin();

        login.login(data("login.invalide.email"), data("login.invalide.motdepasse"));

        Assert.assertTrue(login.isErrorDisplayed(), "Aucun message d'erreur affiché");
        Assert.assertEquals(login.getErrorMessage(), data("login.erreur.attendue"));
        Assert.assertTrue(login.getCurrentUrl().contains("authentification"), "L'utilisateur a quitté la page de connexion");
        Assert.assertTrue(login.isFormDisplayed(), "Le formulaire de connexion devrait rester affiché");
    }

    @Test(priority = 15, description = "TC15 - Accès à la page d'inscription")
    public void tc15_accesInscription() {
        RegistrationPage register = openHomePage().goToLogin().goToRegistration();

        Assert.assertTrue(register.getCurrentUrl().contains("create_account=1"), "URL d'inscription inattendue");
        Assert.assertTrue(register.isFormDisplayed(), "Le formulaire d'inscription n'est pas affiché");
        Assert.assertTrue(register.areIdentityFieldsDisplayed(), "Prénom / nom / e-mail non affichés");
        Assert.assertTrue(register.isSaveButtonDisplayed(), "Le bouton « Enregistrer les informations » est absent");
    }

    @Test(priority = 16, description = "TC16 - Validation des champs obligatoires")
    public void tc16_validationChampsObligatoires() {
        // Partie 1 : formulaire de connexion envoyé vide
        LoginPage login = openHomePage().goToLogin();
        Assert.assertTrue(login.isEmailRequired() && login.isPasswordRequired(), "Les champs devraient être obligatoires");

        String urlBefore = login.getCurrentUrl();
        login.submitEmpty();

        Assert.assertFalse(login.isEmailFieldValid(), "Le champ e-mail vide devrait être invalide");
        Log.info("Message de validation du navigateur : " + login.getEmailValidationMessage());
        Assert.assertFalse(login.getEmailValidationMessage().isEmpty(), "Aucun message de validation");
        Assert.assertEquals(login.getCurrentUrl(), urlBefore, "Le formulaire vide ne devrait pas être envoyé");
        Assert.assertFalse(login.isErrorDisplayed(), "Le serveur n'aurait pas dû être appelé");

        // Partie 2 : champs obligatoires du formulaire d'inscription (sans l'envoyer)
        RegistrationPage register = login.goToRegistration();
        List<String> required = register.getRequiredFieldIds();
        Log.info("Champs obligatoires de l'inscription : " + required);
        Assert.assertTrue(required.containsAll(List.of("customer_firstname", "customer_lastname", "customer_email")),
                "Prénom, nom et e-mail devraient être obligatoires : " + required);
    }

    private static String credential(String envName, String propertyName) {
        String value = System.getProperty(propertyName, System.getenv(envName));
        return (value == null || value.isBlank()) ? null : value;
    }
}
