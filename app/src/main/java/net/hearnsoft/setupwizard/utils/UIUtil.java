package net.hearnsoft.setupwizard.utils;

import android.app.StatusBarManager;
import android.content.Context;
import android.util.Log;

import net.hearnsoft.setupwizard.MainActivity;

public class UIUtil {
    private static final String TAG = "UIUtil";

    public static StatusBarManager disableStatusBar(Context context) {
        StatusBarManager statusBarManager = context.getSystemService(StatusBarManager.class);
        if (statusBarManager != null) {
            Log.v(TAG, "Disabling status bar");
            statusBarManager.setDisabledForSetup(true);
        } else {
            Log.w(TAG, "Skip disabling status bar - could not get StatusBarManager");
        }
        return statusBarManager;
    }

    public static void enableStatusBar() {
        StatusBarManager statusBarManager = MainActivity.getStatusBarManager();
        if (statusBarManager != null) {
            Log.v(TAG, "Enabling status bar");
            statusBarManager.setDisabledForSetup(false);
        } else {
            Log.w(TAG, "Skip enabling status bar - could not get StatusBarManager");
        }
    }
}
