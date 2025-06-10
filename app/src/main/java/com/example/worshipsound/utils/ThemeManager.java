package com.example.worshipsound.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Utility class for managing app themes and user preferences
 */
public class ThemeManager {
    private static final String TAG = "ThemeManager";
    private static final String PREFS_NAME = "worship_sound_prefs";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_FIRST_LAUNCH = "first_launch";

    private final SharedPreferences prefs;
    private static ThemeManager instance;

    private ThemeManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get singleton instance of ThemeManager
     */
    public static synchronized ThemeManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeManager(context.getApplicationContext());
        }
        return instance;
    }

    // Theme management methods

    /**
     * Apply saved theme on app startup
     */
    public void applyTheme() {
        boolean isDarkMode = isDarkTheme();
        int themeMode = isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(themeMode);
        Log.d(TAG, "Applied theme: " + (isDarkMode ? "Dark" : "Light"));
    }

    /**
     * Toggle between light and dark theme
     */
    public void toggleTheme() {
        boolean currentDarkMode = isDarkTheme();
        setDarkTheme(!currentDarkMode);
        applyTheme();
        Log.d(TAG, "Theme toggled to: " + (!currentDarkMode ? "Dark" : "Light"));
    }

    /**
     * Set dark theme preference
     */
    public void setDarkTheme(boolean isDark) {
        prefs.edit().putBoolean(KEY_THEME_MODE, isDark).apply();
    }

    /**
     * Check if dark theme is enabled
     */
    public boolean isDarkTheme() {
        return prefs.getBoolean(KEY_THEME_MODE, false);
    }

    // User session management methods

    /**
     * Save user session after successful login
     */
    public void saveUserSession(int userId, String username, String email) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
        
        Log.d(TAG, "User session saved: " + username);
    }

    /**
     * Clear user session (logout)
     */
    public void clearUserSession() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_USER_EMAIL);
        editor.apply();
        
        Log.d(TAG, "User session cleared");
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get logged in user ID
     */
    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    /**
     * Get logged in username
     */
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    /**
     * Get logged in user email
     */
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    // App state management

    /**
     * Check if this is the first app launch
     */
    public boolean isFirstLaunch() {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    /**
     * Mark that the app has been launched before
     */
    public void setFirstLaunchCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
        Log.d(TAG, "First launch completed");
    }

    // Additional preference methods

    /**
     * Save a string preference
     */
    public void saveString(String key, String value) {
        prefs.edit().putString(key, value).apply();
    }

    /**
     * Get a string preference
     */
    public String getString(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    /**
     * Save a boolean preference
     */
    public void saveBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    /**
     * Get a boolean preference
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    /**
     * Save an integer preference
     */
    public void saveInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
    }

    /**
     * Get an integer preference
     */
    public int getInt(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    /**
     * Clear all preferences (for testing or reset)
     */
    public void clearAllPreferences() {
        prefs.edit().clear().apply();
        Log.d(TAG, "All preferences cleared");
    }

    /**
     * Get user display information
     */
    public String getUserDisplayName() {
        String username = getUsername();
        return username.isEmpty() ? "User" : username;
    }

    /**
     * Check if user session is valid
     */
    public boolean isValidSession() {
        return isLoggedIn() && getUserId() != -1 && !getUsername().isEmpty();
    }
}
