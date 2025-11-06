package com.sendajapan.sendasnap.networking;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.sendajapan.sendasnap.activities.auth.LoginActivity;
import com.sendajapan.sendasnap.utils.SharedPrefsManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;
    private final SharedPrefsManager prefsManager;

    public AuthInterceptor(Context context) {
        this.context = context.getApplicationContext();
        this.prefsManager = SharedPrefsManager.getInstance(this.context);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Skip adding token for authentication endpoints (login, refresh)
        String url = originalRequest.url().toString();
        if (url.contains("/api/v1/auth/login") || url.contains("/api/v1/auth/refresh")) {
            return chain.proceed(originalRequest);
        }

        // Get token from SharedPreferences
        String token = prefsManager.getToken();

        // If no token exists and user is supposed to be logged in, logout
        if (token == null || token.isEmpty()) {
            if (prefsManager.isLoggedIn()) {
                // Token missing but user is logged in - logout
                logoutUser();
            }
            // Proceed without token (will likely fail with 401, but let the API handle it)
            return chain.proceed(originalRequest);
        }

        // Add Authorization header with bearer token
        Request authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer " + token)
                .build();

        Response response = chain.proceed(authenticatedRequest);

        // Handle 401 Unauthorized - token is invalid, logout user
        if (response.code() == 401) {
            if (prefsManager.isLoggedIn()) {
                logoutUser();
            }
        }

        return response;
    }

    private void logoutUser() {
        // Clear user data and token
        prefsManager.logout();

        // Navigate to LoginActivity
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}

