package com.example.lovai.ui.moments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.lovai.databinding.FragmentMomentsBinding;

public class MomentsFragment extends Fragment {

    private FragmentMomentsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMomentsBinding.inflate(inflater, container, false);
        
        // Placeholder content
        binding.tvPlaceholder.setText("Moments\nComing Soon 📸");
        
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
