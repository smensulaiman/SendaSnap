package com.sendajapan.sendasnap;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.firebase.database.FirebaseDatabase;
import com.sendajapan.sendasnap.utils.FcmNotificationSender;
import com.sendajapan.sendasnap.utils.SharedPrefsManager;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";
    private static MyApplication instance;
    private int activityCount = 0;
    private Activity currentActivity;

    public static MyApplication getInstance() {
        return instance;
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public boolean isAppInForeground() {
        return activityCount > 0;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        try {
            database.setPersistenceEnabled(false);
        } catch (Exception e) {
            Log.w(TAG, "Firebase persistence may already be enabled: " + e.getMessage());
        }

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                if (activity instanceof AppCompatActivity) {
                    applyWindowSettings(activity);
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
                activityCount++;
                currentActivity = activity;
                Log.d(TAG, "Activity started. Count: " + activityCount);
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                currentActivity = activity;
                
                // Setup notification listener when user is logged in and app comes to foreground
                SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(MyApplication.this);
                if (prefsManager.isLoggedIn()) {
                    FcmNotificationSender.setupNotificationListener(MyApplication.this);
                }
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {}

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
                activityCount--;
                if (activityCount < 0) {
                    activityCount = 0;
                }
                if (currentActivity == activity) {
                    currentActivity = null;
                }
                Log.d(TAG, "Activity stopped. Count: " + activityCount);
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                if (currentActivity == activity) {
                    currentActivity = null;
                }
            }
        });

        // Setup notification listener on app start if user is logged in
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(this);
        if (prefsManager.isLoggedIn()) {
            FcmNotificationSender.setupNotificationListener(this);
        }

        Log.d(TAG, "MyApplication initialized");
    }

    private void applyWindowSettings(Activity activity) {
        if (activity.getWindow() == null) return;

        activity.getWindow().setStatusBarColor(
            activity.getResources().getColor(R.color.status_bar_color, activity.getTheme())
        );
        activity.getWindow().setNavigationBarColor(
            activity.getResources().getColor(R.color.navigation_bar_color, activity.getTheme())
        );

        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(
            activity.getWindow().getDecorView()
        );
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
        }
    }

    public static void applyWindowInsets(View rootView) {
        if (rootView == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}

