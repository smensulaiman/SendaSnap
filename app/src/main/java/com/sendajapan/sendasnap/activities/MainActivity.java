package com.sendajapan.sendasnap.activities;

import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

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
import com.sendajapan.sendasnap.utils.CookieBarToastHelper;

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
        setupToolbarMenu();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }
    
    private void setupToolbarMenu() {
        binding.toolbar.setOnMenuItemClickListener(item -> {
            hapticHelper.vibrateClick();

            int itemId = item.getItemId();
            if (itemId == R.id.action_notifications) {
                openNotifications();
                return true;
            } else if (itemId == R.id.action_settings) {
                openSettings();
                return true;
            } else if (itemId == R.id.action_help) {
                openHelp();
                return true;
            } else if (itemId == R.id.action_about) {
                openAbout();
                return true;
            }
            return false;
        });
    }
    
    private void openNotifications() {
        Toast.makeText(this, "coming soon", Toast.LENGTH_SHORT).show();
    }
    
    private void openSettings() {
        // TODO: Implement settings activity
        Toast.makeText(this, "coming soon", Toast.LENGTH_SHORT).show();
    }
    
    private void openHelp() {
        // TODO: Implement help activity
        Toast.makeText(this, "coming soon", Toast.LENGTH_SHORT).show();
    }
    
    private void openAbout() {
        // TODO: Implement about dialog
        Toast.makeText(this, "coming soon", Toast.LENGTH_SHORT).show();
    }

    private void initHelpers() {
        networkUtils = NetworkUtils.getInstance(this);
        hapticHelper = HapticFeedbackHelper.getInstance(this);
    }

    private void setupDrawer() {
        setSupportActionBar(binding.toolbar);
        drawerController = new DrawerController(this,
                binding.drawerLayout,
                binding.navigationView,
                binding.toolbar);
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener((OnItemSelectedListener) pos -> {
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
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();

        updateToolbarTitle(fragment);
    }

    private void updateToolbarTitle(Fragment fragment) {
        if (fragment instanceof HomeFragment) {
            binding.toolbar.setTitle("Home");
        } else if (fragment instanceof ScheduleFragment) {
            binding.toolbar.setTitle("Work Schedule");
        } else if (fragment instanceof ChatFragment) {
            binding.toolbar.setTitle("Chat");
        } else if (fragment instanceof ProfileFragment) {
            binding.toolbar.setTitle("Profile");
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
        CookieBarToastHelper.showNoInternet(this);
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
