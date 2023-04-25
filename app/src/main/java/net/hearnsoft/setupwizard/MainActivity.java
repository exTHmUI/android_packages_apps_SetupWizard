package net.hearnsoft.setupwizard;

import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentActivity;

import net.hearnsoft.setupwizard.databinding.ActivityMainBinding;
import net.hearnsoft.setupwizard.utils.UIUtil;
import net.hearnsoft.setupwizard.utils.ViewAdapter;
import net.hearnsoft.setupwizard.utils.WizardManagerHelper;
import net.hearnsoft.setupwizard.views.OtherSettingsFragment;
import net.hearnsoft.setupwizard.views.DataSmsFragment;
import net.hearnsoft.setupwizard.views.DateTimeFragment;
import net.hearnsoft.setupwizard.views.FinishFragment;
import net.hearnsoft.setupwizard.views.IntroFragment;
import net.hearnsoft.setupwizard.views.GestureFragment;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "SetupWizard_Main";
    private ActivityMainBinding binding;
    private static StatusBarManager mStatusBarManager;
    private ViewAdapter adapter;
    private boolean isFinish = false;
    private boolean isWLANMode = false;
    public static final String ACTION_SETUP_NETWORK = "android.settings.NETWORK_PROVIDER_SETTINGS";
    public static final String EXTRA_PREFS_SHOW_BUTTON_BAR = "extra_prefs_show_button_bar";
    public static final String EXTRA_PREFS_SHOW_SKIP = "extra_prefs_show_skip";
    public static final String EXTRA_PREFS_SHOW_SKIP_TV = "extra_show_skip_network";
    public static final String EXTRA_PREFS_SET_BACK_TEXT = "extra_prefs_set_back_text";
    public static final String EXTRA_ENABLE_NEXT_ON_CONNECT = "wifi_enable_next_on_connect";



    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        adapter = new ViewAdapter(this);
        //binding.mMainBG.setRenderEffect(RenderEffect.createBlurEffect(150F,150F, Shader.TileMode.DECAL));
        initSetupViews();
        initNavBar();
    }

    @Override
    public void onBackPressed() {
        // 什么都不做
    }

    //初始化整个界面
    private void initSetupViews(){
        binding.pager.setAdapter(adapter);
        //注册Fragments
        adapter.addFragment(new IntroFragment());
        adapter.addFragment(new GestureFragment());
        adapter.addFragment(new DateTimeFragment());
        adapter.addFragment(new DataSmsFragment());
        adapter.addFragment(new OtherSettingsFragment());
        adapter.addFragment(new FinishFragment());
        //设置 ViewPager2属性
        binding.pager.setCurrentItem(0,/* smooth scroll */true);
        binding.pager.setUserInputEnabled(false);
        setSystemUIParam(false);
    }

    private void initNavBar(){
        setNavBtnShow(true,true);
        binding.navPriv.setOnClickListener(v -> priv());
        binding.navNext.setOnClickListener(v -> next());
    }

    //上一步按钮方法
   private void priv(){
        int current = binding.pager.getCurrentItem();
        if (current > 0 ){
            current--;
            binding.pager.setCurrentItem(current);
            setNavBtnShow(true,false);
            setNavBtnShow(false,false);
        }
        if (current == 0){
            setNavBtnShow(true,true);
            setNavBtnShow(false,false);
        }
    }

    // 下一步按钮方法
    private void next(){
        int current = binding.pager.getCurrentItem();
        if (isFinish){
            FinishSetupWizard();
        }
        if (!isWLANMode) {
            jumpToWLANSettings();
            isWLANMode = true;
        }
        if (current < adapter.getItemCount() -1 ){
            current++;
            binding.pager.setCurrentItem(current);
            setNavBtnShow(true,false);
            setNavBtnShow(false,false);
        }
        if (current == adapter.getItemCount() - 1 ){
            setNavBtnShow(true,true);
            setNavBtnShow(false,false);
            binding.navNext.setText(getString(R.string.btn_finish));
            binding.navNext.setIcon(AppCompatResources.getDrawable(getApplicationContext(),R.drawable.ic_check));
            isFinish = true;
        }
    }

    private void jumpToWLANSettings(){
        Intent intent = new Intent(ACTION_SETUP_NETWORK);
        intent.putExtra(WizardManagerHelper.EXTRA_IS_SETUP_FLOW, true);
        intent.putExtra(EXTRA_PREFS_SHOW_BUTTON_BAR, true);
        intent.putExtra(EXTRA_PREFS_SHOW_SKIP, true);
        intent.putExtra(EXTRA_PREFS_SHOW_SKIP_TV, true);
        intent.putExtra(EXTRA_PREFS_SET_BACK_TEXT, (String) null);
        intent.putExtra(EXTRA_ENABLE_NEXT_ON_CONNECT, true);
        startActivity(intent);
    }

    private void setNavBtnShow(boolean isPriv,boolean hide){
        if (isPriv){
            if (hide){
                binding.navPriv.setVisibility(View.INVISIBLE);
            }else{
                binding.navPriv.setVisibility(View.VISIBLE);
            }
        }else{
            if (hide){
                binding.navNext.setVisibility(View.INVISIBLE);
            }else{
                binding.navNext.setVisibility(View.VISIBLE);
            }
        }

    }

    private void FinishSetupWizard(){
        setSystemUIParam(true);
        Settings.Global.putInt(
                this.getContentResolver(),
                Settings.Global.DEVICE_PROVISIONED,
                1
        );
        Settings.Secure.putInt(
                this.getContentResolver(),
                Settings.Secure.USER_SETUP_COMPLETE,
                1
        );

        PackageManager manager = this.getPackageManager();
        ComponentName pkgName = new ComponentName(this,MainActivity.class);

        manager.setComponentEnabledSetting(pkgName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        finish();
    }

    private void setSystemUIParam(boolean status){
        if (status){
            UIUtil.enableStatusBar();
        } else {
            mStatusBarManager  = UIUtil.disableStatusBar(this);
        }
    }

    public static StatusBarManager getStatusBarManager() {
        return mStatusBarManager;
    }


}