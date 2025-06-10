package com.example.worshipsound.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.worshipsound.R;
import com.example.worshipsound.utils.ThemeManager;

/**
 * Splash screen activity that displays app logo and navigates to appropriate screen
 */
public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    private static final int SPLASH_DURATION = 3000; // 3 seconds
    
    private ThemeManager themeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize theme manager and apply theme before calling super
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        Log.d(TAG, "SplashActivity started");
        
        // Hide action bar for full screen experience
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        // Start timer to navigate to next screen
        new Handler().postDelayed(this::navigateToNextScreen, SPLASH_DURATION);
    }

    /**
     * Navigate to appropriate screen based on user session
     */
    private void navigateToNextScreen() {
        Intent intent;
        
        if (themeManager.isLoggedIn() && themeManager.isValidSession()) {
            // User is logged in, go to MainActivity
            intent = new Intent(this, MainActivity.class);
            Log.d(TAG, "Navigating to MainActivity - User logged in: " + themeManager.getUsername());
        } else {
            // User not logged in, go to LoginActivity
            intent = new Intent(this, LoginActivity.class);
            Log.d(TAG, "Navigating to LoginActivity - No valid session");
        }
        
        startActivity(intent);
        finish(); // Close splash activity
        
        // Add slide transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "SplashActivity paused");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "SplashActivity destroyed");
    }
}