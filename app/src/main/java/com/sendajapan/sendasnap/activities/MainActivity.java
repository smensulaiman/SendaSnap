package com.sendajapan.sendasnap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sendajapan.sendasnap.MyApplication;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.databinding.ActivityMainBinding;
import com.sendajapan.sendasnap.fragments.HomeFragment;
import com.sendajapan.sendasnap.fragments.ProfileFragment;
import com.sendajapan.sendasnap.fragments.ScheduleFragment;
import com.sendajapan.sendasnap.networking.NetworkUtils;
import com.sendajapan.sendasnap.utils.CookieBarToastHelper;
import com.sendajapan.sendasnap.utils.DrawerController;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;

import me.ibrahimsn.lib.OnItemSelectedListener;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private HapticFeedbackHelper hapticHelper;
    private NetworkUtils networkUtils;

    private boolean isNetworkToastShowing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.navigationView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, 0, 0, 0);
            return insets;
        });

        MyApplication.applyWindowInsets(binding.getRoot());

        initHelpers();
        setupDrawer();
        setupBottomNavigation();
        setupNetworkMonitoring();
        setupBackPressHandler();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        } else {
            // Ensure menu is updated when activity is recreated
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        // Menu is now handled by HomeFragment
        // Only inflate menu if no fragment is handling it
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment == null || !(currentFragment instanceof HomeFragment)) {
            getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        // Let fragments handle their own menu items first
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment != null && currentFragment.onOptionsItemSelected(item)) {
            return true;
        }
        
        // Fallback to activity handling if fragment didn't handle it
        hapticHelper.vibrateClick();
        int itemId = item.getItemId();
        if (itemId == R.id.action_notifications) {
            openNotifications();
            return true;
        } else if (itemId == R.id.action_about) {
            openAbout();
            return true;
        } else if (itemId == R.id.action_logout) {
            handleLogout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openNotifications() {
        Toast.makeText(this, "Notifications coming soon", Toast.LENGTH_SHORT).show();
    }

    private void handleLogout() {
        // Show confirmation dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Logout");
        builder.setMessage("Are you sure you want to logout?");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton("Logout", (dialog, which) -> {
            hapticHelper.vibrateClick();
            performLogout();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            hapticHelper.vibrateClick();
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            if (positiveButton instanceof MaterialButton) {
                ((MaterialButton) positiveButton).setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                getResources().getColor(R.color.error, null)
                        )
                );
            } else {
                positiveButton.setBackgroundColor(getResources().getColor(R.color.error, null));
            }
            positiveButton.setTextColor(getResources().getColor(R.color.on_primary, null));
            positiveButton.setAllCaps(false);
        }

        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (negativeButton != null) {
            negativeButton.setTextColor(getResources().getColor(R.color.text_secondary, null));
            negativeButton.setAllCaps(false);
        }
    }

    private void performLogout() {
        // Clear user session/preferences
        com.sendajapan.sendasnap.utils.SharedPrefsManager prefsManager =
                com.sendajapan.sendasnap.utils.SharedPrefsManager.getInstance(this);
        prefsManager.logout();

        // Navigate to LoginActivity
        Intent intent = new Intent(this, com.sendajapan.sendasnap.activities.auth.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Finish current activity
        finish();
    }

    private void openSettings() {
        Toast.makeText(this, "coming soon", Toast.LENGTH_SHORT).show();
    }

    private void openHelp() {
        Toast.makeText(this, "coming soon", Toast.LENGTH_SHORT).show();
    }

    private void openAbout() {
        Toast.makeText(this, "coming soon", Toast.LENGTH_SHORT).show();
    }

    private void initHelpers() {
        networkUtils = NetworkUtils.getInstance(this);
        hapticHelper = HapticFeedbackHelper.getInstance(this);
    }

    private void setupDrawer() {
        setSupportActionBar(binding.toolbar);
        // Ensure menu is created after setting support action bar
        supportInvalidateOptionsMenu();
        new DrawerController(this,
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
            // Show menu items for HomeFragment
            invalidateOptionsMenu();
        } else if (fragment instanceof ScheduleFragment) {
            binding.toolbar.setTitle("Work Schedule");
            // Hide menu items for other fragments
            invalidateOptionsMenu();
        } else if (fragment instanceof ProfileFragment) {
            binding.toolbar.setTitle("Profile");
            // Hide menu items for other fragments
            invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(android.view.Menu menu) {
        // Get current fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);

        // HomeFragment now manages its own menu, so hide menu items for other fragments
        boolean showMenu = currentFragment instanceof HomeFragment;

        android.view.MenuItem notificationsItem = menu.findItem(R.id.action_notifications);
        android.view.MenuItem moreItem = menu.findItem(R.id.action_more);

        if (notificationsItem != null) {
            notificationsItem.setVisible(showMenu);
        }
        if (moreItem != null) {
            moreItem.setVisible(showMenu);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    public void switchToProfileTab() {
        binding.bottomNavigation.setItemActiveIndex(2);
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

    private void setupBackPressHandler() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void showExitConfirmationDialog() {
        hapticHelper.vibrateClick();

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Exit App?");
        builder.setMessage("Are you sure you want to exit SendaSnap?");
        builder.setIcon(android.R.drawable.ic_dialog_alert);

        builder.setPositiveButton("Exit", (dialog, which) -> {
            hapticHelper.vibrateClick();
            finishAffinity();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            hapticHelper.vibrateClick();
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        if (positiveButton != null) {
            if (positiveButton instanceof MaterialButton) {
                ((MaterialButton) positiveButton).setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                getResources().getColor(R.color.error, null)
                        )
                );
            } else {
                positiveButton.setBackgroundColor(getResources().getColor(R.color.error, null));
            }
            positiveButton.setTextColor(getResources().getColor(R.color.on_primary, null));
            positiveButton.setAllCaps(false);
        }

        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        if (negativeButton != null) {
            negativeButton.setTextColor(getResources().getColor(R.color.text_secondary, null));
            negativeButton.setAllCaps(false);
        }
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
