package net.hearnsoft.setupwizard.utils;

import static net.hearnsoft.setupwizard.SetupWizardApp.LOGV;

import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

import net.hearnsoft.setupwizard.MainActivity;
import net.hearnsoft.setupwizard.SetupWizardApp;

public class UIUtil {
    private static final String TAG = "UIUtil";

    public static StatusBarManager disableStatusBar(Context context) {
        StatusBarManager statusBarManager = context.getSystemService(StatusBarManager.class);
        if (statusBarManager != null) {
            if (LOGV) {
                Log.v(TAG, "Disabling status bar");
            }
            statusBarManager.setDisabledForSetup(true);
        } else {
            Log.w(TAG,
                    "Skip disabling status bar - could not get StatusBarManager");
        }
        return statusBarManager;
    }

    public static void enableStatusBar(Context context) {
        final SetupWizardApp setupWizardApp = (SetupWizardApp)context.getApplicationContext();
        StatusBarManager statusBarManager = setupWizardApp.getStatusBarManager();
        if (statusBarManager != null) {
            if (LOGV) {
                Log.v(SetupWizardApp.TAG, "Enabling status bar");
            }
            statusBarManager.setDisabledForSetup(false);

            // Session must be destroyed if it's not used anymore
            statusBarManager = null;
        } else {
            Log.w(SetupWizardApp.TAG,
                    "Skip enabling status bar - could not get StatusBarManager");
        }
    }

    public static void finishSetupWizard(Context context){
        Settings.Global.putInt(
                context.getContentResolver(),
                Settings.Global.DEVICE_PROVISIONED,
                1
        );
        Settings.Secure.putInt(
                context.getContentResolver(),
                Settings.Secure.USER_SETUP_COMPLETE,
                1
        );

        PackageManager manager = context.getPackageManager();
        ComponentName pkgName = new ComponentName(context, MainActivity.class);

        manager.setComponentEnabledSetting(pkgName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

}
