package com.affariyet.listeners;

import com.affariyet.utils.Log;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Traces lisibles pendant l'exécution (console + reports/logs/execution.log).
 */
public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        Log.info(">> DÉMARRAGE : " + label(result));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        Log.info("[RÉUSSI] " + label(result) + duration(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        Log.error("[ÉCHOUÉ] " + label(result) + duration(result));
        if (result.getThrowable() != null) {
            Log.error("   Cause : " + result.getThrowable());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String reason = result.getThrowable() == null ? "" : " — " + result.getThrowable().getMessage();
        Log.info("[IGNORÉ] " + label(result) + reason);
    }

    @Override
    public void onFinish(ITestContext context) {
        Log.info(String.format("BILAN « %s » : %d réussi(s), %d échoué(s), %d ignoré(s)",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size()));
    }

    private static String label(ITestResult r) {
        String desc = r.getMethod().getDescription();
        return (desc == null || desc.isBlank()) ? r.getMethod().getMethodName() : desc;
    }

    private static String duration(ITestResult r) {
        return String.format(" (%.1f s)", (r.getEndMillis() - r.getStartMillis()) / 1000.0);
    }
}
