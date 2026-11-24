package com.affariyet.tests;

import com.affariyet.base.BaseTest;
import com.affariyet.pages.CategoryPage;
import com.affariyet.pages.ProductPage;
import com.affariyet.pages.SearchResultsPage;
import com.affariyet.utils.Log;
import com.affariyet.utils.PriceUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;

/** TC05 – TC07 : catégories, fiche produit et prix. */
public class CatalogueTest extends BaseTest {

    @Test(priority = 5, description = "TC05 - Navigation entre catégories")
    public void tc05_navigationEntreCategories() {
        String cat1 = data("categorie.1");
        String cat2 = data("categorie.2");

        // Étape 1 : première catégorie depuis le pied de page
        CategoryPage first = openHomePage().goToFooterCategory(cat1);
        Assert.assertEquals(first.getBodyId(), "category", "La page catégorie n'est pas affichée");
        // Le thème affiche le titre en majuscules (CSS) : comparaison sans tenir compte de la casse
        Assert.assertTrue(first.getHeading().equalsIgnoreCase(cat1), "Titre de catégorie inattendu : " + first.getHeading());
        Assert.assertEquals(first.getBreadcrumbLast(), cat1, "Fil d'Ariane inattendu");
        Assert.assertTrue(first.getDisplayedProductCount() > 0, "Aucun produit dans « " + cat1 + " »");
        String firstId = first.getCategoryId();

        // Étape 2 : passer directement à une autre catégorie
        CategoryPage second = first.goToFooterCategory(cat2);
        Assert.assertTrue(second.getHeading().equalsIgnoreCase(cat2), "Titre de catégorie inattendu : " + second.getHeading());
        Assert.assertEquals(second.getBreadcrumbLast(), cat2, "Fil d'Ariane inattendu");
        Assert.assertTrue(second.getDisplayedProductCount() > 0, "Aucun produit dans « " + cat2 + " »");
        Assert.assertNotEquals(second.getCategoryId(), firstId, "La catégorie n'a pas changé");
    }

    @Test(priority = 6, description = "TC06 - Consultation d'un produit")
    public void tc06_consultationProduit() {
        SearchResultsPage results = openHomePage().search(data("recherche.existante"));
        String nameInList = results.getFirstProductName();

        ProductPage product = results.openFirstProduct();

        Assert.assertEquals(product.getBodyId(), "product", "La fiche produit n'est pas affichée");
        Assert.assertEquals(product.getProductTitle(), nameInList, "Le titre de la fiche ne correspond pas au produit cliqué");
        Assert.assertTrue(product.isReferenceDisplayed(), "La référence du produit n'est pas affichée");
    }

    @Test(priority = 7, description = "TC07 - Vérification du prix d'un produit")
    public void tc07_verificationPrix() {
        SearchResultsPage results = openHomePage().search(data("recherche.existante"));
        String listPrice = results.getFirstProductPriceText();

        ProductPage product = results.openFirstProduct();
        String productPrice = product.getPriceText();
        Log.info("Prix liste = " + listPrice + " | prix fiche = " + productPrice);

        // Format « 2 369,000 TND », prix > 0, identique entre la liste et la fiche
        Assert.assertTrue(PriceUtils.hasValidFormat(productPrice), "Format de prix inattendu : " + productPrice);
        BigDecimal value = PriceUtils.parse(productPrice);
        Assert.assertTrue(value.signum() > 0, "Le prix doit être positif");
        Assert.assertEquals(value.compareTo(PriceUtils.parse(listPrice)), 0, "Prix différent entre la liste et la fiche");
        Assert.assertEquals(new BigDecimal(product.getPriceContentAttribute()).compareTo(value), 0,
                "Le prix affiché ne correspond pas à la valeur technique (attribut content)");
    }
}
