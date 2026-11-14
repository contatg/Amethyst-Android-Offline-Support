package net.kdt.pojavlaunch;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.tasks.AsyncAssetManager;

import java.io.IOException;

public class TestStorageActivity extends Activity {
    private static final int REQUEST_STORAGE = 1;
    private boolean preparing;
    private AlertDialog storageDialog;

    @Override
    protected void onResume() {
        super.onResume();
        continueStartup();
    }

    private void continueStartup() {
        if (preparing || isFinishing() || (storageDialog != null && storageDialog.isShowing())) return;
        if (!isStorageAllowed(this)) {
            storageDialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.shared_storage_permission_message)
                    .setPositiveButton(R.string.shared_storage_permission_action, (dialog, which) -> requestStorageAccess())
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> finish())
                    .setOnCancelListener(dialog -> finish())
                    .show();
            return;
        }

        if (!Tools.checkStorageRoot(this)) {
            startActivity(new Intent(this, MissingStorageActivity.class));
            finish();
            return;
        }

        preparing = true;
        TextView message = new TextView(this);
        message.setGravity(Gravity.CENTER);
        message.setText(R.string.shared_storage_preparing);
        setContentView(message);

        Context context = getApplicationContext();
        PojavApplication.sExecutorService.execute(() -> {
            try {
                SharedGameStorage.prepare(context);
                runOnUiThread(this::openLauncher);
            } catch (IOException | SecurityException error) {
                runOnUiThread(() -> showStorageError(error));
            }
        });
    }

    public static boolean isStorageAllowed(Context context) {
        if (Build.VERSION.SDK_INT >= 30) return Environment.isExternalStorageManager();
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStorageAccess() {
        if (Build.VERSION.SDK_INT < 30) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE);
            return;
        }

        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.fromParts("package", getPackageName(), null));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION));
            } catch (ActivityNotFoundException unavailable) {
                showStorageError(unavailable);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode != REQUEST_STORAGE) return;
        if (isStorageAllowed(this)) continueStartup();
        else finish();
    }

    private void showStorageError(Exception error) {
        preparing = false;
        if (isFinishing() || isDestroyed()) return;
        storageDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.global_error)
                .setMessage(getString(R.string.shared_storage_error, error.getLocalizedMessage()))
                .setPositiveButton(R.string.global_retry, (dialog, which) -> {
                    storageDialog = null;
                    continueStartup();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> finish())
                .setOnCancelListener(dialog -> finish())
                .show();
    }

    private void openLauncher() {
        if (isFinishing() || isDestroyed()) return;
        LauncherPreferences.loadPreferences(this);
        AsyncAssetManager.unpackComponents(this);
        AsyncAssetManager.unpackSingleFiles(this);
        startActivity(new Intent(this, LauncherActivity.class));
        finish();
    }
}
