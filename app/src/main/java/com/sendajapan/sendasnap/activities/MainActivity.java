package com.sendajapan.sendasnap.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.databinding.ActivityMainBinding;
import com.sendajapan.sendasnap.fragments.HomeFragment;
import com.sendajapan.sendasnap.fragments.ScheduleFragment;
import com.sendajapan.sendasnap.fragments.ChatFragment;
import com.sendajapan.sendasnap.fragments.ProfileFragment;
import com.sendajapan.sendasnap.networking.NetworkUtils;
import com.sendajapan.sendasnap.ui.DrawerController;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.MotionToastHelper;

import me.ibrahimsn.lib.SmoothBottomBar;
import me.ibrahimsn.lib.OnItemSelectedListener;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NetworkUtils networkUtils;
    private HapticFeedbackHelper hapticHelper;
    public DrawerController drawerController;
    private boolean isNetworkToastShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ensure light status bar (dark icons) for visibility
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
            controller.setAppearanceLightNavigationBars(true);
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initHelpers();
        setupDrawer();
        setupBottomNavigation();
        setupNetworkMonitoring();

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void initHelpers() {
        networkUtils = NetworkUtils.getInstance(this);
        hapticHelper = HapticFeedbackHelper.getInstance(this);
    }

    private void setupDrawer() {
        // Initialize drawer controller without toolbar - will be set per fragment
        drawerController = new DrawerController(this,
                findViewById(R.id.drawerLayout),
                findViewById(R.id.navigationView),
                null);
    }

    private void setupBottomNavigation() {
        SmoothBottomBar bottomNav = findViewById(R.id.bottomNavigation);

        // Set item selection listener
        bottomNav.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int pos) {
                hapticHelper.vibrateClick();

                Fragment selectedFragment = null;

                switch (pos) {
                    case 0:
                        selectedFragment = new HomeFragment();
                        break;
                    case 1:
                        selectedFragment = new ScheduleFragment();
                        break;
                    case 2:
                        selectedFragment = new ChatFragment();
                        break;
                    case 3:
                        selectedFragment = new ProfileFragment();
                        break;
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                }

                return true;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();

        // Set up drawer for the fragment
        setupFragmentDrawer(fragment);
    }

    private void setupFragmentDrawer(Fragment fragment) {
        // Get the toolbar from the fragment and set up drawer
        if (fragment instanceof HomeFragment) {
            // HomeFragment will handle its own toolbar setup
        } else if (fragment instanceof ScheduleFragment) {
            // ScheduleFragment will handle its own toolbar setup
        } else if (fragment instanceof ChatFragment) {
            // ChatFragment will handle its own toolbar setup
        } else if (fragment instanceof ProfileFragment) {
            // ProfileFragment will handle its own toolbar setup
        }
    }

    public void switchToProfileTab() {
        // Switch to Profile tab (index 3)
        SmoothBottomBar bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setItemActiveIndex(3);
        loadFragment(new ProfileFragment());
    }

    private void setupNetworkMonitoring() {
        networkUtils.startNetworkCallback();

        networkUtils.getIsConnected().observe(this, isConnected -> {
            if (isConnected) {
                // Network is available
                if (isNetworkToastShowing) {
                    isNetworkToastShowing = false;
                    // Toast will automatically dismiss
                }
            } else {
                // No network
                if (!isNetworkToastShowing) {
                    isNetworkToastShowing = true;
                    showNoInternetToast();
                }
            }
        });
    }

    private void showNoInternetToast() {
        MotionToastHelper.showNoInternet(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkUtils != null) {
            networkUtils.stopNetworkCallback();
        }
        binding = null;
    }
}
