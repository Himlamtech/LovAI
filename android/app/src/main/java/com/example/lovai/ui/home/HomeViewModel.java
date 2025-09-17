package com.example.lovai.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<String> daysTogetherText = new MutableLiveData<>();
    private final MutableLiveData<String> weatherText = new MutableLiveData<>();
    private final MutableLiveData<String> weatherAdvice = new MutableLiveData<>();
    private final MutableLiveData<String> selectedDateWeather = new MutableLiveData<>();

    public HomeViewModel() {
        initializeData();
    }

    private void initializeData() {
        // Calculate days together (mock data - in real app this would come from user preferences)
        Calendar startDate = Calendar.getInstance();
        startDate.set(2022, Calendar.JUNE, 15); // Mock couple start date
        Calendar today = Calendar.getInstance();
        
        long diffInMillis = today.getTimeInMillis() - startDate.getTimeInMillis();
        long daysTogether = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
        
        daysTogetherText.setValue(daysTogether + " days together");
        
        // Mock weather data (in real app this would come from weather API)
        weatherText.setValue("75°F - Perfect date weather");
        weatherAdvice.setValue("Perfect for outdoor dates! 😊");
        selectedDateWeather.setValue("Sunny");
        
        // In a real implementation, you would:
        // 1. Fetch weather data from WeatherRepository
        // 2. Calculate days together from CoupleProfile
        // 3. Load calendar events from database
        // 4. Generate AI recommendations
    }

    // Getters for LiveData
    public LiveData<String> getDaysTogetherText() {
        return daysTogetherText;
    }

    public LiveData<String> getWeatherText() {
        return weatherText;
    }

    public LiveData<String> getWeatherAdvice() {
        return weatherAdvice;
    }

    public LiveData<String> getSelectedDateWeather() {
        return selectedDateWeather;
    }

    // Methods for user interactions
    public void onDateSelected(Calendar date) {
        // Handle date selection - update selected date weather
        // In real implementation, this would fetch weather for selected date
        selectedDateWeather.setValue("Sunny");
    }

    public void refreshWeather() {
        // Refresh weather data
        // In real implementation, this would call WeatherRepository.refreshWeather()
        weatherText.setValue("75°F - Perfect date weather");
        weatherAdvice.setValue("Perfect for outdoor dates! 😊");
    }

    public void refreshRecommendations() {
        // Refresh AI recommendations
        // In real implementation, this would call RecommendationRepository.getWeekendSuggestions()
    }
}
