package com.example.worshipsound.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.worshipsound.R;
import com.example.worshipsound.database.SongDAO;
import com.example.worshipsound.utils.MediaPlayerManager;
import com.example.worshipsound.utils.ThemeManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.navigation.fragment.NavHostFragment;

/**
 * Main activity that hosts fragments using Navigation Component
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    
    // Navigation
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    
    // Utilities
    private ThemeManager themeManager;
    private MediaPlayerManager mediaPlayerManager;
    private SongDAO songDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize theme manager and apply theme
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize components
        initializeViews();
        initializeUtils();
        setupNavigation();
        
        Log.d(TAG, "MainActivity created for user: " + themeManager.getUsername());
    }

    /**
     * Initialize view components
     */
    private void initializeViews() {
        // Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("WorshipSound");
        }
    }

    /**
     * Initialize utility classes
     */
    private void initializeUtils() {
        mediaPlayerManager = MediaPlayerManager.getInstance();
        songDAO = SongDAO.getInstance(this);
    }

    /**
     * Setup navigation component with bottom navigation
     */
    private void setupNavigation() {
        // Get NavController using NavHostFragment directly
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Setup Bottom Navigation
            BottomNavigationView bottomNavView = findViewById(R.id.bottom_navigation);
            NavigationUI.setupWithNavController(bottomNavView, navController);

            // Configure app bar with top-level destinations
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home,
                    R.id.navigation_search,
                    R.id.navigation_playlist
            ).build();

            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

            // Handle navigation item selection
            bottomNavView.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_home) {
                    navController.navigate(R.id.navigation_home);
                    return true;
                } else if (itemId == R.id.navigation_search) {
                    navController.navigate(R.id.navigation_search);
                    return true;
                } else if (itemId == R.id.navigation_playlist) {
                    navController.navigate(R.id.navigation_playlist);
                    return true;
                }

                return false;
            });

            Log.d(TAG, "Navigation setup completed");
        } else {
            Log.e(TAG, "NavHostFragment not found");
            Toast.makeText(this, "Error setting up navigation", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        
        // Update theme icon based on current theme
        MenuItem themeItem = menu.findItem(R.id.action_toggle_theme);
        if (themeItem != null) {
            updateThemeIcon(themeItem);
        }
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.action_toggle_theme) {
            toggleTheme(item);
            return true;
        } else if (itemId == R.id.action_refresh) {
            handleRefresh();
            return true;
        } else if (itemId == R.id.action_logout) {
            handleLogout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }

    /**
     * Toggle app theme
     */
    private void toggleTheme(MenuItem themeItem) {
        themeManager.toggleTheme();
        updateThemeIcon(themeItem);
        
        // Update user theme preference in database
        int userId = themeManager.getUserId();
        if (userId != -1) {
            songDAO.updateUserTheme(userId, themeManager.isDarkTheme());
        }
        
        Toast.makeText(this, 
                themeManager.isDarkTheme() ? "Dark theme enabled" : "Light theme enabled", 
                Toast.LENGTH_SHORT).show();
        
        Log.d(TAG, "Theme toggled to: " + (themeManager.isDarkTheme() ? "Dark" : "Light"));
    }

    /**
     * Update theme icon based on current theme
     */
    private void updateThemeIcon(MenuItem themeItem) {
        if (themeManager.isDarkTheme()) {
            themeItem.setIcon(R.drawable.ic_light_mode);
            themeItem.setTitle("Light Mode");
        } else {
            themeItem.setIcon(R.drawable.ic_dark_mode);
            themeItem.setTitle("Dark Mode");
        }
    }

    /**
     * Handle refresh action
     */
    private void handleRefresh() {
        // Broadcast refresh event to current fragment
        // This can be done through the NavController or by using a listener pattern
        Toast.makeText(this, "Refreshing...", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Refresh action triggered");
        
        // You can add fragment-specific refresh logic here
        // For example, refresh the current fragment's data
    }

    /**
     * Handle user logout
     */
    private void handleLogout() {
        // Stop any playing media
        mediaPlayerManager.stopPlayback();
        
        // Clear user session
        themeManager.clearUserSession();
        
        // Navigate to login activity
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "User logged out");
        
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        // Release media player resources
        if (mediaPlayerManager != null) {
            mediaPlayerManager.release();
        }
        
        Log.d(TAG, "MainActivity destroyed");
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        // Pause media playback when activity goes to background
        if (mediaPlayerManager != null && mediaPlayerManager.isPlaying()) {
            mediaPlayerManager.pausePlayback();
        }
        
        Log.d(TAG, "MainActivity paused");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity resumed");
    }

    // Public methods for fragment communication

    /**
     * Get current user display name for fragments
     */
    public String getCurrentUserDisplayName() {
        return themeManager.getUserDisplayName();
    }

    /**
     * Check if user is in dark theme
     */
    public boolean isDarkTheme() {
        return themeManager.isDarkTheme();
    }

    /**
     * Get current navigation controller
     */
    public NavController getNavController() {
        return navController;
    }
}