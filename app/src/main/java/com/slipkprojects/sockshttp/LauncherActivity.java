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
        
        final File crashFile = new File(getFilesDir(), "last_crash.txt");
        if (crashFile.exists()) {
            try {
                FileInputStream input = new FileInputStream(crashFile);
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[2048];
                int count;
                while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
                input.close();
                final String crashText = output.toString("UTF-8");
                new AlertDialog.Builder(this)
                    .setTitle("تم تسجيل خطأ في التشغيل")
                    .setMessage(crashText)
                    .setCancelable(false)
                    .setPositiveButton("نسخ الخطأ", (dialog, which) -> {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboard.setPrimaryClip(ClipData.newPlainText("Crash log", crashText));
                        crashFile.delete();
                        startMainScreen();
                    })
                    .setNegativeButton("متابعة بدون نسخ", (dialog, which) -> {
                        crashFile.delete();
                        startMainScreen();
                    })
                    .show();
                return;
            } catch (Exception ignored) { crashFile.delete(); }
        }
		
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
