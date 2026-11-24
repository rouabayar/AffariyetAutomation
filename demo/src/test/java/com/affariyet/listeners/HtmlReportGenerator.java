package com.affariyet.listeners;

import com.affariyet.base.BaseTest;
import com.affariyet.config.ConfigReader;
import com.affariyet.utils.Avertissements;
import com.affariyet.utils.Log;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.xml.XmlSuite;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Génère, À PARTIR DES RÉSULTATS RÉELS de TestNG :
 *  - reports/rapport-execution.html : tableau de bord (réussis / échoués / ignorés, durées, erreurs, captures)
 *  - reports/resume-execution.txt   : même bilan en texte
 * Les rapports standard de TestNG (index.html, emailable-report.html) sont produits dans test-output/.
 */
public class HtmlReportGenerator implements IReporter {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outputDirectory) {
        List<ITestResult> results = new ArrayList<>();
        for (ISuite suite : suites) {
            for (ISuiteResult sr : suite.getResults().values()) {
                ITestContext ctx = sr.getTestContext();
                results.addAll(ctx.getPassedTests().getAllResults());
                results.addAll(ctx.getFailedTests().getAllResults());
                results.addAll(ctx.getSkippedTests().getAllResults());
            }
        }
        results.sort(Comparator.comparing(HtmlReportGenerator::label));

        long passed = results.stream().filter(r -> r.getStatus() == ITestResult.SUCCESS).count();
        long failed = results.stream().filter(r -> r.getStatus() == ITestResult.FAILURE).count();
        long skipped = results.stream().filter(r -> r.getStatus() == ITestResult.SKIP).count();
        long totalMs = results.stream().mapToLong(r -> r.getEndMillis() - r.getStartMillis()).sum();

        Path reportsDir = Paths.get(ConfigReader.get("reports.dir")).toAbsolutePath();
        try {
            Files.createDirectories(reportsDir);
            Files.writeString(reportsDir.resolve("rapport-execution.html"),
                    html(results, passed, failed, skipped, totalMs, reportsDir), StandardCharsets.UTF_8);
            Files.writeString(reportsDir.resolve("resume-execution.txt"),
                    text(results, passed, failed, skipped, totalMs), StandardCharsets.UTF_8);
            Log.info("Rapport HTML généré : " + reportsDir.resolve("rapport-execution.html"));
        } catch (IOException e) {
            Log.error("Impossible d'écrire le rapport : " + e.getMessage());
        }
    }

    // ------------------------------------------------------------ HTML

    private String html(List<ITestResult> results, long passed, long failed, long skipped, long totalMs, Path reportsDir) {
        StringBuilder rows = new StringBuilder();
        for (ITestResult r : results) {
            String status = status(r);
            String css = r.getStatus() == ITestResult.SUCCESS ? "ok" : r.getStatus() == ITestResult.FAILURE ? "ko" : "skip";
            String error = r.getThrowable() == null ? "" : esc(r.getThrowable().toString());
            List<String> warns = Avertissements.pour(TestListener.id(r));
            String badge = warns.isEmpty() ? "" : " <span class=\"badge\">⚠ " + warns.size() + " avertissement"
                    + (warns.size() > 1 ? "s" : "") + "</span>";
            StringBuilder warnHtml = new StringBuilder();
            for (String w : warns) {
                warnHtml.append("<div class=\"warn\">⚠ Avertissement (non compté comme échec) : ").append(esc(w)).append("</div>");
            }
            String trace = r.getStatus() == ITestResult.FAILURE ? "<details><summary>Trace</summary><pre>"
                    + esc(shortTrace(r.getThrowable())) + "</pre></details>" : "";
            String shot = "";
            Object png = r.getAttribute(BaseTest.SCREENSHOT_ATTRIBUTE);
            if (png != null) {
                String rel = reportsDir.relativize(Paths.get(png.toString())).toString().replace('\\', '/');
                String relHtml = rel.replaceAll("\\.png$", ".html");
                shot = "<a href=\"" + rel + "\">capture</a> · <a href=\"" + relHtml + "\">HTML</a>";
            }
            rows.append("<tr class=\"").append(css).append("\">")
                    .append("<td>").append(esc(label(r))).append("</td>")
                    .append("<td><code>").append(r.getTestClass().getRealClass().getSimpleName()).append('.')
                    .append(r.getMethod().getMethodName()).append("</code></td>")
                    .append("<td class=\"st\">").append(status).append(badge).append("</td>")
                    .append("<td>").append(String.format("%.1f s", (r.getEndMillis() - r.getStartMillis()) / 1000.0)).append("</td>")
                    .append("<td>").append(warnHtml).append(error).append(trace).append("</td>")
                    .append("<td>").append(shot).append("</td></tr>\n");
        }
        long total = passed + failed + skipped;
        return "<!DOCTYPE html><html lang=\"fr\"><head><meta charset=\"UTF-8\">"
                + "<title>Rapport d'exécution - Tests Affariyet</title><style>"
                + "body{font-family:Segoe UI,Arial,sans-serif;margin:24px;color:#222}"
                + "h1{margin:0 0 4px}.meta{color:#666;margin-bottom:18px}"
                + ".cards{display:flex;gap:12px;margin-bottom:20px}.card{padding:12px 18px;border-radius:8px;min-width:120px}"
                + ".card b{display:block;font-size:28px}.t{background:#eef}.p{background:#e3f6e8}.f{background:#fde4e4}.s{background:#fff4d6}"
                + ".w{background:#ffe8cc;border:1px dashed #e07b00}.card small{display:block;color:#7a4a00;font-size:11px}"
                + ".badge{background:#ffe8cc;color:#7a4a00;border-radius:10px;padding:1px 7px;font-size:12px;font-weight:normal;white-space:nowrap}"
                + ".warn{background:#fff6ea;border-left:3px solid #e07b00;padding:4px 6px;margin-bottom:4px}"
                + "table{border-collapse:collapse;width:100%;font-size:14px}th,td{border:1px solid #ddd;padding:6px 8px;vertical-align:top;text-align:left}"
                + "th{background:#f3f3f3}tr.ok .st{color:#1a7f37;font-weight:bold}tr.ko .st{color:#c62828;font-weight:bold}"
                + "tr.skip .st{color:#a66b00;font-weight:bold}pre{white-space:pre-wrap;font-size:12px}"
                + "</style></head><body>"
                + "<h1>Rapport d'exécution — Tests fonctionnels Affariyet</h1>"
                + "<div class=\"meta\">Généré le " + LocalDateTime.now().format(FMT)
                + " · Site : " + esc(ConfigReader.baseUrl())
                + " · Navigateur : " + esc(ConfigReader.get("browser"))
                + " · Java " + esc(System.getProperty("java.version"))
                + " · Durée cumulée : " + String.format("%.1f s", totalMs / 1000.0) + "</div>"
                + "<div class=\"cards\">"
                + "<div class=\"card t\">Total<b>" + total + "</b></div>"
                + "<div class=\"card p\">Réussis<b>" + passed + "</b></div>"
                + "<div class=\"card f\">Échoués<b>" + failed + "</b></div>"
                + "<div class=\"card s\">Ignorés<b>" + skipped + "</b></div>"
                + "<div class=\"card w\">Avertissements<b>" + Avertissements.total() + "</b>"
                + "<small>non comptés comme échecs</small></div></div>"
                + "<table><tr><th>Cas de test</th><th>Méthode</th><th>Statut</th><th>Durée</th>"
                + "<th>Erreur / trace</th><th>Preuves</th></tr>\n" + rows + "</table>"
                + outsideTestWarnings()
                + "<p class=\"meta\">Rapports TestNG détaillés : test-output/index.html et test-output/emailable-report.html · "
                + "Traces : reports/logs/execution.log</p></body></html>";
    }

    /** Avertissements émis hors d'un test (aucun identifiant) : listés sous le tableau. */
    private static String outsideTestWarnings() {
        List<String> l = Avertissements.pour(Avertissements.HORS_TEST);
        StringBuilder sb = new StringBuilder();
        for (String w : l) {
            sb.append("<div class=\"warn\">⚠ Avertissement hors test (non compté comme échec) : ").append(esc(w)).append("</div>");
        }
        return sb.toString();
    }

    // ------------------------------------------------------------ texte

    private String text(List<ITestResult> results, long passed, long failed, long skipped, long totalMs) {
        StringBuilder sb = new StringBuilder();
        sb.append("RÉSUMÉ D'EXÉCUTION - TESTS AFFARIYET\n")
                .append("Date : ").append(LocalDateTime.now().format(FMT)).append('\n')
                .append(String.format("Total : %d | Réussis : %d | Échoués : %d | Ignorés : %d | "
                                + "Avertissements : %d (non comptés comme échecs) | Durée : %.1f s%n%n",
                        passed + failed + skipped, passed, failed, skipped, Avertissements.total(), totalMs / 1000.0));
        for (ITestResult r : results) {
            sb.append(String.format("%-9s %s", status(r), label(r)));
            if (r.getThrowable() != null) {
                sb.append("\n          -> ").append(r.getThrowable().toString().split("\n")[0]);
            }
            for (String w : Avertissements.pour(TestListener.id(r))) {
                sb.append("\n          AVERTISSEMENT (non compté comme échec) : ").append(w);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    // ------------------------------------------------------------ outils

    private static String label(ITestResult r) {
        String d = r.getMethod().getDescription();
        return (d == null || d.isBlank()) ? r.getMethod().getMethodName() : d;
    }

    private static String status(ITestResult r) {
        switch (r.getStatus()) {
            case ITestResult.SUCCESS:
                return "RÉUSSI";
            case ITestResult.FAILURE:
                return "ÉCHOUÉ";
            case ITestResult.SKIP:
                return "IGNORÉ";
            default:
                return "AUTRE";
        }
    }

    private static String shortTrace(Throwable t) {
        if (t == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        String[] lines = sw.toString().split("\\R");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(lines.length, 25); i++) {
            sb.append(lines[i]).append('\n');
        }
        return sb.toString();
    }

    private static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
