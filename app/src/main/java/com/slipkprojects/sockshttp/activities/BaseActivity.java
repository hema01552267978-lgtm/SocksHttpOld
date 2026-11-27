package com.slipkprojects.sockshttp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.slipkprojects.sockshttp.preference.SettingsPreference;
import com.slipkprojects.ultrasshservice.config.Settings;
import android.support.v7.app.AppCompatDelegate;
import android.content.Context;
import com.slipkprojects.sockshttp.preference.LocaleHelper;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import static android.content.pm.PackageManager.GET_META_DATA;

/**
 * Created by Pankaj on 03-11-2017.
 */
public abstract class BaseActivity extends AppCompatActivity
{
	public static int mTheme = 0;
    
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		com.slipkprojects.sockshttp.SocksHttpApp.markStartupStage(this, "BASE_ACTIVITY: before AppCompat onCreate");
		super.onCreate(savedInstanceState);
		com.slipkprojects.sockshttp.SocksHttpApp.markStartupStage(this, "BASE_ACTIVITY: AppCompat onCreate completed");
		//setTheme(ThemeUtil.getThemeId(mTheme));
		
		com.slipkprojects.sockshttp.SocksHttpApp.markStartupStage(this, "BASE_ACTIVITY: before local night mode");
		setModoNoturnoLocal();
		com.slipkprojects.sockshttp.SocksHttpApp.markStartupStage(this, "BASE_ACTIVITY: local night mode completed");
			
		resetTitles();
		com.slipkprojects.sockshttp.SocksHttpApp.markStartupStage(this, "BASE_ACTIVITY: titles reset completed");
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.setLocale(base));
	}
	
	public void setModoNoturnoLocal() {
		boolean is = new Settings(this)
			.getModoNoturno().equals("on");

		int night_mode = is ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
		//AppCompatDelegate.setDefaultNightMode(night_mode);
		getDelegate().setLocalNightMode(night_mode);
	}
	
	protected void resetTitles() {
		try {
			ActivityInfo info = getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA);
			if (info.labelRes != 0) {
				setTitle(info.labelRes);
			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
	}
}
