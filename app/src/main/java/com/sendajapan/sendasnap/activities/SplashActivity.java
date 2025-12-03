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

import com.sendajapan.sendasnap.BuildConfig;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.activities.auth.LoginActivity;
import com.sendajapan.sendasnap.databinding.ActivitySplashBinding;
import com.sendajapan.sendasnap.utils.SharedPrefsManager;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY_MS = 2000;
    private static final int ANIMATION_DURATION_MS = 800;
    private static final int BOUNCE_DURATION_MS = 200;
    private static final float INITIAL_SCALE = 0.0f;
    private static final float FINAL_SCALE = 1.0f;
    private static final float BOUNCE_SCALE = 0.95f;
    private static final float INITIAL_ALPHA = 0.0f;
    private static final float FINAL_ALPHA = 1.0f;

    private ActivitySplashBinding binding;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_splash);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (!BuildConfig.DEBUG) {
            binding.txtVersion.setText(BuildConfig.VERSION_NAME);
        } else {
            binding.txtVersion.setVisibility(View.GONE);
        }

        startLogoAnimation();

        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToNextActivity, SPLASH_DELAY_MS);
    }

    private void startLogoAnimation() {
        View logoView = findViewById(R.id.imgLogo);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(logoView, "scaleX", INITIAL_SCALE, FINAL_SCALE);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(logoView, "scaleY", INITIAL_SCALE, FINAL_SCALE);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(logoView, "alpha", INITIAL_ALPHA, FINAL_ALPHA);

        scaleX.setDuration(ANIMATION_DURATION_MS);
        scaleY.setDuration(ANIMATION_DURATION_MS);
        alpha.setDuration(ANIMATION_DURATION_MS);

        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        alpha.setInterpolator(new AccelerateDecelerateInterpolator());

        scaleX.start();
        scaleY.start();
        alpha.start();

        scaleX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ObjectAnimator bounceX = ObjectAnimator.ofFloat(logoView, "scaleX", FINAL_SCALE, BOUNCE_SCALE, FINAL_SCALE);
                ObjectAnimator bounceY = ObjectAnimator.ofFloat(logoView, "scaleY", FINAL_SCALE, BOUNCE_SCALE, FINAL_SCALE);

                bounceX.setDuration(BOUNCE_DURATION_MS);
                bounceY.setDuration(BOUNCE_DURATION_MS);

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