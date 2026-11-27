package com.slipkprojects.sockshttp.activities;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import com.slipkprojects.sockshttp.LauncherActivity;
import com.slipkprojects.sockshttp.SocksHttpMainActivity;
import com.slipkprojects.sockshttp.R;
import com.slipkprojects.ultrasshservice.config.ConfigParser;
import com.slipkprojects.ultrasshservice.config.Settings;
import com.slipkprojects.ultrasshservice.logger.SkStatus;

public class ConfigImportFileActivity extends BaseActivity {
    private static final int REQUEST_PICK_CONFIG = 2201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (SkStatus.isTunnelActive()) {
            Toast.makeText(this, R.string.error_tunnel_service_execution, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        openSystemFilePicker();
    }

    private void openSystemFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "اختار ملف إعدادات SKS");
        try {
            startActivityForResult(intent, REQUEST_PICK_CONFIG);
        } catch (Exception e) {
            Toast.makeText(this, "تعذر فتح مدير الملفات على هذا الجهاز", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_PICK_CONFIG) return;

        if (resultCode != RESULT_OK || data == null || data.getData() == null) {
            finish();
            return;
        }

        Uri uri = data.getData();
        String name = getDisplayName(uri);
        if (name == null || !name.toLowerCase(Locale.ROOT).endsWith("." + ConfigParser.FILE_EXTENSAO)) {
            Toast.makeText(this, "الملف غير مدعوم. اختار ملف بامتداد .sks", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        try {
            InputStream input = getContentResolver().openInputStream(uri);
            if (input == null) throw new IOException("تعذر قراءة الملف");
            importarConfigInputFile(input);
        } catch (Exception e) {
            Toast.makeText(this, "تعذر فتح ملف الإعدادات: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private String getDisplayName(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) return cursor.getString(index);
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        String path = uri.getLastPathSegment();
        return path == null ? null : path;
    }

    private void importarConfigInputFile(InputStream inputFile) {
        try {
            try {
                if (!ConfigParser.convertInputAndSave(inputFile, this)) {
                    throw new IOException(getString(R.string.error_save_settings));
                }
            } finally {
                inputFile.close();
            }

            long expiry = new Settings(this).getPrefsPrivate().getLong(Settings.CONFIG_VALIDADE_KEY, 0);
            if (expiry > 0) {
                SkStatus.logInfo(R.string.log_settings_valid,
                    android.text.format.DateFormat.getDateFormat(this).format(expiry));
            }

            Toast.makeText(this, R.string.success_import_settings, Toast.LENGTH_SHORT).show();
            SocksHttpMainActivity.updateMainViews(this);

            Intent intent = new Intent(this, LauncherActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            finish();
        }
    }
}
