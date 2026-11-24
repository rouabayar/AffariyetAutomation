package com.affariyet.pages;

import com.affariyet.utils.Log;
import com.affariyet.utils.PriceUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Partie commune aux listes de produits (résultats de recherche et pages catégorie).
 * Même gabarit PrestaShop/ZOneTheme sur les deux pages (vérifié).
 */
public abstract class ProductListPage extends BasePage {

    protected static final By MINIATURES = By.cssSelector("#js-product-list .product-miniature");
    protected static final By PRODUCT_NAME_LINKS = By.cssSelector("#js-product-list .product-miniature .product-name a");
    protected static final By PRODUCT_PRICES = By.cssSelector("#js-product-list .product-miniature .price");
    protected static final By TOTAL_PRODUCTS = By.cssSelector(".total-products");
    protected static final By PAGINATION = By.cssSelector("nav.pagination");
    protected static final By NEXT_PAGE = By.cssSelector("nav.pagination a.next");
    protected static final By SORT_BUTTON = By.cssSelector(".products-sort-order .select-title");
    protected static final By SORT_PRICE_ASC = By.cssSelector(".products-sort-order a[href*='order=product.price.asc']");

    private static final Pattern TOTAL_PATTERN = Pattern.compile("(\\d+)\\s+produit");

    protected ProductListPage(WebDriver driver) {
        super(driver);
    }

    public int getDisplayedProductCount() {
        return driver.findElements(MINIATURES).size();
    }

    /** Nombre annoncé par « Il y a N produits. » */
    public int getAnnouncedTotal() {
        String text = text(TOTAL_PRODUCTS);
        Matcher m = TOTAL_PATTERN.matcher(text);
        if (!m.find()) {
            throw new IllegalStateException("Total des produits illisible : '" + text + "'");
        }
        return Integer.parseInt(m.group(1));
    }

    public List<String> getProductNames() {
        List<String> names = new ArrayList<>();
        for (WebElement a : wait.allVisible(PRODUCT_NAME_LINKS)) {
            names.add(normalize(a.getText()));
        }
        return names;
    }

    public String getFirstProductName() {
        return normalize(wait.visible(PRODUCT_NAME_LINKS).getText());
    }

    public String getFirstProductPriceText() {
        return normalize(wait.visible(PRODUCT_PRICES).getDomProperty("textContent"));
    }

    public List<BigDecimal> getProductPrices() {
        List<BigDecimal> prices = new ArrayList<>();
        for (WebElement p : wait.allVisible(PRODUCT_PRICES)) {
            prices.add(PriceUtils.parse(p.getDomProperty("textContent")));
        }
        return prices;
    }

    public ProductPage openFirstProduct() {
        Log.step("Ouverture du premier produit : " + getFirstProductName());
        clickAndWaitNavigation(PRODUCT_NAME_LINKS);
        return new ProductPage(driver);
    }

    public String getPaginationText() {
        return text(PAGINATION);
    }

    /** Clic sur « Suivant ». La liste est rechargée en AJAX : on attend le remplacement des anciens produits. */
    public void goToNextPage() {
        Log.step("Clic sur « Suivant » (pagination)");
        WebElement firstBefore = wait.visible(MINIATURES);
        click(NEXT_PAGE);
        wait.urlContains("page=2");
        wait.staleness(firstBefore);
        wait.pageReady();
        wait.allVisible(MINIATURES);
    }

    /** Tri « Prix, croissant » via la liste déroulante « Trier par ». */
    public void sortByPriceAscending() {
        Log.step("Tri par prix croissant");
        WebElement firstBefore = wait.visible(MINIATURES);
        click(SORT_BUTTON);
        click(SORT_PRICE_ASC);
        wait.urlContains("order=product.price.asc");
        wait.staleness(firstBefore);
        wait.pageReady();
        wait.allVisible(MINIATURES);
    }
}
