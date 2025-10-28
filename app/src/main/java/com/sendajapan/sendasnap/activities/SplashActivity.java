package com.sendajapan.sendasnap.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.activities.auth.LoginActivity;
import com.sendajapan.sendasnap.utils.SharedPrefsManager;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Start logo animation
        startLogoAnimation();

        // Navigate after delay
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToNextActivity, SPLASH_DELAY);
    }

    private void startLogoAnimation() {
        View logoView = findViewById(R.id.imgLogo);

        // Scale animation
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoView, "scaleX", 0.0f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoView, "scaleY", 0.0f, 1.0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(logoView, "alpha", 0.0f, 1.0f);

        scaleX.setDuration(800);
        scaleY.setDuration(800);
        alpha.setDuration(800);

        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        alpha.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.start();
        scaleY.start();
        alpha.start();

        // Add a subtle bounce effect
        scaleX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Small bounce back effect
                ObjectAnimator bounceX = ObjectAnimator.ofFloat(logoView, "scaleX", 1.0f, 0.95f, 1.0f);
                ObjectAnimator bounceY = ObjectAnimator.ofFloat(logoView, "scaleY", 1.0f, 0.95f, 1.0f);

                bounceX.setDuration(200);
                bounceY.setDuration(200);

                bounceX.start();
                bounceY.start();
            }
        });
    }

    private void navigateToNextActivity() {
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(this);

        Intent intent;
        if (prefsManager.isLoggedIn()) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}