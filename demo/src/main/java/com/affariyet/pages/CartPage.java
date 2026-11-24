package com.affariyet.pages;

import com.affariyet.utils.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Panier : /panier?action=show (body id="cart"). */
public class CartPage extends BasePage {

    private static final By OVERVIEW = By.cssSelector(".cart-grid .cart-overview");
    private static final By EMPTY_MESSAGE = By.cssSelector(".cart-grid .no-items");
    /**
     * Lien de suppression d'une ligne du panier.
     * Attribut data-link-action="delete-from-cart" : crochet JavaScript standard de PrestaShop.
     * Le panier rempli n'a pas encore pu être inspecté : si TC11 échoue ici,
     * le HTML enregistré dans screenshots/ permettra de corriger ce sélecteur.
     */
    private static final By DELETE_LINE = By.cssSelector(".cart-grid [data-link-action='delete-from-cart']");

    public CartPage(WebDriver driver) {
        super(driver);
        wait.visible(OVERVIEW);
    }

    public boolean isEmpty() {
        return isDisplayed(EMPTY_MESSAGE);
    }

    public String getEmptyMessage() {
        return text(EMPTY_MESSAGE);
    }

    public String getOverviewText() {
        return text(OVERVIEW);
    }

    public boolean containsProduct(String productName) {
        return getOverviewText().toLowerCase().contains(normalize(productName).toLowerCase());
    }

    public void deleteFirstLine() {
        Log.step("Suppression de la première ligne du panier");
        click(DELETE_LINE);
        wait.visible(EMPTY_MESSAGE); // le panier est rafraîchi en AJAX
    }
}
