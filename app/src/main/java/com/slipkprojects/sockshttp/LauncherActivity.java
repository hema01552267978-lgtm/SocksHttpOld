package com.slipkprojects.sockshttp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Minimal platform launcher kept independent from AppCompat so startup
 * diagnostics can still be displayed if the legacy support library fails.
 */
public class LauncherActivity extends Activity {

    private String readText(File file) throws Exception {
        FileInputStream input = new FileInputStream(file);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int count;
        try {
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            return output.toString("UTF-8");
        } finally {
            input.close();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView splash = new TextView(this);
        splash.setText("");
        splash.setTextColor(Color.WHITE);
        splash.setTextSize(25);
        splash.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        splash.setGravity(Gravity.CENTER);
        splash.setBackgroundResource(R.drawable.splash_screen_background);
        splash.setContentDescription("ELMALOO3N VPN");
        setContentView(splash);

        File stageFile = new File(getFilesDir(), "previous_startup_stage.txt");
        String previousStage = null;
        try {
            if (stageFile.exists()) previousStage = readText(stageFile);
        } catch (Exception ignored) { }
        stageFile.delete();

        SocksHttpApp.markStartupStage(this, "LAUNCHER: native Activity created");

        final File crashFile = new File(getFilesDir(), "last_crash.txt");
        String crashText = null;
        if (crashFile.exists()) {
            try {
                crashText = readText(crashFile);
            } catch (Exception ignored) {
                crashFile.delete();
            }
        }

        if ((previousStage != null && !previousStage.trim().isEmpty())
                || (crashText != null && !crashText.trim().isEmpty())) {
            StringBuilder diagnostic = new StringBuilder();
            if (previousStage != null && !previousStage.trim().isEmpty()) {
                diagnostic.append("آخر مرحلة محفوظة من التشغيل السابق:\n")
                    .append(previousStage).append("\n\n");
            }
            if (crashText != null && !crashText.trim().isEmpty()) {
                diagnostic.append("تفاصيل الخطأ المسجل:\n").append(crashText);
            }
            String details = diagnostic.toString();
            showDiagnostic("تشخيص توقف التطبيق",
                details, "نسخ التشخيص وفتح التطبيق", details);
            return;
        }

        startMainScreen();
    }

    private void showDiagnostic(String title, String message, String button, final String copyText) {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(button, (dialog, which) -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(ClipData.newPlainText("ELMALOO3N VPN diagnostic", copyText));
                }
                new File(getFilesDir(), "last_crash.txt").delete();
                new File(getFilesDir(), "previous_startup_stage.txt").delete();
                startMainScreen();
            })
            .setNegativeButton("فتح التطبيق بدون نسخ", (dialog, which) -> {
                new File(getFilesDir(), "last_crash.txt").delete();
                new File(getFilesDir(), "previous_startup_stage.txt").delete();
                startMainScreen();
            })
            .show();
    }

    private void startMainScreen() {
        startActivity(new Intent(this, SocksHttpMainActivity.class));
        finish();
    }
}
