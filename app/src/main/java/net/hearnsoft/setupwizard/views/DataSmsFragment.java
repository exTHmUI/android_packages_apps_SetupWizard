package net.hearnsoft.setupwizard.views;

import android.os.Bundle;
import android.telephony.SubscriptionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.hearnsoft.setupwizard.R;
import net.hearnsoft.setupwizard.databinding.DialogDefaultSubidBinding;
import net.hearnsoft.setupwizard.databinding.FragmentSimSubViewBinding;
import net.hearnsoft.setupwizard.utils.DataSmsUtils;

public class DataSmsFragment extends Fragment {

    private FragmentSimSubViewBinding binding;
    private SubscriptionManager manager;
    private DataSmsUtils utils;
    private int[] subIds;
    private boolean isEnabledData;
    private final int[] mIds = {R.id.default_sim_sub_1, R.id.default_sim_sub_2};

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        utils = new DataSmsUtils(requireContext());
        binding = FragmentSimSubViewBinding.inflate(getLayoutInflater());
        manager = requireContext().getSystemService(SubscriptionManager.class);
        subIds = manager.getActiveSubscriptionIdList();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding.getRoot();
        binding.defaultCallSummary.setText(utils.getSummary(manager.getDefaultVoiceSubscriptionInfo()));
        binding.defaultSmsSummary.setText(utils.getSummary(manager.getDefaultSmsSubscriptionInfo()));
        binding.defaultDataSummary.setText(utils.getSummary(manager.getDefaultDataSubscriptionInfo()));
        binding.defaultCall.setOnClickListener(v -> { showSubscriptionDialogs(subIds,0); });
        binding.defaultSms.setOnClickListener(v -> { showSubscriptionDialogs(subIds,1); });
        binding.defaultCellureData.setOnClickListener(v -> { showSubscriptionDialogs(subIds,2); });
        return view;
    }

    private void showSubscriptionDialogs(int[] subIds,int options){
        if (subIds.length == 1 ){
            //什么都不做
            return;
        }
        DialogDefaultSubidBinding dialogBinding = DialogDefaultSubidBinding.inflate(getLayoutInflater());
        dialogBinding.subidSelectItems.setPadding(getResources().getDimensionPixelSize(R.dimen.setup_nav_settings_radio_group_padding_left), 0,
                getResources().getDimensionPixelSize(R.dimen.setup_nav_settings_radio_group_padding_right), 0);
        RadioButton subItem;
        for (int i = 0; i < subIds.length; i++) {
            subItem = createRadioButton(i, String.valueOf(utils.getSummary(manager.getAvailableSubscriptionInfoList().get(i))));
            dialogBinding.subidSelectItems.addView(subItem, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        subItem = new RadioButton(requireContext());
        subItem.setId(R.id.default_sim_sub_ask);
        subItem.setText(getString(R.string.calls_and_sms_ask_every_time));
        subItem.setPadding(getResources().getDimensionPixelSize(R.dimen.setup_nav_settings_radio_group_padding_left) / 2,
                getResources().getDimensionPixelSize(R.dimen.setup_nav_settings_radio_group_padding_top),
                0,
                getResources().getDimensionPixelSize(R.dimen.setup_nav_settings_radio_group_padding_bottom));
        dialogBinding.subidSelectItems.addView(subItem, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        View view = dialogBinding.getRoot();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.choose_default_sim_title))
                .setView(view)
                .setCancelable(true);
        AlertDialog dialog = builder.show();
        dialogBinding.subidSelectItems.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.default_sim_sub_1) {
                if (options == 2) {
                    boolean isShowWarning = showDataWarningAndSetDataEnabled();
                    if (isShowWarning) {
                        setDefaultSubscription(subIds[0], options);
                    }
                } else {
                    setDefaultSubscription(subIds[0], options);
                }
            } else if (checkedId == R.id.default_sim_sub_2) {
                if (options == 2) {
                    boolean isShowWarning = showDataWarningAndSetDataEnabled();
                    if (isShowWarning) {
                        setDefaultSubscription(subIds[1], options);
                    }
                } else {
                    setDefaultSubscription(subIds[1], options);
                }
            } else if (checkedId == R.id.default_sim_sub_ask) {
                setDefaultSubscription(-1, options);
            }
            dialog.dismiss();
        });
    }

    //设置默认订阅
    private void setDefaultSubscription(int subId,int options){
        if (options == 0){
            manager.setDefaultVoiceSubscriptionId(subId);
            binding.defaultCallSummary.setText(utils.getSummary(manager.getDefaultVoiceSubscriptionInfo()));
        } else if (options == 1){
            manager.setDefaultSmsSubId(subId);
            binding.defaultSmsSummary.setText(utils.getSummary(manager.getDefaultSmsSubscriptionInfo()));
        } else if (options == 2){
            utils.setDefaultDatSubId(manager,subId);
            binding.defaultDataSummary.setText(utils.getSummary(manager.getDefaultDataSubscriptionInfo()));
        }
    }

    //创建带有SubId的RadioButton
    private RadioButton createRadioButton(int i, String text) {
        RadioButton radioButton = new RadioButton(requireContext());
        radioButton.setId(mIds[i]);  // 使用subIds的索引作为id
        radioButton.setText(text);
        radioButton.setPadding(getResources().getDimensionPixelSize(R.dimen.setup_nav_settings_radio_group_padding_left) / 2,
                getResources().getDimensionPixelSize(R.dimen.setup_nav_settings_radio_group_padding_top),
                0,
                getResources().getDimensionPixelSize(R.dimen.setup_nav_settings_radio_group_padding_bottom));
        return radioButton;
    }

    //显示数据流量警告对话框
    private boolean showDataWarningAndSetDataEnabled() {
        boolean isShowWarning = createDataWarningDialog();
        if (isShowWarning) {
            utils.setDataEnabled(true);
        }
        return isShowWarning;
    }

    //创建数据流量警告对话框
    private boolean createDataWarningDialog(){
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.mobile_data_warning_title))
                .setMessage(getString(R.string.mobile_data_warning_message))
                .setCancelable(false)
                .setPositiveButton(getString(android.R.string.ok), (dialog, which) -> isEnabledData = true)
                .setNegativeButton(getString(android.R.string.cancel), (dialog, which) -> isEnabledData = false)
                .show();
        return isEnabledData;
    }


}
