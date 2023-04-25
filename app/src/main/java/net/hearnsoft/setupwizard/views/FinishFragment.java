package net.hearnsoft.setupwizard.views;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.hearnsoft.setupwizard.R;
import net.hearnsoft.setupwizard.databinding.FragmentFinishViewBinding;

import java.util.Random;

public class FinishFragment extends Fragment {

    private FragmentFinishViewBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentFinishViewBinding.inflate(getLayoutInflater());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = binding.getRoot();
        binding.endImg.setOnClickListener(v -> {
            String[] str = getResources().getStringArray(R.array.random_text);
            int length = getResources().getStringArray(R.array.random_text).length;
            int random = genRandomInt(length);
            Toast.makeText(requireContext(),str[random],Toast.LENGTH_SHORT).show();
        });
        return view;
    }

    private int genRandomInt(int max){
        Random random = new Random();
        return random.nextInt(max - 1);
    }
}
