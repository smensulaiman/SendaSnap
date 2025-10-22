package com.sendajapan.sendasnap.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.databinding.ActivityMainBinding;
import com.sendajapan.sendasnap.fragments.HomeFragment;
import com.sendajapan.sendasnap.fragments.HistoryFragment;
import com.sendajapan.sendasnap.fragments.ProfileFragment;
import com.sendajapan.sendasnap.networking.NetworkUtils;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.MotionToastHelper;

import me.ibrahimsn.lib.SmoothBottomBar;
import me.ibrahimsn.lib.OnItemSelectedListener;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NetworkUtils networkUtils;
    private HapticFeedbackHelper hapticHelper;
    private boolean isNetworkToastShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initHelpers();
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
                        selectedFragment = new HistoryFragment();
                        break;
                    case 2:
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
