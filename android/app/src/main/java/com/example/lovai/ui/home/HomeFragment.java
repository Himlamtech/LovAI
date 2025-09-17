package com.example.lovai.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lovai.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        
        setupObservers();
        setupViews();
        
        return binding.getRoot();
    }

    private void setupObservers() {
        homeViewModel.getDaysTogetherText().observe(getViewLifecycleOwner(), daysText -> {
            binding.tvDaysTogether.setText(daysText);
        });

        homeViewModel.getWeatherText().observe(getViewLifecycleOwner(), weatherText -> {
            binding.tvWeatherCondition.setText(weatherText);
        });

        homeViewModel.getWeatherAdvice().observe(getViewLifecycleOwner(), adviceText -> {
            binding.chipWeatherAdvice.setText(adviceText);
        });
    }

    private void setupViews() {
        // Initialize views with default data
        binding.tvAppName.setText("LovAi");
        binding.tvSubtitle.setText("Together Forever");
        
        // Set up calendar view (basic implementation)
        setupCalendar();
        
        // Set up weekend suggestions
        setupWeekendSuggestions();
    }

    private void setupCalendar() {
        // Basic calendar setup - in a real implementation, this would be more sophisticated
        binding.tvCurrentMonth.setText("April 2023");
    }

    private void setupWeekendSuggestions() {
        // Set up weekend suggestions header
        binding.tvWeekendHeader.setText("Weekend looks great! 🌟");
        binding.chipAiSuggested.setText("AI Suggested");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
