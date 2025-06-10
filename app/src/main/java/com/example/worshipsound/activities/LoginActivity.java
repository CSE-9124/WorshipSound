package com.example.worshipsound.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.worshipsound.R;
import com.example.worshipsound.database.SongDAO;
import com.example.worshipsound.models.User;
import com.example.worshipsound.utils.ThemeManager;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Login and Registration Activity
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    
    // UI Components
    private EditText etUsername, etEmail, etPassword, etFirstName, etLastName;
    private TextInputLayout tilUsername, tilEmail, tilPassword, tilFirstName, tilLastName;
    private Button btnLogin, btnRegister;
    private TextView tvTitle, tvSubtitle, tvToggleMode;
    private ProgressBar progressBar;
    
    // Utilities
    private ThemeManager themeManager;
    private SongDAO songDAO;
    private ExecutorService executorService;
    
    // State
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme first
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        // Initialize components
        initializeViews();
        initializeUtils();
        setupClickListeners();
        updateUIMode();
        
        Log.d(TAG, "LoginActivity created");
    }

    /**
     * Initialize all view components
     */
    private void initializeViews() {
        tilUsername = findViewById(R.id.til_username);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilFirstName = findViewById(R.id.til_first_name);
        tilLastName = findViewById(R.id.til_last_name);

        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        tvToggleMode = findViewById(R.id.tv_toggle_mode);
        
        tvTitle = findViewById(R.id.tv_title);
        tvSubtitle = findViewById(R.id.tv_subtitle);
        progressBar = findViewById(R.id.progress_bar);
    }

    /**
     * Initialize utility classes
     */
    private void initializeUtils() {
        songDAO = SongDAO.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();
    }

    /**
     * Setup click listeners for buttons
     */
    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());
        btnRegister.setOnClickListener(v -> handleRegister());
        tvToggleMode.setOnClickListener(v -> toggleMode());
    }

    /**
     * Update UI based on current mode (login/register)
     */
    private void updateUIMode() {
        if (isLoginMode) {
            // Login mode
            tvTitle.setText("Welcome Back");
            tvSubtitle.setText("Sign in to continue your worship journey");

            tilEmail.setVisibility(View.GONE);
            tilFirstName.setVisibility(View.GONE);
            tilLastName.setVisibility(View.GONE);
            
            etEmail.setVisibility(View.VISIBLE);
            etFirstName.setVisibility(View.GONE);
            etLastName.setVisibility(View.GONE);
            
            btnLogin.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.GONE);
            tvToggleMode.setText("Don't have an account? Register");
        } else {
            // Register mode
            tvTitle.setText("Join WorshipSound");
            tvSubtitle.setText("Create an account to save your favorite worship songs");
            tvSubtitle.setGravity(View.TEXT_ALIGNMENT_CENTER);

            tilEmail.setVisibility(View.VISIBLE);
            tilFirstName.setVisibility(View.VISIBLE);
            tilLastName.setVisibility(View.VISIBLE);
            
            etEmail.setVisibility(View.VISIBLE);
            etFirstName.setVisibility(View.VISIBLE);
            etLastName.setVisibility(View.VISIBLE);
            
            btnLogin.setVisibility(View.GONE);
            btnRegister.setVisibility(View.VISIBLE);
            tvToggleMode.setText("Already have an account? Login");
        }
    }

    /**
     * Toggle between login and register mode
     */
    private void toggleMode() {
        isLoginMode = !isLoginMode;
        updateUIMode();
        clearInputs();
        Log.d(TAG, "Toggled to " + (isLoginMode ? "Login" : "Register") + " mode");
    }

    /**
     * Handle login process
     */
    private void handleLogin() {
        String usernameOrEmail = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // Validate inputs
        if (!validateLoginInputs(usernameOrEmail, password)) {
            return;
        }
        
        showLoading(true);
        
        // Perform login in background thread
        executorService.execute(() -> {
            try {
                User user = songDAO.getUser(usernameOrEmail, password);
                
                runOnUiThread(() -> {
                    showLoading(false);
                    
                    if (user != null) {
                        // Login successful
                        themeManager.saveUserSession(user.getId(), user.getUsername(), user.getEmail());
                        themeManager.setDarkTheme(user.isDarkTheme());
                        
                        Toast.makeText(this, "Welcome back, " + user.getDisplayName() + "!", 
                                     Toast.LENGTH_SHORT).show();
                        
                        navigateToMainActivity();
                        Log.d(TAG, "Login successful for user: " + user.getUsername());
                    } else {
                        // Login failed
                        Toast.makeText(this, "Invalid username/email or password", 
                                     Toast.LENGTH_LONG).show();
                        Log.w(TAG, "Login failed for: " + usernameOrEmail);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error during login", e);
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Login error: " + e.getMessage(), 
                                 Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Handle registration process
     */
    private void handleRegister() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        
        // Validate inputs
        if (!validateRegisterInputs(username, email, password, firstName, lastName)) {
            return;
        }
        
        showLoading(true);
        
        // Perform registration in background thread
        executorService.execute(() -> {
            try {
                // Check if user already exists
                if (songDAO.userExists(username, email)) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(this, "Username or email already exists", 
                                     Toast.LENGTH_LONG).show();
                    });
                    return;
                }
                
                // Create new user
                User newUser = new User(username, email, password, firstName, lastName);
                long userId = songDAO.insertUser(newUser);
                
                runOnUiThread(() -> {
                    showLoading(false);
                    
                    if (userId > 0) {
                        // Registration successful
                        Toast.makeText(this, "Registration successful! Please login.", 
                                     Toast.LENGTH_SHORT).show();
                        
                        // Switch to login mode and pre-fill username
                        isLoginMode = true;
                        updateUIMode();
                        etUsername.setText(username);
                        
                        Log.d(TAG, "Registration successful for user: " + username);
                    } else {
                        Toast.makeText(this, "Registration failed. Please try again.", 
                                     Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Registration failed for user: " + username);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error during registration", e);
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(this, "Registration error: " + e.getMessage(), 
                                 Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    /**
     * Validate login inputs
     */
    private boolean validateLoginInputs(String usernameOrEmail, String password) {
        if (usernameOrEmail.isEmpty()) {
            etUsername.setError("Username or email is required");
            etUsername.requestFocus();
            return false;
        }
        
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }
        
        return true;
    }

    /**
     * Validate registration inputs
     */
    private boolean validateRegisterInputs(String username, String email, String password, 
                                         String firstName, String lastName) {
        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return false;
        }
        
        if (username.length() < 3) {
            etUsername.setError("Username must be at least 3 characters");
            etUsername.requestFocus();
            return false;
        }
        
        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }
        
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }
        
        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }
        
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }
        
        return true;
    }

    /**
     * Show/hide loading indicator
     */
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!show);
        btnRegister.setEnabled(!show);
        tvToggleMode.setEnabled(!show);
    }

    /**
     * Clear all input fields
     */
    private void clearInputs() {
        etUsername.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etFirstName.setText("");
        etLastName.setText("");
        etUsername.requestFocus();
    }

    /**
     * Navigate to MainActivity
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        Log.d(TAG, "LoginActivity destroyed");
    }
}