package com.affariyet.tests;

import com.affariyet.base.BaseTest;
import com.affariyet.pages.SearchResultsPage;
import com.affariyet.utils.Log;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;

/** TC03, TC04, TC19, TC20 : recherche, pagination et tri. */
public class RechercheTest extends BaseTest {

    @Test(priority = 3, description = "TC03 - Recherche d'un produit existant")
    public void tc03_rechercheProduitExistant() {
        String keyword = data("recherche.existante");

        // Étape 1 : rechercher le mot-clé depuis l'en-tête
        SearchResultsPage results = openHomePage().search(keyword);

        // Étape 2 : vérifier la page et les résultats
        Assert.assertEquals(results.getBodyId(), "search", "La page de résultats n'est pas affichée");
        Assert.assertTrue(results.getCurrentUrl().contains("s=" + keyword), "Le mot-clé n'apparaît pas dans l'URL");
        Assert.assertTrue(results.getDisplayedProductCount() > 0, "Aucun produit affiché");
        int total = results.getAnnouncedTotal();
        Log.info("Nombre de produits annoncé : " + total);
        Assert.assertTrue(total > 0, "Le total annoncé devrait être supérieur à 0");
        Assert.assertTrue(results.getFirstProductName().toLowerCase().contains(keyword.toLowerCase()),
                "Le premier résultat ne contient pas « " + keyword + " » : " + results.getFirstProductName());
    }

    @Test(priority = 4, description = "TC04 - Recherche d'un produit inexistant")
    public void tc04_rechercheProduitInexistant() {
        SearchResultsPage results = openHomePage().search(data("recherche.inexistante"));

        Assert.assertTrue(results.isNoResultMessageDisplayed(), "Le message « aucun produit » n'est pas affiché");
        Assert.assertEquals(results.getNoResultMessage(), data("recherche.vide.message"));
        Assert.assertEquals(results.getDisplayedProductCount(), 0, "Aucun produit ne devrait être affiché");
    }

    @Test(priority = 19, description = "TC19 - Navigation entre les pages de produits (pagination)")
    public void tc19_navigationEntrePagesProduits() {
        SearchResultsPage results = openHomePage().search(data("recherche.existante"));
        List<String> page1 = results.getProductNames();
        Assert.assertTrue(results.getPaginationText().contains("1-" + page1.size()),
                "La pagination de la page 1 est incorrecte : " + results.getPaginationText());

        // Étape : passer à la page 2
        results.goToNextPage();

        List<String> page2 = results.getProductNames();
        Log.info("Pagination page 2 : " + results.getPaginationText());
        Assert.assertTrue(results.getCurrentUrl().contains("page=2"), "L'URL ne contient pas page=2");
        Assert.assertTrue(results.getPaginationText().contains((page1.size() + 1) + "-"),
                "La pagination n'indique pas la 2e page : " + results.getPaginationText());
        Assert.assertFalse(page2.isEmpty(), "La page 2 ne contient aucun produit");
        Assert.assertFalse(page1.contains(page2.get(0)), "Le premier produit de la page 2 figure déjà en page 1");
    }

    @Test(priority = 20, description = "TC20 - Fonctionnalité supplémentaire : tri par prix croissant")
    public void tc20_triParPrixCroissant() {
        SearchResultsPage results = openHomePage().search(data("recherche.existante"));

        results.sortByPriceAscending();

        List<BigDecimal> prices = results.getProductPrices();
        Log.info("Prix après tri : " + prices);
        Assert.assertTrue(prices.size() > 1, "Pas assez de produits pour vérifier le tri");
        for (int i = 1; i < prices.size(); i++) {
            Assert.assertTrue(prices.get(i - 1).compareTo(prices.get(i)) <= 0,
                    "Prix non croissants à la position " + i + " : " + prices.get(i - 1) + " > " + prices.get(i));
        }
    }
}
