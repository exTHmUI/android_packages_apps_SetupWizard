package net.hearnsoft.setupwizard.views;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.hardware.biometrics.BiometricManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.hearnsoft.setupwizard.R;
import net.hearnsoft.setupwizard.databinding.FragmentMiscViewBinding;

import java.util.List;

public class OtherSettingsFragment extends Fragment {

    private static final String LAUNCHED_SETTINGS = "app_launched_settings";
    private FragmentMiscViewBinding binding;
    private Intent intent;

    private String mWallpaperPackage;
    private String mWallpaperClass;
    private String mStylesAndWallpaperClass;
    private String mWallpaperLaunchExtra;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentMiscViewBinding.inflate(getLayoutInflater());
        mWallpaperPackage = requireContext().getString(R.string.config_wallpaper_picker_package);
        mWallpaperClass = requireContext().getString(R.string.config_wallpaper_picker_class);
        mStylesAndWallpaperClass =
                requireContext().getString(R.string.config_styles_and_wallpaper_picker_class);
        mWallpaperLaunchExtra =
                requireContext().getString(R.string.config_wallpaper_picker_launch_extra);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding.getRoot();
        binding.lockSettings.setOnClickListener(v -> {
            intent = new Intent();
            intent.setAction("android.app.action.SET_NEW_PASSWORD");
            startActivity(intent);
        });
        binding.fingerprintSettings.setOnClickListener(v -> {
            if (checkIfSupportFingerprint()){
                intent = new Intent();
                intent.setAction("android.settings.FINGERPRINT_SETTINGS");
                startActivity(intent);
            } else {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.fingerprint_not_support)
                        .setMessage(R.string.fingerprint_not_support_summary)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });
        binding.multiuserSettings.setOnClickListener(v -> {
            if (checkIfSupportMultiUser()){
                intent = new Intent();
                intent.setAction("android.settings.USER_SETTINGS");
                startActivity(intent);
            } else {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.multiuser_not_support)
                        .setMessage(R.string.multiuser_not_support_summary)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });
        binding.wallpaperSettings.setOnClickListener(v -> {
            intent = new Intent().setComponent(
                    getComponentName()).putExtra(mWallpaperLaunchExtra, LAUNCHED_SETTINGS);
            if (areStylesAvailable()) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            startActivity(intent);
        });
        return view;
    }

    private boolean checkIfSupportFingerprint(){
        BiometricManager biometricManager = requireContext().getSystemService(BiometricManager.class);
        return biometricManager.canAuthenticate(UserHandle.USER_CURRENT, BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private boolean checkIfSupportMultiUser(){
        Resources sysRes = Resources.getSystem();
        return sysRes.getBoolean(sysRes.getIdentifier("config_enableMultiUserUI", "bool", "android"));
    }

    public ComponentName getComponentName() {
        return new ComponentName(mWallpaperPackage, getComponentClassString());
    }

    public String getComponentClassString() {
        return areStylesAvailable() ? mStylesAndWallpaperClass : mWallpaperClass;
    }

    /** Returns whether Styles & Wallpaper is enabled and available. */
    public boolean areStylesAvailable() {
        return !TextUtils.isEmpty(mStylesAndWallpaperClass)
                && canResolveWallpaperComponent(mStylesAndWallpaperClass);
    }

    private boolean canResolveWallpaperComponent(String className) {
        final ComponentName componentName = new ComponentName(mWallpaperPackage, className);
        final PackageManager pm = requireContext().getPackageManager();
        final Intent intent = new Intent().setComponent(componentName);
        final List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0 /* flags */);
        return resolveInfos != null && !resolveInfos.isEmpty();
    }


}
