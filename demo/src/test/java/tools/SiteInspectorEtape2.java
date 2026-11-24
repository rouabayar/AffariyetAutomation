package tools;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

/**
 * Inspection n°2 (PAS un des 20 tests) : pages que l'inspection n°1 n'a pas pu voir.
 *  - pages catégorie + pagination
 *  - recherche sans résultat (avec une vraie attente de navigation)
 *  - panier contenant un produit (ajout au panier = aucune commande, aucun paiement)
 *  - résultat d'une connexion avec des identifiants FICTIFS
 *  - formulaire d'inscription (ouvert, jamais envoyé)
 *  - case de consentement du formulaire contact (le message n'est PAS envoyé)
 *
 * Lancement :  mvn test -Dtest=SiteInspectorEtape2
 * Résultat  :  reports/site-inspection-2/
 */
public class SiteInspectorEtape2 {

    private static final String BASE_URL = "https://www.affariyet.com/";
    private static final Path OUT_DIR = Paths.get("reports", "site-inspection-2");

    private WebDriver driver;
    private WebDriverWait wait;
    private final StringBuilder report = new StringBuilder();

    private static final String DUMP_JS = String.join("\n",
        "const out = [];",
        "const attrs = e => !e ? '' : ['id','name','type','class','placeholder','href','action','method','value','aria-label','title','disabled','required','data-link-action','data-button-action','data-id-product','content']",
        "  .filter(a => e.hasAttribute(a)).map(a => a + '=\"' + String(e.getAttribute(a)).trim().slice(0,120) + '\"').join(' ');",
        "const txt = e => (e.innerText || e.textContent || '').replace(/\\s+/g,' ').trim().slice(0,100);",
        "const vis = e => !!(e.offsetWidth || e.offsetHeight || e.getClientRects().length);",
        "out.push('URL: ' + location.href);",
        "out.push('TITLE: ' + document.title);",
        "out.push('BODY: ' + attrs(document.body));",
        "out.push('--- TITRES H1/H2 ---');",
        "document.querySelectorAll('h1,h2').forEach((e,i) => { if (i < 15) out.push(e.tagName + (vis(e)?' (visible)':' (cache)') + ' [' + txt(e) + '] ' + attrs(e)); });",
        "out.push('--- FORMS ---');",
        "document.querySelectorAll('form').forEach((f,i) => out.push('form#' + i + ' ' + attrs(f)));",
        "out.push('--- INPUTS / SELECT / TEXTAREA (hors champs caches) ---');",
        "document.querySelectorAll('input,select,textarea').forEach(e => { if (e.type !== 'hidden') out.push(e.tagName + (vis(e)?' (visible)':' (cache)') + ' ' + attrs(e)); });",
        "out.push('--- BUTTONS / LIENS-BOUTONS ---');",
        "document.querySelectorAll('button,input[type=submit],a.btn,a[data-link-action]').forEach((e,i) => { if (i < 60) out.push(e.tagName + (vis(e)?' (visible)':' (cache)') + ' [' + txt(e) + '] ' + attrs(e)); });",
        "out.push('--- ELEMENTS PANIER (class contient cart) ---');",
        "document.querySelectorAll('[class*=cart]').forEach((e,i) => { if (i < 60) out.push(e.tagName + (vis(e)?' (visible)':' (cache)') + ' [' + txt(e).slice(0,60) + '] ' + attrs(e)); });",
        "out.push('--- MODALES ---');",
        "document.querySelectorAll('.modal, [id*=modal], [class*=modal]').forEach((e,i) => { if (i < 20) out.push(e.tagName + (vis(e)?' (visible)':' (cache)') + ' [' + txt(e).slice(0,80) + '] ' + attrs(e)); });",
        "out.push('--- ELEMENTS PRODUIT (10 premiers) ---');",
        "document.querySelectorAll('.product-miniature').forEach((e,i) => { if (i < 10) out.push(attrs(e) + ' | nom: ' + txt(e.querySelector('.product-name')) + ' | prix: ' + txt(e.querySelector('.price'))); });",
        "out.push('--- PAGINATION / TRI / TOTAL ---');",
        "document.querySelectorAll('.pagination a, .total-products, .products-sort-order a, .showing').forEach((e,i) => { if (i < 25) out.push(e.tagName + ' [' + txt(e) + '] ' + attrs(e)); });",
        "out.push('--- MESSAGES / ALERTES / ERREURS ---');",
        "document.querySelectorAll('.alert,[class*=error],.help-block,.invalid-feedback,[role=alert],.page-not-found').forEach((e,i) => { if (i < 25) out.push(e.tagName + (vis(e)?' (visible)':' (cache)') + ' [' + txt(e) + '] ' + attrs(e)); });",
        "out.push('--- FIL D ARIANE ---');",
        "document.querySelectorAll('.breadcrumb li').forEach(e => out.push('LI [' + txt(e) + '] ' + attrs(e.querySelector('a'))));",
        "return out.join('\\n');"
    );

    @BeforeClass
    public void setUp() throws IOException {
        Files.createDirectories(OUT_DIR);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized", "--lang=fr-FR");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @Test
    public void inspecterPagesComplementaires() throws IOException {
        etape("Catégories + pagination", this::categories);
        etape("Recherche sans résultat", this::rechercheSansResultat);
        etape("Panier avec un produit", this::panierAvecProduit);
        etape("Connexion invalide", this::connexionInvalide);
        etape("Inscription", this::inscription);
        etape("Contact (consentement)", this::contactConsentement);
        Files.writeString(OUT_DIR.resolve("inspection-2.txt"), report.toString(), StandardCharsets.UTF_8);
        System.out.println("Inspection 2 terminée -> " + OUT_DIR.toAbsolutePath());
    }

    // ------------------------------------------------------------ scénarios

    private void categories() throws IOException {
        open(BASE_URL);
        clickAndWaitNavigation(By.xpath("//footer[@id='footer']//a[normalize-space()='Réfrigérateurs']"));
        snapshot("10-categorie-refrigerateurs");

        clickAndWaitNavigation(By.xpath("//footer[@id='footer']//a[normalize-space()='Lave Linge']"));
        snapshot("11-categorie-lave-linge");

        List<WebElement> next = driver.findElements(By.cssSelector("nav.pagination a.next"));
        if (!next.isEmpty()) {
            clickAndWaitNavigation(By.cssSelector("nav.pagination a.next"));
            // la liste est rechargée en AJAX : on attend un peu que l'URL contienne page=2
            try {
                wait.until(ExpectedConditions.urlContains("page=2"));
            } catch (Exception e) {
                log("!! URL sans page=2 après clic sur Suivant");
            }
            snapshot("12-categorie-page-2");
        } else {
            log("!! Pas de bouton 'Suivant' sur la catégorie Lave Linge");
        }
    }

    private void rechercheSansResultat() throws IOException {
        open(BASE_URL);
        WebElement s = driver.findElement(By.cssSelector("input[name='s']"));
        s.sendKeys("zzqxwproduitinexistant");
        clickAndWaitNavigation(By.cssSelector("form[action*='recherche'] button[type='submit']"));
        snapshot("13-recherche-inexistante");
    }

    private void panierAvecProduit() throws IOException {
        open(BASE_URL + "recherche?controller=search&s=samsung");
        String produit = driver.findElement(By.cssSelector(".product-miniature .product-name a")).getDomProperty("href");
        open(produit);
        driver.findElement(By.cssSelector("button[data-button-action='add-to-cart']")).click();
        try {
            wait.until(d -> d.findElements(By.cssSelector(".cart-products-count")).stream()
                    .anyMatch(e -> !"0".equals(e.getDomProperty("textContent").trim())));
            log("Compteur panier mis à jour après ajout.");
        } catch (Exception e) {
            log("!! Compteur panier toujours à 0 après 20 s");
        }
        snapshot("14-apres-ajout-au-panier");

        open(BASE_URL + "panier?action=show");
        snapshot("15-panier-avec-produit");
    }

    private void connexionInvalide() throws IOException {
        open(BASE_URL + "authentification?back=my-account");
        // Identifiants FICTIFS (domaine réservé example.com) : aucun vrai compte.
        driver.findElement(By.id("field-email")).sendKeys("compte.inexistant@example.com");
        driver.findElement(By.id("field-password")).sendKeys("MotDePasseFaux123");
        clickAndWaitNavigation(By.id("submit-login"));
        snapshot("16-connexion-invalide");
    }

    private void inscription() throws IOException {
        open(BASE_URL + "authentification?back=my-account");
        clickAndWaitNavigation(By.cssSelector(".no-account a"));
        snapshot("17-inscription");
    }

    private void contactConsentement() throws IOException {
        open(BASE_URL + "contactez-nous");
        WebElement submit = driver.findElement(By.cssSelector("input[name='submitMessage']"));
        log("Bouton Envoyer activé AVANT consentement : " + submit.isEnabled());
        WebElement consent = driver.findElement(By.cssSelector("form[action$='contactez-nous'] input[name='psgdpr_consent_checkbox']"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", consent);
        log("Case consentement cochée : " + consent.isSelected());
        log("Bouton Envoyer activé APRÈS consentement : " + submit.isEnabled());
        snapshot("18-contact-consentement-coche");
        // On ne clique PAS sur Envoyer.
    }

    // ------------------------------------------------------------ helpers

    @FunctionalInterface
    private interface Etape { void run() throws Exception; }

    private void etape(String nom, Etape e) {
        log("\n######## " + nom + " ########");
        try {
            e.run();
        } catch (Exception ex) {
            log("!! ERREUR pendant '" + nom + "' : " + ex);
            try { snapshot("erreur-" + nom.replaceAll("[^A-Za-z0-9]", "_")); } catch (Exception ignored) { }
        }
    }

    private void open(String url) {
        driver.get(url);
        waitReady();
    }

    /** Clique puis attend que l'URL change (corrige le défaut de l'inspection n°1). */
    private void clickAndWaitNavigation(By by) {
        String before = driver.getCurrentUrl();
        WebElement el = wait.until(ExpectedConditions.presenceOfElementLocated(by));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        try {
            el.click();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
        try {
            wait.until(ExpectedConditions.not(ExpectedConditions.urlToBe(before)));
        } catch (Exception e) {
            log("!! L'URL n'a pas changé après le clic sur " + by);
        }
        waitReady();
    }

    private void waitReady() {
        wait.until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    private void snapshot(String name) throws IOException {
        log("\n==================== " + name + " ====================");
        try {
            log((String) ((JavascriptExecutor) driver).executeScript(DUMP_JS));
        } catch (Exception ex) {
            log("!! Erreur script d'inspection : " + ex.getMessage());
        }
        Files.writeString(OUT_DIR.resolve(name + ".html"), driver.getPageSource(), StandardCharsets.UTF_8);
        Files.write(OUT_DIR.resolve(name + ".png"), ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
    }

    private void log(String s) {
        report.append(s).append('\n');
        System.out.println(s);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws IOException {
        Files.writeString(OUT_DIR.resolve("inspection-2.txt"), report.toString(), StandardCharsets.UTF_8);
        if (driver != null) {
            driver.quit();
        }
    }
}
