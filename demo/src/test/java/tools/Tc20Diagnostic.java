package tools;

import com.affariyet.config.ConfigReader;
import com.affariyet.utils.CsvDataReader;
import com.affariyet.utils.DriverFactory;
import com.affariyet.utils.PriceUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DIAGNOSTIC de TC20 (tri par prix croissant) — OUTIL HORS SUITE, PAS un des 20 tests.
 *
 * Il ne modifie ni TC20 ni ses assertions : il observe ce que fait réellement le site pendant 60 s
 * après le clic sur « Prix, croissant », avec les mêmes sélecteurs que ProductListPage.
 *
 *  Phase A : recherche « samsung », enregistreur des requêtes AJAX injecté dans la page, clic sur le tri,
 *            puis relevé toutes les 2 s : URL, état de chargement, 5 premiers prix, requêtes AJAX.
 *            Captures + HTML à 0 s, 20 s et 60 s, erreurs de la console du navigateur.
 *  Phase B : ouverture DIRECTE de l'URL triée, temps de chargement, ordre des prix.
 *
 * Lancement (séparé : n'exécute pas les 20 tests, ne charge ni testng.xml ni ses listeners) :
 *     mvn test -Pdiagnostic-tc20
 * (le profil s'active aussi avec  mvn test "-Dtest=Tc20Diagnostic" , voir pom.xml)
 *
 * Résultat, dans un NOUVEAU dossier à chaque lancement : diagnostics/tc20/<aaaaMMjj_HHmmss>_UTC/
 *     diagnostic.txt, A-00s / A-20s / A-60s / B-url-directe (.png + .html),
 *     surefire/ (rapports Surefire et TestNG du diagnostic), journal/logs/execution.log.
 * Rien n'est écrit dans reports/, screenshots/ ni test-output/ (résultats de la suite des 20 tests).
 * Sans le profil (propriété diagnostic.dossier absente), l'outil s'arrête AVANT d'ouvrir le navigateur.
 */
public class Tc20Diagnostic {

    // Mêmes sélecteurs que com.affariyet.pages.ProductListPage / BasePage (vérifiés sur le HTML réel)
    private static final By SEARCH_INPUT = By.cssSelector("input[name='s']");
    private static final By SEARCH_BUTTON = By.cssSelector("form[action*='recherche'] button[type='submit']");
    private static final By SORT_BUTTON = By.cssSelector(".products-sort-order .select-title");
    private static final By SORT_PRICE_ASC = By.cssSelector(".products-sort-order a[href*='order=product.price.asc']");
    private static final By PRICES = By.cssSelector("#js-product-list .product-miniature .price");

    private static final int OBSERVATION_SECONDS = 60;
    private static final int STEP_SECONDS = 2;
    /** Dossier de sortie, fourni par le profil Maven diagnostic-tc20 (propriété diagnostic.dossier). */
    private static Path OUT;
    /** Dossiers des résultats de la suite des 20 tests : le diagnostic ne doit jamais y écrire. */
    private static final List<String> PROTECTED = List.of("reports", "screenshots", "test-output");
    private static final DateTimeFormatter T = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Enregistreur des requêtes XHR / fetch et des erreurs JavaScript, injecté dans la page avant le clic. */
    private static final String RECORDER_JS = String.join("\n",
        "if (!window.__diag) {",
        "  window.__diag = [];",
        "  const o = XMLHttpRequest.prototype.open, s = XMLHttpRequest.prototype.send;",
        "  XMLHttpRequest.prototype.open = function (m, u) { this.__d = {type:'xhr', m:m, u:String(u), t0:Date.now()}; return o.apply(this, arguments); };",
        "  XMLHttpRequest.prototype.send = function () { const d = this.__d || {type:'xhr'}; window.__diag.push(d);",
        "    this.addEventListener('loadend', () => { d.t1 = Date.now(); d.status = this.status; d.len = (this.responseText || '').length; });",
        "    return s.apply(this, arguments); };",
        "  if (window.fetch) { const f = window.fetch; window.fetch = function (u, opt) {",
        "    const d = {type:'fetch', m:(opt && opt.method) || 'GET', u:String((u && u.url) || u), t0:Date.now()}; window.__diag.push(d);",
        "    return f.apply(this, arguments).then(r => { d.t1 = Date.now(); d.status = r.status; return r; },",
        "                                         e => { d.t1 = Date.now(); d.status = 'ERREUR ' + e; throw e; }); }; }",
        "  window.addEventListener('error', e => window.__diag.push({type:'js-error', message:String(e.message)}));",
        "  window.__diagStart = Date.now();",
        "}",
        "return true;");

    /** Synthèse de l'état de la page à un instant donné. */
    private static final String STATE_JS = String.join("\n",
        "const vis = e => !!(e && (e.offsetWidth || e.offsetHeight || e.getClientRects().length));",
        "const list = document.querySelector('#js-product-list');",
        "const loaders = [...document.querySelectorAll('[class*=loading],[class*=spinner],[class*=loader],.faceted-overlay')]",
        "  .filter(e => vis(e) && !e.classList.contains('reviews-loading')).map(e => e.tagName.toLowerCase() + '.' + [...e.classList].join('.'));",
        "const req = (window.__diag || []).map(d => d.type === 'js-error' ? ('JS-ERREUR ' + d.message)",
        "  : (d.type + ' ' + d.m + ' ' + (d.u || '').replace(location.origin, '').slice(0, 90) + ' -> '",
        "     + (d.t1 ? (d.status + ' en ' + (d.t1 - d.t0) + ' ms, ' + d.len + ' car.') : 'EN ATTENTE depuis ' + (Date.now() - d.t0) + ' ms')));",
        "return JSON.stringify({",
        "  url: location.href.replace(location.origin, ''),",
        "  body: document.body.className.split(' ').filter(c => c.startsWith('pace')).join(' '),",
        "  listeOpacite: list ? getComputedStyle(list).opacity : 'absente',",
        "  listeClasses: list ? list.className : '',",
        "  indicateursVisibles: loaders.slice(0, 5),",
        "  triAffiche: (document.querySelector('.products-sort-order .select-title') || {}).innerText || '',",
        "  requetes: req",
        "});");

    private WebDriver driver;
    private final StringBuilder out = new StringBuilder();

    @BeforeClass
    public void setUp() throws IOException {
        OUT = outputDirectory();
        Files.createDirectories(OUT);
        System.out.println("Diagnostic TC20 -> dossier " + OUT);
        DriverFactory.configureSeleniumLogging();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--lang=fr-FR", "--disable-notifications");
        options.setCapability("goog:loggingPrefs", Map.of("browser", "ALL")); // console du navigateur
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getInt("page.load.timeout.seconds")));
    }

    @Test
    public void diagnostiquerTriParPrix() throws IOException {
        String keyword = CsvDataReader.get("recherche.existante");
        log("DIAGNOSTIC TC20 — " + java.time.LocalDateTime.now() + " — mot-clé : " + keyword);

        // ---------------- Phase A : parcours identique à TC20, puis observation
        log("\n=== PHASE A : clic sur « Prix, croissant » puis observation " + OBSERVATION_SECONDS + " s ===");
        driver.get(ConfigReader.baseUrl());
        pageReady(60);
        WebElement input = new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.elementToBeClickable(SEARCH_INPUT));
        input.clear();
        input.sendKeys(keyword);
        String before = driver.getCurrentUrl();
        driver.findElement(SEARCH_BUTTON).click();
        new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.not(ExpectedConditions.urlToBe(before)));
        pageReady(60);
        log("Page de résultats : " + driver.getCurrentUrl());
        log("Prix avant tri : " + prices(5));

        WebElement link = driver.findElement(SORT_PRICE_ASC);
        log("Lien de tri trouvé : href=" + link.getDomProperty("href") + " | classes=" + link.getDomAttribute("class"));

        js(RECORDER_JS);
        clickRobust(SORT_BUTTON, "bouton « Trier par »");
        clickRobust(SORT_PRICE_ASC, "option « Prix, croissant »");
        long t0 = System.currentTimeMillis();
        snapshot("A-00s");

        String sortedUrl = link.getDomProperty("href");
        for (int s = STEP_SECONDS; s <= OBSERVATION_SECONDS; s += STEP_SECONDS) {
            sleepUntil(t0 + s * 1000L); // pas d'attente d'une condition : on OBSERVE à intervalles fixes
            log(String.format("t+%02ds %s | prix=%s", s, js(STATE_JS), prices(5)));
            if (s == 20 || s == OBSERVATION_SECONDS) {
                snapshot(String.format("A-%02ds", s));
            }
        }
        consoleErrors();

        // ---------------- Phase B : ouverture directe de l'URL triée
        log("\n=== PHASE B : ouverture directe de " + sortedUrl + " ===");
        long b0 = System.currentTimeMillis();
        driver.get(sortedUrl);
        boolean ready = pageReady(60);
        log(String.format("Chargement %s en %.1f s", ready ? "terminé" : "NON terminé", (System.currentTimeMillis() - b0) / 1000.0));
        List<BigDecimal> p = prices(16);
        log("URL finale : " + driver.getCurrentUrl());
        log("Prix (16 premiers) : " + p);
        log("Ordre croissant : " + (isAscending(p) ? "OUI" : "NON") + " (" + p.size() + " prix lus)");
        snapshot("B-url-directe");

        Files.writeString(OUT.resolve("diagnostic.txt"), out.toString(), StandardCharsets.UTF_8);
        System.out.println("Diagnostic terminé -> " + OUT.toAbsolutePath().resolve("diagnostic.txt"));
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() throws IOException {
        if (OUT != null) {
            Files.writeString(OUT.resolve("diagnostic.txt"), out.toString(), StandardCharsets.UTF_8);
        }
        if (driver != null) {
            driver.quit();
        }
    }

    // ------------------------------------------------------------ outils

    /**
     * Dossier du diagnostic : propriété diagnostic.dossier (profil Maven), jamais un dossier de résultats
     * de la suite, jamais un dossier déjà utilisé par un diagnostic précédent.
     */
    private static Path outputDirectory() {
        String p = System.getProperty("diagnostic.dossier");
        if (p == null || p.isBlank()) {
            throw new IllegalStateException("Propriété diagnostic.dossier absente : lancer le diagnostic avec "
                    + "« mvn test -Pdiagnostic-tc20 » (rien n'a été exécuté ni écrit)");
        }
        Path dir = Paths.get(p).toAbsolutePath().normalize();
        Path base = Paths.get("").toAbsolutePath().normalize();
        for (String name : PROTECTED) {
            if (dir.startsWith(base.resolve(name))) {
                throw new IllegalStateException("Dossier de diagnostic refusé (résultats de la suite) : " + dir);
            }
        }
        if (Files.exists(dir.resolve("diagnostic.txt"))) {
            throw new IllegalStateException("Un diagnostic existe déjà dans " + dir + " : il n'est pas écrasé");
        }
        return dir;
    }

    private void clickRobust(By by, String label) {
        WebElement e = new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.presenceOfElementLocated(by));
        js("arguments[0].scrollIntoView({block:'center'});", e);
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20)).until(ExpectedConditions.elementToBeClickable(by)).click();
            log("Clic natif effectué sur le " + label);
        } catch (RuntimeException ex) {
            js("arguments[0].click();", e);
            log("Clic natif refusé (" + ex.getClass().getSimpleName() + ") -> clic JavaScript sur le " + label);
        }
    }

    private boolean pageReady(int seconds) {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(seconds)).until(d -> Boolean.TRUE.equals(js(
                    "return document.readyState === 'complete' && !(document.body && document.body.classList.contains('pace-running'));")));
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private List<BigDecimal> prices(int max) {
        List<BigDecimal> list = new ArrayList<>();
        try {
            for (WebElement e : driver.findElements(PRICES)) {
                if (list.size() >= max) {
                    break;
                }
                list.add(PriceUtils.parse(e.getDomProperty("textContent")));
            }
        } catch (RuntimeException e) {
            log("  (lecture des prix impossible à cet instant : " + e.getClass().getSimpleName() + ")");
        }
        return list;
    }

    private static boolean isAscending(List<BigDecimal> p) {
        for (int i = 1; i < p.size(); i++) {
            if (p.get(i - 1).compareTo(p.get(i)) > 0) {
                return false;
            }
        }
        return p.size() > 1;
    }

    private void consoleErrors() {
        try {
            List<LogEntry> entries = driver.manage().logs().get(LogType.BROWSER).getAll();
            log("Console du navigateur : " + entries.size() + " message(s)");
            for (LogEntry e : entries) {
                log("  [" + e.getLevel() + "] " + e.getMessage());
            }
        } catch (RuntimeException e) {
            log("Console du navigateur non disponible : " + e.getMessage());
        }
    }

    private void snapshot(String name) throws IOException {
        Files.write(OUT.resolve(name + ".png"), ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
        Files.writeString(OUT.resolve(name + ".html"), driver.getPageSource(), StandardCharsets.UTF_8);
        log("  capture : " + name + ".png / .html");
    }

    private static void sleepUntil(long target) {
        long wait = target - System.currentTimeMillis();
        if (wait > 0) {
            try {
                Thread.sleep(wait); // justifié : relevé à intervalle fixe dans un outil de diagnostic (pas dans les tests)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private Object js(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    private void log(String s) {
        String line = LocalTime.now().format(T) + " " + s;
        out.append(line).append('\n');
        System.out.println(line);
    }
}
