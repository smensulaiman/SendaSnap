package com.sendajapan.sendasnap.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.activities.MainActivity;
import com.sendajapan.sendasnap.databinding.ActivityLoginBinding;
import com.sendajapan.sendasnap.models.User;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.MotionToastHelper;
import com.sendajapan.sendasnap.utils.SharedPrefsManager;

import java.util.Objects;

import www.sanju.motiontoast.MotionToast;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SharedPrefsManager prefsManager;
    private HapticFeedbackHelper hapticHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initHelpers();
        setupClickListeners();
    }

    private void initHelpers() {
        prefsManager = SharedPrefsManager.getInstance(this);
        hapticHelper = HapticFeedbackHelper.getInstance(this);
        loadSavedCredentials();
    }

    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            performLogin();
        });
    }

    private void performLogin() {
        String username = Objects.requireNonNull(binding.etUsername.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.etPassword.getText()).toString().trim();

        // Clear previous errors
        binding.tilUsername.setError(null);
        binding.tilPassword.setError(null);

        // Validate inputs
        if (TextUtils.isEmpty(username)) {
            binding.tilUsername.setError("Username is required");
            binding.etUsername.requestFocus();
            hapticHelper.vibrateError();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Password is required");
            binding.etPassword.requestFocus();
            hapticHelper.vibrateError();
            return;
        }

        if (password.length() < 3) {
            binding.tilPassword.setError("Password must be at least 3 characters");
            binding.etPassword.requestFocus();
            hapticHelper.vibrateError();
            return;
        }

        // Simple local validation (as per plan - no backend)
        if (isValidCredentials(username, password)) {
            // Login successful
            hapticHelper.vibrateSuccess();

            // Save user data
            User user = new User(username, username.contains("@") ? username : username + "@sendasnap.com");
            prefsManager.saveUser(user);

            // Handle "Remember Me" functionality
            if (binding.cbRememberMe.isChecked()) {
                saveCredentials(username, password);
            } else {
                clearSavedCredentials();
            }

            // Navigate to MainActivity
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    finish();
                }
            }, 700);
        } else {
            // Login failed
            binding.tilPassword.setError("Invalid credentials");
            binding.etPassword.requestFocus();
            MotionToastHelper.showError(this, "Login Failed", "Invalid username or password",
                    MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);
        }
    }

    private boolean isValidCredentials(String username, String password) {
        // Simple validation - accept any username with password length >= 3
        // In a real app, this would be validated against a backend
        return !TextUtils.isEmpty(username) && password.length() >= 3;
    }

    private void loadSavedCredentials() {
        String savedUsername = prefsManager.getSavedUsername();
        String savedPassword = prefsManager.getSavedPassword();
        boolean rememberMe = prefsManager.isRememberMeEnabled();

        if (rememberMe && !TextUtils.isEmpty(savedUsername) && !TextUtils.isEmpty(savedPassword)) {
            binding.etUsername.setText(savedUsername);
            binding.etPassword.setText(savedPassword);
            binding.cbRememberMe.setChecked(true);
        }
    }

    private void saveCredentials(String username, String password) {
        prefsManager.saveCredentials(username, password, true);
    }

    private void clearSavedCredentials() {
        prefsManager.saveCredentials("", "", false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}