package net.hearnsoft.setupwizard.views;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.hearnsoft.setupwizard.R;
import net.hearnsoft.setupwizard.databinding.FragmentIntroViewBinding;
import net.hearnsoft.setupwizard.widgets.SuperTextView;

import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class IntroFragment extends Fragment {
    private FragmentIntroViewBinding binding;
    private Random random;
    private Timer timer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentIntroViewBinding.inflate(getLayoutInflater());
        random = new Random();
        timer = new Timer();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding.getRoot();
        binding.welcomeText.setTextColor(getResources().getColor(R.color.system_accent_darker));
        binding.welcomeText.setDynamicStyle(SuperTextView.DynamicStyle.TYPEWRITING);
        binding.welcomeLanguage.setText(getSystemLanguage(requireContext().getResources().getConfiguration()).getDisplayName());
        binding.welcomeLanguage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction("android.settings.LOCALE_SETTINGS");
            startActivity(intent);
        });
        binding.welcomeAccessibility.setOnClickListener(new AccessibilityClick());
        binding.welcomeEmergencyCall.setOnClickListener(new EmergencyDialerClick());
        randomText();
        return view;
    }

    private class AccessibilityClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setAction("android.settings.ACCESSIBILITY_SETTINGS");
            startActivity(intent);
        }
    }

    private class EmergencyDialerClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setAction("com.android.phone.EmergencyDialer.DIAL");
            startActivity(intent);
        }
    }

    private static Locale getSystemLanguage(Configuration configuration){
        return configuration.getLocales().get(0);
    }

    private void randomText(){
        String[] str = getResources().getStringArray(R.array.welcome_text);
        int length = getResources().getStringArray(R.array.welcome_text).length;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                int ran_int = random.nextInt(length);
                binding.welcomeText.post(() -> {
                    binding.welcomeText.setDynamicText(str[ran_int]);
                    binding.welcomeText.start();
                });
            }
        },0,5000);
    }

}
