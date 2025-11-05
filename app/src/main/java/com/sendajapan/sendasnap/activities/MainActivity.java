package com.sendajapan.sendasnap.activities;

import android.os.Build;
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
import com.sendajapan.sendasnap.fragments.ChatFragment;
import com.sendajapan.sendasnap.fragments.HomeFragment;
import com.sendajapan.sendasnap.fragments.ProfileFragment;
import com.sendajapan.sendasnap.fragments.ScheduleFragment;
import com.sendajapan.sendasnap.networking.NetworkUtils;
import com.sendajapan.sendasnap.ui.DrawerController;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.MotionToastHelper;

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

        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_color, getTheme()));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.navigation_bar_color, getTheme()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().setBackgroundBlurRadius(30);
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.navigationView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, 0);
            return insets;
        });

        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
        }

        initHelpers();
        setupDrawer();
        setupBottomNavigation();
        setupNetworkMonitoring();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void initHelpers() {
        networkUtils = NetworkUtils.getInstance(this);
        hapticHelper = HapticFeedbackHelper.getInstance(this);
    }

    private void setupDrawer() {
        drawerController = new DrawerController(this,
                binding.drawerLayout,
                binding.navigationView,
                null);
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(new OnItemSelectedListener() {
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

        setupFragmentDrawer(fragment);
    }

    private void setupFragmentDrawer(Fragment fragment) {
        if (fragment instanceof HomeFragment) {
        } else if (fragment instanceof ScheduleFragment) {
        } else if (fragment instanceof ChatFragment) {
        } else if (fragment instanceof ProfileFragment) {
        }
    }

    public void switchToProfileTab() {
        binding.bottomNavigation.setItemActiveIndex(3);
        loadFragment(new ProfileFragment());
    }

    private void setupNetworkMonitoring() {
        networkUtils.startNetworkCallback();

        networkUtils.getIsConnected().observe(this, isConnected -> {
            if (isConnected) {
                if (isNetworkToastShowing) {
                    isNetworkToastShowing = false;
                }
            } else {
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
