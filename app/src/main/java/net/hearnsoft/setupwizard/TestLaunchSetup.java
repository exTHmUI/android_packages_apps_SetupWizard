package net.hearnsoft.setupwizard;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class TestLaunchSetup extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // define component name
        PackageManager manager = getPackageManager();
        ComponentName pkgName = new ComponentName(this, MainActivity.class);

        // if Activity was disabled, re-enable it.
        if (manager.getComponentEnabledSetting(pkgName) ==
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            manager.setComponentEnabledSetting(pkgName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
