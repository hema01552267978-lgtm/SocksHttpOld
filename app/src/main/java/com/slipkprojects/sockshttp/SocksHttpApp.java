package com.slipkprojects.sockshttp;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;
import com.slipkprojects.sockshttp.preference.SettingsPreference;
import com.slipkprojects.ultrasshservice.util.SkProtect;
import com.slipkprojects.ultrasshservice.logger.SkStatus;
import android.content.Context;
import android.content.pm.PackageInfo;
import com.slipkprojects.sockshttp.util.Utils;
import android.os.Build;
import android.content.pm.PackageManager;
import com.slipkprojects.ultrasshservice.SocksHttpCore;
import com.slipkprojects.ultrasshservice.config.Settings;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import com.google.android.gms.ads.MobileAds;
import android.content.res.Configuration;
import com.slipkprojects.sockshttp.preference.LocaleHelper;

/**
* App
*/
public class SocksHttpApp extends Application
{
	private static final String TAG = SocksHttpApp.class.getSimpleName();
	public static final String PREFS_GERAL = "SocksHttpGERAL";
	
	public static final String ADS_UNITID_INTERSTITIAL_MAIN = "ca-app-pub-6560862030501171/1687567442";
	public static final String ADS_UNITID_BANNER_MAIN = "ca-app-pub-6560862030501171/3101206569";
	public static final String ADS_UNITID_BANNER_SOBRE = "ca-app-pub-6560862030501171/6860519994";
	public static final String ADS_UNITID_BANNER_TEST = "ca-app-pub-3940256099942544/6300978111";
	public static final String APP_FLURRY_KEY = "RQQ8J9Q2N4RH827G32X9";
	
	private static SocksHttpApp mApp;
	
	@Override
	public void onCreate()
	{
		super.onCreate();

		// Install crash reporting before any file or app initialization work.
		// This captures failures that occur while preserving the previous startup stage.
		// Save uncaught startup crashes so they can be shown on the next launch.
		final Thread.UncaughtExceptionHandler previousHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override public void uncaughtException(Thread thread, Throwable error) {
				try {
					File crashFile = new File(getFilesDir(), "last_crash.txt");
					PrintWriter writer = new PrintWriter(new FileWriter(crashFile, false));
					writer.println("ELMALOO3N VPN startup crash");
					writer.println("Android SDK: " + Build.VERSION.SDK_INT);
					writer.println("Device: " + Build.MANUFACTURER + " " + Build.MODEL);
					error.printStackTrace(writer);
					writer.close();
				} catch (Throwable ignored) { }
				if (previousHandler != null) previousHandler.uncaughtException(thread, error);
			}
		});

		preservePreviousStartupStage();
		markStartupStage(this, "APPLICATION: handler installed");
		mApp = this;
		
		// captura dados para análise
		/*new FlurryAgent.Builder()
			.withCaptureUncaughtExceptions(true)
            .withIncludeBackgroundSessionsInMetrics(true)
            .withLogLevel(Log.VERBOSE)
            .withPerformanceMetrics(FlurryPerformance.ALL)
			.build(this, APP_FLURRY_KEY);*/
			
		// Initialize components independently so a startup failure can be shown by the launcher.
		runStartupStep("SocksHttpCore.init", () -> SocksHttpCore.init(this));
		runStartupStep("SkProtect.init", () -> SkProtect.init(this));
		runStartupStep("MobileAds.initialize", () -> MobileAds.initialize(this));
		runStartupStep("setModoNoturno", () -> setModoNoturno(this));
		markStartupStage(this, "APPLICATION: completed");
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		//LocaleHelper.setLocale(this);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		//LocaleHelper.setLocale(this);
	}
	
	private void setModoNoturno(Context context) {
		boolean is = new Settings(context)
			.getModoNoturno().equals("on");

		int night_mode = is ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
		AppCompatDelegate.setDefaultNightMode(night_mode);
	}
	
	private void preservePreviousStartupStage() {
		File current = new File(getFilesDir(), "startup_stage.txt");
		File previous = new File(getFilesDir(), "previous_startup_stage.txt");
		if (!current.exists()) {
			previous.delete();
			return;
		}
		try {
			FileInputStream input = new FileInputStream(current);
			FileOutputStream output = new FileOutputStream(previous, false);
			byte[] buffer = new byte[1024];
			int count;
			while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
			input.close();
			output.close();
		} catch (Throwable error) {
			Log.e(TAG, "Could not preserve previous startup stage", error);
		}
	}

	private void runStartupStep(String step, Runnable action) {
		markStartupStage(this, "APPLICATION: before " + step);
		try {
			action.run();
			markStartupStage(this, "APPLICATION: " + step + " completed");
		} catch (Throwable error) {
			try {
				File crashFile = new File(getFilesDir(), "last_crash.txt");
				PrintWriter writer = new PrintWriter(new FileWriter(crashFile, false));
				writer.println("ELMALOO3N VPN startup error: " + step);
				writer.println("Android SDK: " + Build.VERSION.SDK_INT);
				writer.println("Device: " + Build.MANUFACTURER + " " + Build.MODEL);
				error.printStackTrace(writer);
				writer.close();
			} catch (Throwable ignored) { }
			markStartupStage(this, "APPLICATION_ERROR: " + step);
			Log.e(TAG, "Startup step failed: " + step, error);
		}
	}

	public static void markStartupStage(Context context, String stage) {
		try {
			File stageFile = new File(context.getFilesDir(), "startup_stage.txt");
			FileWriter writer = new FileWriter(stageFile, false);
			writer.write(stage + "\nAndroid SDK: " + Build.VERSION.SDK_INT + "\nDevice: " + Build.MANUFACTURER + " " + Build.MODEL);
			writer.close();
		} catch (Throwable ignored) { }
	}

	public static SocksHttpApp getApp() {
		return mApp;
	}
}
