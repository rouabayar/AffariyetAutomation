package com.affariyet.pages;

import com.affariyet.config.ConfigReader;
import com.affariyet.utils.Log;
import com.affariyet.utils.WaitUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Classe mère de toutes les pages : actions communes + éléments de l'en-tête et du pied de page,
 * présents sur toutes les pages d'Affariyet (sélecteurs vérifiés lors des inspections).
 */
public abstract class BasePage {

    // ---- En-tête
    protected static final By LOGO = By.cssSelector("img.logo");
    protected static final By SEARCH_INPUT = By.cssSelector("input[name='s']");
    protected static final By SEARCH_BUTTON = By.cssSelector("form[action*='recherche'] button[type='submit']");
    protected static final By LOGIN_LINK = By.cssSelector("a[title='Identifiez-vous']");
    protected static final By CART_COUNT = By.cssSelector(".cart-products-count");
    protected static final By MENU_CONTACT = By.cssSelector("a.ets_mm_url[href$='contactez-nous']");
    // ---- Fil d'Ariane
    protected static final By BREADCRUMB_LAST = By.cssSelector(".breadcrumb li:last-child .item-name");
    // ---- Pied de page
    protected static final By FOOTER_DELIVERY = By.cssSelector("#footer a[title='Livraison']");

    protected final WebDriver driver;
    protected final WaitUtils wait;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver, ConfigReader.getInt("timeout.seconds"));
    }

    // ------------------------------------------------------------ actions génériques

    protected void openUrl(String url) {
        Log.step("Ouverture de " + url);
        driver.get(url);
        wait.pageReady();
    }

    /** Clic robuste : attend la fin du chargement, centre l'élément, puis clique (repli JavaScript si intercepté). */
    protected void click(By by) {
        wait.pageReady();
        WebElement element = wait.present(by);
        scrollIntoView(element);
        try {
            wait.clickable(by).click();
        } catch (ElementClickInterceptedException e) {
            Log.info("Clic intercepté sur " + by + " -> nouvel essai via JavaScript");
            wait.pageReady();
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }

    /** Clic qui provoque le chargement d'une nouvelle URL. */
    protected void clickAndWaitNavigation(By by) {
        String before = driver.getCurrentUrl();
        click(by);
        wait.urlChangesFrom(before);
        wait.pageReady();
    }

    protected void type(By by, String text) {
        WebElement element = wait.visible(by);
        element.clear();
        element.sendKeys(text);
    }

    protected String text(By by) {
        return normalize(wait.visible(by).getText());
    }

    protected boolean isDisplayed(By by) {
        List<WebElement> elements = driver.findElements(by);
        return !elements.isEmpty() && elements.get(0).isDisplayed();
    }

    protected void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);
    }

    protected Object js(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    /** Remplace les espaces multiples / insécables par un espace simple. */
    public static String normalize(String s) {
        return s == null ? "" : s.replaceAll("[\\s\\u00A0\\u202F]+", " ").trim();
    }

    // ------------------------------------------------------------ informations de page

    public String getTitle() {
        return driver.getTitle();
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    /** id de la balise body : index, search, category, product, cart, cms, contact, authentication... */
    public String getBodyId() {
        return driver.findElement(By.tagName("body")).getDomAttribute("id");
    }

    public String getBreadcrumbLast() {
        return text(BREADCRUMB_LAST);
    }

    // ------------------------------------------------------------ en-tête

    public boolean isLogoDisplayed() {
        return isDisplayed(LOGO);
    }

    public boolean isSearchFieldDisplayed() {
        return isDisplayed(SEARCH_INPUT);
    }

    public SearchResultsPage search(String keyword) {
        Log.step("Recherche du mot-clé : " + keyword);
        type(SEARCH_INPUT, keyword);
        clickAndWaitNavigation(SEARCH_BUTTON);
        wait.urlContains("recherche");
        return new SearchResultsPage(driver);
    }

    /** Valeur du compteur du panier dans l'en-tête. */
    public int getCartCount() {
        String value = wait.present(CART_COUNT).getDomProperty("textContent");
        return Integer.parseInt(value == null ? "0" : value.trim());
    }

    /** Attend que le compteur du panier (mis à jour en AJAX) atteigne la valeur attendue. */
    public void waitForCartCount(int expected) {
        wait.raw().until(d -> {
            try {
                return getCartCount() == expected;
            } catch (RuntimeException e) {
                return false; // élément en cours de rafraîchissement
            }
        });
    }

    public CartPage openCart() {
        openUrl(ConfigReader.baseUrl() + "panier?action=show");
        return new CartPage(driver);
    }

    public LoginPage goToLogin() {
        Log.step("Clic sur « Connexion » dans l'en-tête");
        clickAndWaitNavigation(LOGIN_LINK);
        return new LoginPage(driver);
    }

    public ContactPage goToContactFromMenu() {
        Log.step("Clic sur « CONTACT » dans le menu");
        clickAndWaitNavigation(MENU_CONTACT);
        return new ContactPage(driver);
    }

    // ------------------------------------------------------------ pied de page

    public CmsPage goToDeliveryFromFooter() {
        Log.step("Clic sur « Livraison » dans le pied de page");
        clickAndWaitNavigation(FOOTER_DELIVERY);
        return new CmsPage(driver);
    }

    /** Liens de catégories du pied de page (« Les must have du moment »), ex. « Réfrigérateurs ». */
    public CategoryPage goToFooterCategory(String linkText) {
        Log.step("Clic sur la catégorie « " + linkText + " » dans le pied de page");
        clickAndWaitNavigation(By.xpath("//footer[@id='footer']//a[normalize-space()=\"" + linkText + "\"]"));
        return new CategoryPage(driver);
    }
}
