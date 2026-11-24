package com.affariyet.listeners;

import com.affariyet.config.ConfigReader;
import com.affariyet.utils.Avertissements;
import com.affariyet.utils.Log;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Traces lisibles pendant l'exécution :
 *  - une ligne de statut par test (la ligne d'en-tête « ==== TCxx ==== » est écrite par BaseTest) ;
 *  - pour un échec, la cause utile en clair dans la console, la trace complète dans execution.log ;
 *  - un BILAN GLOBAL UNIQUE à la fin de la suite (la suite compte 3 blocs <test>, voir testng.xml).
 */
public class TestListener implements ITestListener, ISuiteListener {

    private long suiteStart;

    // ------------------------------------------------------------ tests

    @Override
    public void onTestSuccess(ITestResult result) {
        Log.info("RÉUSSI" + duration(result) + warnings(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Log.error("ÉCHOUÉ" + duration(result) + warnings(result) + " — " + cause(result.getThrowable()));
        Log.detail("Trace complète :" + System.lineSeparator() + fullTrace(result.getThrowable()));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String reason = result.getThrowable() == null ? "" : " — " + firstLine(result.getThrowable().getMessage());
        Log.info("IGNORÉ" + warnings(result) + reason);
    }

    // ------------------------------------------------------------ suite

    @Override
    public void onStart(ISuite suite) {
        suiteStart = System.currentTimeMillis();
    }

    @Override
    public void onFinish(ISuite suite) {
        List<ITestResult> passed = new ArrayList<>();
        List<ITestResult> failed = new ArrayList<>();
        List<ITestResult> skipped = new ArrayList<>();
        long cumulated = 0;
        for (ISuiteResult sr : suite.getResults().values()) {
            ITestContext ctx = sr.getTestContext();
            passed.addAll(ctx.getPassedTests().getAllResults());
            failed.addAll(ctx.getFailedTests().getAllResults());
            skipped.addAll(ctx.getSkippedTests().getAllResults());
        }
        for (List<ITestResult> l : List.of(passed, failed, skipped)) {
            l.sort(Comparator.comparing(TestListener::label));
            for (ITestResult r : l) {
                cumulated += r.getEndMillis() - r.getStartMillis();
            }
        }
        int total = passed.size() + failed.size() + skipped.size();
        String line = "=".repeat(100);

        Log.banner(line);
        Log.banner(String.format("BILAN GLOBAL : %d tests — %d réussi(s), %d échoué(s), %d ignoré(s) — "
                        + "%d avertissement(s) (non comptés comme échecs) — durée cumulée %.1f s, durée totale %.1f s",
                total, passed.size(), failed.size(), skipped.size(), Avertissements.total(),
                cumulated / 1000.0, (System.currentTimeMillis() - suiteStart) / 1000.0));
        for (ITestResult r : failed) {
            Log.banner("  ÉCHOUÉ : " + label(r) + " — " + firstLine(cause(r.getThrowable())));
        }
        for (ITestResult r : skipped) {
            String reason = r.getThrowable() == null ? "" : " — " + firstLine(r.getThrowable().getMessage());
            Log.banner("  IGNORÉ : " + label(r) + reason);
        }
        for (Map.Entry<String, List<String>> e : Avertissements.tous().entrySet()) {
            for (String w : e.getValue()) {
                Log.banner("  AVERTISSEMENT : " + labelOf(e.getKey(), passed, failed, skipped) + " — " + w);
            }
        }
        String reports = Paths.get(ConfigReader.get("reports.dir")).toString();
        Log.banner("  Rapports : " + Paths.get(reports, "rapport-execution.html") + " · test-output"
                + java.io.File.separator + "index.html · " + Paths.get(reports, "logs", "execution.log"));
        Log.banner(line);
    }

    // ------------------------------------------------------------ outils

    /**
     * Cause lisible : pour une AssertionError (dont SoftAssert), toutes les lignes d'assertion ;
     * pour une exception Selenium, la première ligne du message seulement (le reste — Build info,
     * Capabilities… — est conservé dans execution.log et dans les rapports).
     */
    static String cause(Throwable t) {
        if (t == null) {
            return "";
        }
        String msg = t.getMessage() == null ? "" : t.getMessage();
        if (t instanceof AssertionError) {
            String cleaned = msg.replace("The following asserts failed:", "")
                    .replaceAll("\\s*expected \\[[^\\]]*\\] but found \\[[^\\]]*\\]", "")
                    .replaceAll("[\\r\\n]+\\s*", " ")
                    .replaceAll(",\\s*\\[", " ; [")
                    .trim();
            return cleaned.isEmpty() ? t.getClass().getSimpleName() : cleaned;
        }
        return t.getClass().getSimpleName() + " : " + firstLine(msg);
    }

    private static String firstLine(String s) {
        if (s == null) {
            return "";
        }
        String trimmed = s.trim();
        int nl = trimmed.indexOf('\n');
        return (nl < 0 ? trimmed : trimmed.substring(0, nl)).trim();
    }

    private static String fullTrace(Throwable t) {
        if (t == null) {
            return "";
        }
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /** Identifiant « TCxx » du test (même règle que BaseTest et le rapport HTML). */
    static String id(ITestResult r) {
        return Avertissements.identifiant(r.getMethod().getDescription(), r.getMethod().getMethodName());
    }

    /** « — avec N avertissement(s) » si le test en a, sinon chaîne vide. Ne change pas le statut. */
    private static String warnings(ITestResult r) {
        int n = Avertissements.pour(id(r)).size();
        return n == 0 ? "" : " — avec " + n + " avertissement" + (n > 1 ? "s" : "");
    }

    /** Libellé complet d'un test à partir de son identifiant (l'identifiant seul s'il est introuvable). */
    @SafeVarargs
    private static String labelOf(String testId, List<ITestResult>... groups) {
        for (List<ITestResult> g : groups) {
            for (ITestResult r : g) {
                if (id(r).equals(testId)) {
                    return label(r);
                }
            }
        }
        return testId;
    }

    private static String label(ITestResult r) {
        String d = r.getMethod().getDescription();
        return (d == null || d.isBlank()) ? r.getMethod().getMethodName() : d;
    }

    private static String duration(ITestResult r) {
        return String.format(" (%.1f s)", (r.getEndMillis() - r.getStartMillis()) / 1000.0);
    }
}
