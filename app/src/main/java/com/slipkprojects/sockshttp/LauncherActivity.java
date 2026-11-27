package com.slipkprojects.sockshttp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import com.slipkprojects.sockshttp.preference.LocaleHelper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.setLocale(base));
    }

    private String readText(File file) throws Exception {
        FileInputStream input = new FileInputStream(file);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int count;
        while ((count = input.read(buffer)) != -1) {
            output.write(buffer, 0, count);
        }
        input.close();
        return output.toString("UTF-8");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        File stageFile = new File(getFilesDir(), "previous_startup_stage.txt");
        String previousStage = null;
        try {
            if (stageFile.exists()) {
                previousStage = readText(stageFile);
            }
        } catch (Exception ignored) {
        }
        stageFile.delete();

        // Record launcher entry before AppCompat setup.
        SocksHttpApp.markStartupStage(this, "LAUNCHER: onCreate entered");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (previousStage != null && !previousStage.contains("MAIN_READY")) {
            final String diagnostic = previousStage;
            new AlertDialog.Builder(this)
                .setTitle("تشخيص توقف التطبيق")
                .setMessage("آخر مرحلة وصل لها التطبيق قبل الإغلاق:\n\n" + diagnostic)
                .setCancelable(false)
                .setPositiveButton("نسخ التشخيص وفتح التطبيق", (dialog, which) -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(ClipData.newPlainText("Startup diagnostic", diagnostic));
                    }
                    stageFile.delete();
                    startMainScreen();
                })
                .setNegativeButton("فتح التطبيق", (dialog, which) -> {
                    stageFile.delete();
                    startMainScreen();
                })
                .show();
            return;
        }

        final File crashFile = new File(getFilesDir(), "last_crash.txt");
        if (crashFile.exists()) {
            try {
                final String crashText = readText(crashFile);
                new AlertDialog.Builder(this)
                    .setTitle("تم تسجيل خطأ في التشغيل")
                    .setMessage(crashText)
                    .setCancelable(false)
                    .setPositiveButton("نسخ الخطأ", (dialog, which) -> {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(ClipData.newPlainText("Crash log", crashText));
                        }
                        crashFile.delete();
                        startMainScreen();
                    })
                    .setNegativeButton("متابعة بدون نسخ", (dialog, which) -> {
                        crashFile.delete();
                        startMainScreen();
                    })
                    .show();
                return;
            } catch (Exception ignored) {
                crashFile.delete();
            }
        }

        startMainScreen();
    }

    private void startMainScreen() {
        Intent intent = new Intent(this, SocksHttpMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }
}
