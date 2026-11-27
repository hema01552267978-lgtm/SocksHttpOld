package com.slipkprojects.sockshttp;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;
import com.slipkprojects.ultrasshservice.SocksHttpCore;
import com.slipkprojects.ultrasshservice.util.SkProtect;
import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;

/** Minimal application startup. Legacy services are initialized from the main
 * activity, after the native launcher has had a chance to start. */
public class SocksHttpApp extends Application {
    private static final String TAG = "SocksHttpApp";
    public static final String PREFS_GERAL = "SocksHttpGERAL";
    public static final String ADS_UNITID_INTERSTITIAL_MAIN = "ca-app-pub-6560862030501171/1687567442";
    public static final String ADS_UNITID_BANNER_MAIN = "ca-app-pub-6560862030501171/3101206569";
    public static final String ADS_UNITID_BANNER_SOBRE = "ca-app-pub-6560862030501171/6860519994";
    public static final String ADS_UNITID_BANNER_TEST = "ca-app-pub-3940256099942544/6300978111";
    public static final String APP_FLURRY_KEY = "RQQ8J9Q2N4RH827G32X9";

    private static SocksHttpApp mApp;
    private static boolean componentsInitialized;

    @Override public void onCreate() {
        super.onCreate();
        final Thread.UncaughtExceptionHandler previousHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
            try (PrintWriter writer = new PrintWriter(new FileWriter(new File(getFilesDir(), "last_crash.txt"), false))) {
                writer.println("ELMALOO3N VPN uncaught startup crash");
                writer.println("Android SDK: " + Build.VERSION.SDK_INT);
                writer.println("Device: " + Build.MANUFACTURER + " " + Build.MODEL);
                error.printStackTrace(writer);
            } catch (Throwable ignored) { }
            if (previousHandler != null) previousHandler.uncaughtException(thread, error);
        });
        mApp = this;
        preservePreviousStartupStage();
        markStartupStage(this, "APPLICATION: minimal startup complete");
    }

    /** Called only after the native launcher has successfully opened the main activity. */
    public static synchronized void initializeComponents(Context context) {
        if (componentsInitialized) return;
        runStartupStep(context, "SocksHttpCore.init", () -> SocksHttpCore.init(context.getApplicationContext()));
        runStartupStep(context, "SkProtect.init", () -> SkProtect.init(context.getApplicationContext()));
        componentsInitialized = true;
    }

    @Override protected void attachBaseContext(Context base) { super.attachBaseContext(base); }
    @Override public void onConfigurationChanged(Configuration newConfig) { super.onConfigurationChanged(newConfig); }

    private void preservePreviousStartupStage() {
        File current = new File(getFilesDir(), "startup_stage.txt");
        File previous = new File(getFilesDir(), "previous_startup_stage.txt");
        if (!current.exists()) { previous.delete(); return; }
        try (FileInputStream input = new FileInputStream(current)) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[1024];
            int count;
            while ((count = input.read(chunk)) != -1) buffer.write(chunk, 0, count);
            String lastStage = buffer.toString("UTF-8");

            // A fully initialized main screen means the prior process had already
            // completed startup; do not misreport a normal later process death as a crash.
            if (lastStage.contains("MAIN_READY:")) {
                previous.delete();
                return;
            }

            try (FileOutputStream output = new FileOutputStream(previous, false)) {
                output.write(lastStage.getBytes("UTF-8"));
            }
        } catch (Throwable error) {
            Log.e(TAG, "Could not preserve previous startup stage", error);
        }
    }

    private static void runStartupStep(Context context, String step, Runnable action) {
        markStartupStage(context, "APPLICATION: before " + step);
        try {
            action.run();
            markStartupStage(context, "APPLICATION: " + step + " completed");
        } catch (Throwable error) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(new File(context.getFilesDir(), "last_crash.txt"), false))) {
                writer.println("ELMALOO3N VPN startup error: " + step);
                writer.println("Android SDK: " + Build.VERSION.SDK_INT);
                writer.println("Device: " + Build.MANUFACTURER + " " + Build.MODEL);
                error.printStackTrace(writer);
            } catch (Throwable ignored) { }
            markStartupStage(context, "APPLICATION_ERROR: " + step);
            Log.e(TAG, "Startup step failed: " + step, error);
            throw new IllegalStateException("Startup initialization failed at " + step, error);
        }
    }

    public static void markStartupStage(Context context, String stage) {
        try (FileWriter writer = new FileWriter(new File(context.getFilesDir(), "startup_stage.txt"), false)) {
            writer.write(stage + "\nAndroid SDK: " + Build.VERSION.SDK_INT
                + "\nDevice: " + Build.MANUFACTURER + " " + Build.MODEL);
        } catch (Throwable ignored) { }
    }

    public static SocksHttpApp getApp() { return mApp; }
}