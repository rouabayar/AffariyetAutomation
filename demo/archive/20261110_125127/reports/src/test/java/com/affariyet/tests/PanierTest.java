package com.affariyet.tests;

import com.affariyet.base.BaseTest;
import com.affariyet.pages.CartPage;
import com.affariyet.pages.ProductPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC08 – TC12 : panier.
 * Aucune commande ni aucun paiement : on ne dépasse jamais la page panier.
 * Chaque test démarre avec un navigateur neuf, donc un panier vide.
 */
public class PanierTest extends BaseTest {

    /** Ouvre la fiche du premier résultat de la recherche définie dans testdata.csv. */
    private ProductPage openTestProduct() {
        return openHomePage().search(data("recherche.existante")).openFirstProduct();
    }

    @Test(priority = 8, description = "TC08 - Ajout d'un produit au panier")
    public void tc08_ajoutAuPanier() {
        ProductPage product = openTestProduct();
        Assert.assertEquals(product.getCartCount(), 0, "Le panier devrait être vide au départ");

        product.addToCart(1);

        Assert.assertEquals(product.getCartCount(), 1, "Le compteur du panier devrait valoir 1");
    }

    @Test(priority = 9, description = "TC09 - Vérification du contenu du panier")
    public void tc09_verificationPanier() {
        ProductPage product = openTestProduct();
        String name = product.getProductTitle();
        product.addToCart(1);

        CartPage cart = product.openCart();

        Assert.assertEquals(cart.getBodyId(), "cart", "La page panier n'est pas affichée");
        Assert.assertFalse(cart.isEmpty(), "Le panier ne devrait pas être vide");
        Assert.assertTrue(cart.containsProduct(name), "Le produit « " + name + " » n'apparaît pas dans le panier");
    }

    @Test(priority = 10, description = "TC10 - Modification de la quantité")
    public void tc10_modificationQuantite() {
        int target = Integer.parseInt(data("quantite.cible"));
        ProductPage product = openTestProduct();
        Assert.assertEquals(product.getQuantity(), 1, "Quantité initiale inattendue");

        product.increaseQuantity();
        Assert.assertEquals(product.getQuantity(), target, "La quantité n'a pas augmenté");

        product.addToCart(target);
        Assert.assertEquals(product.getCartCount(), target, "Le panier ne contient pas la quantité choisie");
    }

    @Test(priority = 11, description = "TC11 - Suppression d'un produit du panier")
    public void tc11_suppressionProduit() {
        ProductPage product = openTestProduct();
        product.addToCart(1);
        CartPage cart = product.openCart();
        Assert.assertFalse(cart.isEmpty(), "Précondition : le panier devrait contenir un produit");

        cart.deleteFirstLine();

        Assert.assertTrue(cart.isEmpty(), "Le panier devrait être vide après suppression");
        Assert.assertEquals(cart.getEmptyMessage(), data("panier.vide.message"));
    }

    @Test(priority = 12, description = "TC12 - Vérification du panier vide")
    public void tc12_panierVide() {
        CartPage cart = openHomePage().openCart();

        Assert.assertEquals(cart.getCartCount(), 0, "Le compteur du panier devrait valoir 0");
        Assert.assertTrue(cart.isEmpty(), "Le message de panier vide n'est pas affiché");
        Assert.assertEquals(cart.getEmptyMessage(), data("panier.vide.message"));
    }
}
