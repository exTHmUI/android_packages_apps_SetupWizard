package net.hearnsoft.setupwizard.views;

import static android.os.UserHandle.USER_CURRENT;
import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_3BUTTON_OVERLAY;
import static android.view.WindowManagerPolicyConstants.NAV_BAR_MODE_GESTURAL_OVERLAY;

import android.app.StatusBarManager;
import android.content.Context;
import android.content.om.IOverlayManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.hearnsoft.setupwizard.R;
import net.hearnsoft.setupwizard.databinding.FragmentGestureViewBinding;
import net.hearnsoft.setupwizard.utils.GestureUtils;
import net.hearnsoft.setupwizard.utils.UIUtil;

public class GestureFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    private static final String TAG = GestureFragment.class.getSimpleName();
    private final int[] mIds = {R.id.gesture_navigation, R.id.three_button_navigation};
    private String[] mSelectorTitle;
    private FragmentGestureViewBinding binding;
    private IOverlayManager mOverlayManager;
    private AlertDialog dialogs;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentGestureViewBinding.inflate(getLayoutInflater());
        mOverlayManager = IOverlayManager.Stub.asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));
        this.mSelectorTitle = getResources().getStringArray(R.array.gesture_title_array);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding.getRoot();
        int gestureMode = getCurrentSystemNavigationMode(requireContext().getApplicationContext());
        Log.d(TAG, "GestureMode: " + gestureMode);
        dialogs = this.createLoadingDialog().create();
        ViewGroup.LayoutParams params = binding.gestureView.getLayoutParams();
        params.height = getResources().getDimensionPixelSize(R.dimen.setup_nav_settings_image_height) *
                (getResources().getDisplayMetrics().heightPixels / 1920);
        binding.gestureView.setLayoutParams(params);
        binding.selectorItems.setPadding(getResources().getDimensionPixelSize(R.dimen.setup_nav_settings_radio_group_padding_left), 0,
                getResources().getDimensionPixelSize(R.dimen.setup_nav_settings_radio_group_padding_right), 0);
        for (int i = 0; i < mIds.length; i++){
            RadioButton radioButton = new RadioButton(requireContext());
            radioButton.setId(mIds[i]);
            radioButton.setText(mSelectorTitle[i]);
            radioButton.setPadding(getResources().getDimensionPixelSize(R.dimen.setup_nav_settings_radio_group_padding_left) / 2,
                    getResources().getDimensionPixelSize(R.dimen.setup_nav_settings_radio_group_padding_top),
                    0,
                    getResources().getDimensionPixelSize(R.dimen.setup_nav_settings_radio_group_padding_bottom));
            binding.selectorItems.addView(radioButton, ViewGroup.LayoutParams.MATCH_PARENT, 156);
        }
        if (gestureMode == 0){
            binding.gestureView.setImageResource(R.drawable.system_nav_fully_gestural);
        } else if (gestureMode == 1){
            binding.gestureView.setImageResource(R.drawable.system_nav_3_button);
        }
        binding.selectorItems.setOnCheckedChangeListener(this);
        return view;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.gesture_navigation){
            binding.gestureView.setImageResource(R.drawable.system_nav_fully_gestural);
            dialogs.show();
            setCurrentSystemNavigationMode(mOverlayManager, 0);
        } else if (checkedId == R.id.three_button_navigation){
            binding.gestureView.setImageResource(R.drawable.system_nav_3_button);
            dialogs.show();
            setCurrentSystemNavigationMode(mOverlayManager, 1);
        }
    }

    private MaterialAlertDialogBuilder createLoadingDialog() {
        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.setup_nav_applying))
                .setCancelable(false)
                .setView(new ProgressBar(requireContext()))
                .setCancelable(false);
    }

    private int getCurrentSystemNavigationMode(Context context) {
        if (GestureUtils.isGestureNavigationEnabled(context)) {
            return 0;
        } else {
            return 1;
        }
    }

    private void setCurrentSystemNavigationMode(IOverlayManager overlayManager, int key) {
        String overlayPackage = NAV_BAR_MODE_GESTURAL_OVERLAY;
        switch (key) {
            case 0:
                overlayPackage = NAV_BAR_MODE_GESTURAL_OVERLAY;
                break;
            case 1:
                overlayPackage = NAV_BAR_MODE_3BUTTON_OVERLAY;
                break;
        }

        try {
            overlayManager.setEnabledExclusiveInCategory(overlayPackage, USER_CURRENT);
            UIUtil.disableStatusBar(requireContext().getApplicationContext());
        } catch (RemoteException | SecurityException e) {
            e.printStackTrace();
            dialogs.dismiss();
            Toast.makeText(requireContext(), getString(R.string.setup_nav_applying_failed), Toast.LENGTH_SHORT).show();
        }
        if (dialogs != null) {
            dialogs.dismiss();
        }
    }
}
