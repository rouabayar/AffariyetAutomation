package com.affariyet.pages;

import com.affariyet.utils.Log;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/** Fiche produit (body id="product"). */
public class ProductPage extends BasePage {

    private static final By TITLE = By.cssSelector("h1.page-heading");
    private static final By PRICE = By.cssSelector(".product-prices .current-price-value");
    private static final By REFERENCE = By.cssSelector(".product-reference");
    private static final By QUANTITY = By.id("quantity_wanted");
    private static final By QUANTITY_UP = By.cssSelector(".product-add-to-cart .bootstrap-touchspin-up");
    private static final By ADD_TO_CART = By.cssSelector(".product-add-to-cart button[data-button-action='add-to-cart']");

    public ProductPage(WebDriver driver) {
        super(driver);
        wait.visible(TITLE);
    }

    public String getProductTitle() {
        return text(TITLE);
    }

    public String getPriceText() {
        return normalize(wait.visible(PRICE).getDomProperty("textContent"));
    }

    /** Prix brut fourni par le site dans l'attribut content (ex. "2369"). */
    public String getPriceContentAttribute() {
        return wait.present(PRICE).getDomAttribute("content");
    }

    public boolean isReferenceDisplayed() {
        return isDisplayed(REFERENCE);
    }

    public int getQuantity() {
        return Integer.parseInt(wait.visible(QUANTITY).getDomProperty("value").trim());
    }

    public void increaseQuantity() {
        int before = getQuantity();
        Log.step("Clic sur « + » (quantité actuelle : " + before + ")");
        click(QUANTITY_UP);
        wait.raw().until(d -> getQuantity() == before + 1);
    }

    /** Ajoute au panier puis attend que le compteur de l'en-tête affiche la valeur attendue. */
    public void addToCart(int expectedCartCount) {
        Log.step("Clic sur « Ajouter au panier »");
        click(ADD_TO_CART);
        waitForCartCount(expectedCartCount);
        Log.info("Compteur du panier = " + getCartCount());
    }
}
