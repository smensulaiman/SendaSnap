package com.sendajapan.sendasnap.activities.auth;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.Gravity;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.activities.MainActivity;
import com.sendajapan.sendasnap.databinding.ActivityLoginBinding;
import com.sendajapan.sendasnap.models.ErrorResponse;
import com.sendajapan.sendasnap.models.LoginRequest;
import com.sendajapan.sendasnap.models.LoginResponse;
import com.sendajapan.sendasnap.models.UserData;
import com.sendajapan.sendasnap.networking.ApiService;
import com.sendajapan.sendasnap.networking.RetrofitClient;
import com.sendajapan.sendasnap.services.ChatService;
import com.sendajapan.sendasnap.utils.CookieBarToastHelper;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.MotionToastHelper;
import com.sendajapan.sendasnap.utils.SharedPrefsManager;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import www.sanju.motiontoast.MotionToast;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SharedPrefsManager prefsManager;
    private HapticFeedbackHelper hapticHelper;
    private ApiService apiService;
    private CookieBarToastHelper cookieBar;

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

        // Confirm app exit on back press from login screen
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (hapticHelper != null)
                    hapticHelper.vibrateClick();
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle("Exit App")
                        .setMessage("Are you sure you want to exit?")
                        .setPositiveButton("Exit", (dialog, which) -> {
                            dialog.dismiss();
                            finishAffinity();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .setCancelable(true)
                        .show();
            }
        });
    }

    private void initHelpers() {
        prefsManager = SharedPrefsManager.getInstance(LoginActivity.this);
        hapticHelper = HapticFeedbackHelper.getInstance(LoginActivity.this);
        apiService = RetrofitClient.getInstance().getApiService();

        cookieBar = new CookieBarToastHelper(LoginActivity.this);

        loadSavedCredentials();
    }

    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            performLogin();
        });
    }

    private void performLogin() {
        String email = Objects.requireNonNull(binding.etUsername.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.etPassword.getText()).toString().trim();

        // Clear previous errors
        binding.tilUsername.setError(null);
        binding.tilPassword.setError(null);

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            binding.tilUsername.setError("Email is required");
            binding.etUsername.requestFocus();
            hapticHelper.vibrateError();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilUsername.setError("Please enter a valid email address");
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

        // Show loading state
        setLoadingState(true);

        // Make API call
        Call<LoginResponse> call = apiService.login(new LoginRequest(email, password));
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                setLoadingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse.getSuccess() != null && loginResponse.getSuccess()) {
                        hapticHelper.vibrateSuccess();

                        // Extract user data and token
                        if (loginResponse.getData() != null) {
                            // Save token
                            String token = loginResponse.getData().getToken();
                            if (token != null) {
                                prefsManager.saveToken(token);
                            }

                            // Create User from API response
                            UserData user = loginResponse.getData().getUser();
                            prefsManager.saveUser(user);

                            // Initialize user in Firebase
                            ChatService.getInstance().initializeUser(LoginActivity.this);

                            // Handle "Remember Me" functionality
                            if (binding.cbRememberMe.isChecked()) {
                                saveCredentials(email, password);
                            } else {
                                clearSavedCredentials();
                            }

                            cookieBar.setCookieBarDismissListener(i -> new Handler(Looper.getMainLooper()).post(() -> {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                finish();
                            }));

                            cookieBar.showSuccessToast("Login Successful", loginResponse.getMessage(),
                                    Gravity.TOP, CookieBarToastHelper.DURATION_SHORT);
                        }

                    } else {
                        // Login failed with success: false
                        handleLoginError(loginResponse.getMessage());
                    }
                } else {
                    // Handle error response
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                setLoadingState(false);
                hapticHelper.vibrateError();
                MotionToastHelper.showError(LoginActivity.this, "Login Failed",
                        "Network error. Please check your connection and try again.",
                        MotionToast.GRAVITY_TOP, MotionToast.LONG_DURATION);
            }
        });
    }

    private void handleLoginError(String errorMessage) {
        hapticHelper.vibrateError();
        binding.tilPassword.setError(errorMessage != null ? errorMessage : "Invalid credentials");
        binding.etPassword.requestFocus();
        cookieBar.showErrorToast("Login Failed", errorMessage,
                Gravity.TOP, CookieBarToastHelper.DURATION_LONG);
    }

    private void handleErrorResponse(Response<LoginResponse> response) {
        hapticHelper.vibrateError();
        String errorMessage = "Invalid credentials";

        if (response.errorBody() != null) {
            try {
                Gson gson = new Gson();
                ErrorResponse errorResponse = gson.fromJson(response.errorBody().string(), ErrorResponse.class);
                if (errorResponse != null && errorResponse.getMessage() != null) {
                    errorMessage = errorResponse.getMessage();
                }
            } catch (Exception e) {
                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        binding.tilPassword.setError(errorMessage);
        binding.etPassword.requestFocus();

        cookieBar.showErrorToast("Login Failed", errorMessage,
                Gravity.TOP, CookieBarToastHelper.DURATION_LONG);
    }

    private void setLoadingState(boolean isLoading) {
        binding.btnLogin.setEnabled(!isLoading);
        binding.btnLogin.setText(isLoading ? "Logging in..." : "Login");
        binding.etUsername.setEnabled(!isLoading);
        binding.etPassword.setEnabled(!isLoading);
        binding.cbRememberMe.setEnabled(!isLoading);

        // Show/hide progress bar
        if (isLoading) {
            binding.progressBar.setVisibility(android.view.View.VISIBLE);
            binding.btnLogin.setTextColor(ContextCompat.getColor(this, android.R.color.transparent));
        } else {
            binding.progressBar.setVisibility(android.view.View.GONE);
            binding.btnLogin.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    private void loadSavedCredentials() {
        String savedEmail = prefsManager.getEmail();
        String savedPassword = prefsManager.getSavedPassword();
        boolean rememberMe = prefsManager.isRememberMeEnabled();

        if (rememberMe && !TextUtils.isEmpty(savedEmail) && !TextUtils.isEmpty(savedPassword)) {
            binding.etUsername.setText(savedEmail);
            binding.etPassword.setText(savedPassword);
            binding.cbRememberMe.setChecked(true);
        } else {
            binding.etUsername.setText("sulaiman@sendasnap.com");
            binding.etPassword.setText("password");
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