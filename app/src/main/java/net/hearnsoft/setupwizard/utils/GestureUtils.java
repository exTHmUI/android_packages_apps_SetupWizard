package net.hearnsoft.setupwizard.utils;

import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_GESTURAL;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class GestureUtils {

    private static final String ACTION_QUICKSTEP = "android.intent.action.QUICKSTEP_SERVICE";

    public static boolean isGestureNavigationEnabled(Context context) {
        return NAV_BAR_MODE_GESTURAL == context.getResources().getInteger(
                com.android.internal.R.integer.config_navBarInteractionMode);
    }

    public static boolean isGestureAvailable(Context context) {
        // Skip if the swipe up settings are not available
        if (!context.getResources().getBoolean(
                com.android.internal.R.bool.config_swipe_up_gesture_setting_available)) {
            return false;
        }

        // Skip if the recents component is not defined
        final ComponentName recentsComponentName = ComponentName.unflattenFromString(
                context.getString(com.android.internal.R.string.config_recentsComponentName));
        if (recentsComponentName == null) {
            return false;
        }

        // Skip if the overview proxy service exists
        final Intent quickStepIntent = new Intent(ACTION_QUICKSTEP)
                .setPackage(recentsComponentName.getPackageName());
        if (context.getPackageManager().resolveService(quickStepIntent,
                PackageManager.MATCH_SYSTEM_ONLY) == null) {
            return false;
        }

        return true;
    }

    public static boolean isOverlayPackageAvailable(Context context, String overlayPackage) {
        try {
            return context.getPackageManager().getPackageInfo(overlayPackage, 0) != null;
        } catch (PackageManager.NameNotFoundException e) {
            // Not found, just return unavailable
            e.printStackTrace();
            return false;
        }
    }

}
