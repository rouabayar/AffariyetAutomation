package tools;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Outil d'inspection (PAS un des 20 tests).
 *
 * But : ouvrir les pages clés d'Affariyet dans un vrai Chrome et enregistrer
 * la structure HTML réelle (formulaires, champs, boutons, liens, prix...)
 * pour construire les Page Objects avec des sélecteurs vérifiés.
 *
 * Lecture seule : aucun ajout au panier, aucune connexion, aucune commande.
 *
 * Lancement :  mvn test -Dtest=SiteInspector
 * Résultat  :  reports/site-inspection/  (inspection.txt + .html + .png)
 */
public class SiteInspector {

    private static final String BASE_URL = "https://www.affariyet.com/";
    private static final String SEARCH_KEYWORD = "samsung";
    private static final Path OUT_DIR = Paths.get("reports", "site-inspection");

    private WebDriver driver;
    private WebDriverWait wait;
    private final StringBuilder report = new StringBuilder();

    /** Script JS qui résume la structure de la page courante. */
    private static final String DUMP_JS = String.join("\n",
        "const out = [];",
        "const attrs = e => ['id','name','type','class','placeholder','href','action','method','value','aria-label','title','data-product-id']",
        "  .filter(a => e.getAttribute(a)).map(a => a + '=\"' + String(e.getAttribute(a)).trim().slice(0,120) + '\"').join(' ');",
        "const txt = e => (e.innerText || e.textContent || '').replace(/\\s+/g,' ').trim().slice(0,80);",
        "out.push('URL: ' + location.href);",
        "out.push('TITLE: ' + document.title);",
        "const gen = document.querySelector('meta[name=generator]');",
        "out.push('GENERATOR: ' + (gen ? gen.content : '(aucun)'));",
        "out.push('BODY: ' + attrs(document.body));",
        "out.push('--- FORMS ---');",
        "document.querySelectorAll('form').forEach((f,i) => out.push('form#' + i + ' ' + attrs(f)));",
        "out.push('--- INPUTS / SELECT / TEXTAREA ---');",
        "document.querySelectorAll('input,select,textarea').forEach(e => { if (e.type !== 'hidden') out.push(e.tagName + ' ' + attrs(e)); });",
        "out.push('--- BUTTONS ---');",
        "document.querySelectorAll('button,input[type=submit],a.btn,a.button').forEach(e => out.push(e.tagName + ' [' + txt(e) + '] ' + attrs(e)));",
        "out.push('--- LIENS UTILES (compte, panier, contact, livraison, inscription, pages...) ---');",
        "const kw = /connexion|login|compte|account|panier|cart|contact|livraison|delivery|inscri|register|creer|créer|cgv|condition|faq|page=|p=|next|suivant|wishlist|favori/i;",
        "document.querySelectorAll('a[href]').forEach(a => { const s = a.href + ' ' + txt(a) + ' ' + (a.className||''); if (kw.test(s)) out.push('A [' + txt(a) + '] ' + attrs(a)); });",
        "out.push('--- MENU / NAVIGATION (nav, header, menu) ---');",
        "document.querySelectorAll('nav a[href], header a[href], [class*=menu] a[href]').forEach((a,i) => { if (i < 80) out.push('A [' + txt(a) + '] ' + attrs(a)); });",
        "out.push('--- ELEMENTS PRIX (class contient price/prix) ---');",
        "document.querySelectorAll('[class*=price],[class*=prix],[itemprop=price]').forEach((e,i) => { if (i < 15) out.push(e.tagName + ' [' + txt(e) + '] ' + attrs(e)); });",
        "out.push('--- ELEMENTS PRODUIT (class contient product) ---');",
        "document.querySelectorAll('[class*=product] a[href]').forEach((a,i) => { if (i < 25) out.push('A [' + txt(a) + '] ' + attrs(a) + ' | parent: ' + attrs(a.parentElement)); });",
        "out.push('--- MESSAGES / ALERTES ---');",
        "document.querySelectorAll('[class*=alert],[class*=error],[class*=message],[class*=notice],[role=alert]').forEach((e,i) => { if (i < 15) out.push(e.tagName + ' [' + txt(e) + '] ' + attrs(e)); });",
        "return out.join('\\n');"
    );

    @BeforeClass
    public void setUp() throws IOException {
        Files.createDirectories(OUT_DIR);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--lang=fr-FR");
        // Selenium Manager (inclus dans Selenium 4.27) télécharge le bon chromedriver automatiquement.
        driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(60));
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @Test
    public void inspecterAffariyet() throws IOException {
        // 1. Page d'accueil
        open(BASE_URL);
        snapshot("01-accueil");

        // On mémorise les liens découverts sur l'accueil pour visiter les autres pages.
        Map<String, String> liens = findLinks(new String[][]{
            {"connexion", "connexion|login|mon-compte|my-account|customer/account|authentication|se connecter"},
            {"inscription", "inscription|register|create|creer|créer"},
            {"panier", "panier|cart|order"},
            {"contact", "contact"},
            {"livraison", "livraison|delivery|shipping"},
        });

        // 2. Recherche d'un produit
        WebElement search = findSearchInput();
        if (search != null) {
            search.clear();
            search.sendKeys(SEARCH_KEYWORD, Keys.ENTER);
            waitPageLoaded();
            snapshot("02-recherche-" + SEARCH_KEYWORD);

            // 3. Premier produit des résultats (lecture seule)
            String produit = firstProductUrl();
            if (produit != null) {
                open(produit);
                snapshot("03-fiche-produit");
            } else {
                log("!! Aucun lien produit détecté automatiquement dans les résultats.");
            }

            // 4. Recherche d'un mot inexistant
            open(BASE_URL);
            WebElement s2 = findSearchInput();
            if (s2 != null) {
                s2.sendKeys("zzqxwproduitinexistant", Keys.ENTER);
                waitPageLoaded();
                snapshot("04-recherche-inexistante");
            }
        } else {
            log("!! Champ de recherche non détecté automatiquement sur l'accueil.");
        }

        // 5. Pages découvertes (connexion, inscription, panier, contact, livraison)
        int n = 5;
        for (Map.Entry<String, String> e : liens.entrySet()) {
            if (e.getValue() != null) {
                open(e.getValue());
                snapshot(String.format("%02d-%s", n++, e.getKey()));
            } else {
                log("!! Lien '" + e.getKey() + "' non trouvé sur l'accueil.");
            }
        }

        Files.writeString(OUT_DIR.resolve("inspection.txt"), report.toString(), StandardCharsets.UTF_8);
        System.out.println("Inspection terminée -> " + OUT_DIR.toAbsolutePath());
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws IOException {
        if (report.length() > 0) {
            Files.writeString(OUT_DIR.resolve("inspection.txt"), report.toString(), StandardCharsets.UTF_8);
        }
        if (driver != null) {
            driver.quit();
        }
    }

    // ---------------------------------------------------------------- helpers

    private void open(String url) {
        try {
            driver.get(url);
            waitPageLoaded();
        } catch (Exception ex) {
            log("!! Erreur en ouvrant " + url + " : " + ex.getMessage());
        }
    }

    private void waitPageLoaded() {
        wait.until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
    }

    /** Enregistre : résumé texte + code HTML complet + capture d'écran. */
    private void snapshot(String name) throws IOException {
        log("\n==================== " + name + " ====================");
        try {
            log((String) ((JavascriptExecutor) driver).executeScript(DUMP_JS));
        } catch (Exception ex) {
            log("!! Erreur script d'inspection : " + ex.getMessage());
        }
        Files.writeString(OUT_DIR.resolve(name + ".html"), driver.getPageSource(), StandardCharsets.UTF_8);
        byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
        Files.write(OUT_DIR.resolve(name + ".png"), png);
    }

    private WebElement findSearchInput() {
        String[] css = {
            "input[type='search']", "input[name='q']", "input[name='s']",
            "input[name='search']", "input[name='search_query']",
            "input[placeholder*='echerch']", "input[placeholder*='earch']"
        };
        for (String c : css) {
            for (WebElement e : driver.findElements(By.cssSelector(c))) {
                if (e.isDisplayed()) {
                    log("Champ de recherche détecté avec : " + c);
                    return e;
                }
            }
        }
        return null;
    }

    private String firstProductUrl() {
        String[] css = {
            "[class*='product'] h2 a", "[class*='product'] h3 a", "[class*='product-title'] a",
            "[class*='product-name'] a", "a[class*='product']", "[class*='product'] a[href]"
        };
        for (String c : css) {
            for (WebElement a : driver.findElements(By.cssSelector(c))) {
                String href = a.getDomProperty("href");
                if (href != null && href.startsWith("http") && a.isDisplayed()) {
                    log("Lien produit détecté avec : " + c + " -> " + href);
                    return href;
                }
            }
        }
        return null;
    }

    /** Cherche sur la page courante un lien par mots-clés (href ou texte). */
    private Map<String, String> findLinks(String[][] wanted) {
        Map<String, String> result = new LinkedHashMap<>();
        List<WebElement> links = new ArrayList<>(driver.findElements(By.cssSelector("a[href]")));
        for (String[] w : wanted) {
            String found = null;
            for (WebElement a : links) {
                try {
                    String href = a.getDomProperty("href");
                    String text = a.getDomProperty("textContent");
                    String s = ((href == null ? "" : href) + " " + (text == null ? "" : text)).toLowerCase();
                    if (href != null && href.startsWith("http") && s.matches("(?s).*(" + w[1] + ").*")) {
                        found = href;
                        break;
                    }
                } catch (Exception ignored) {
                    // élément devenu obsolète : on passe au suivant
                }
            }
            log("Lien '" + w[0] + "' : " + found);
            result.put(w[0], found);
        }
        return result;
    }

    private void log(String s) {
        report.append(s).append('\n');
        System.out.println(s);
    }
}
