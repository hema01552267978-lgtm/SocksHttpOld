package com.slipkprojects.sockshttp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import android.os.Bundle;
import com.slipkprojects.sockshttp.R;
import com.slipkprojects.sockshttp.activities.BaseActivity;

/**
 * @author anuragdhunna
 */
public class LauncherActivity extends BaseActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash_screen);
		
		// inicia atividade principal
        startMainScreen();
    }
	
    private void startMainScreen() {
        Intent intent = new Intent(this, SocksHttpMainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }
}
