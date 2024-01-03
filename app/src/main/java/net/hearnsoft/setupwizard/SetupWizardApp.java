package net.hearnsoft.setupwizard;

import android.app.Application;
import android.app.StatusBarManager;
import android.util.Log;

import net.hearnsoft.setupwizard.utils.UIUtil;

public class SetupWizardApp extends Application {

    public static final String TAG = SetupWizardApp.class.getSimpleName();
    // Verbose logging
    public static final boolean LOGV = Log.isLoggable(TAG, Log.VERBOSE);

    private static StatusBarManager sStatusBarManager;

    @Override
    public void onCreate() {
        super.onCreate();
        sStatusBarManager = UIUtil.disableStatusBar(this);
    }

    public static StatusBarManager getStatusBarManager() {
        return sStatusBarManager;
    }
}
