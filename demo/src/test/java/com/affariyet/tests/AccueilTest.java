package com.affariyet.tests;

import com.affariyet.base.BaseTest;
import com.affariyet.pages.HomePage;
import com.affariyet.utils.Log;
import org.testng.Assert;
import org.testng.annotations.Test;

/** TC01 – TC02 : page d'accueil. */
public class AccueilTest extends BaseTest {

    @Test(priority = 1, description = "TC01 - Accès à la page d'accueil")
    public void tc01_accesPageAccueil() {
        // Étape 1 : ouvrir le site
        HomePage home = openHomePage();

        // Étape 2 : vérifier qu'on est bien sur l'accueil et que les éléments principaux sont visibles
        Assert.assertEquals(home.getBodyId(), "index", "La page affichée n'est pas la page d'accueil");
        Assert.assertTrue(home.isLogoDisplayed(), "Le logo Affariyet n'est pas affiché");
        Assert.assertTrue(home.isSearchFieldDisplayed(), "Le champ de recherche n'est pas affiché");
    }

    @Test(priority = 2, description = "TC02 - Vérification du titre de la page d'accueil")
    public void tc02_verificationTitre() {
        HomePage home = openHomePage();

        String title = home.getTitle();
        Log.info("Titre de la page : " + title);
        Assert.assertTrue(title.contains("Affariyet"), "Le titre ne contient pas « Affariyet » : " + title);
        Assert.assertTrue(title.contains("Vente en Ligne Tunisie"), "Le titre ne décrit pas l'activité du site : " + title);
    }
}
