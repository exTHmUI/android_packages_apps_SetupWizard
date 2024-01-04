package net.hearnsoft.setupwizard.views;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentMiscViewBinding.inflate(getLayoutInflater());
        intent = new Intent();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding.getRoot();
        binding.lockSettings.setOnClickListener(v -> {
            intent.setAction("android.app.action.SET_NEW_PASSWORD");
            startActivity(intent);
        });
        binding.soundSettings.setOnClickListener(v -> {
            intent.setAction("android.settings.SOUND_SETTINGS");
            startActivity(intent);
        });
        binding.multiuserSettings.setOnClickListener(v -> {
            if (checkIfSupportMultiUser()){
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
        binding.displaySettings.setOnClickListener(v -> {
            intent.setAction("android.settings.DISPLAY_SETTINGS");
            startActivity(intent);
        });
        return view;
    }

    private boolean checkIfSupportMultiUser(){
        Resources sysRes = Resources.getSystem();
        return sysRes.getBoolean(sysRes.getIdentifier("config_enableMultiUserUI", "bool", "android"));
    }


}
