package com.sendajapan.sendasnap.ui;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.appbar.MaterialToolbar;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.activities.HistoryActivity;
import com.sendajapan.sendasnap.activities.MainActivity;
import com.sendajapan.sendasnap.activities.auth.LoginActivity;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.SharedPrefsManager;

public class DrawerController {

    private final AppCompatActivity activity;
    private final DrawerLayout drawerLayout;
    private final NavigationView navigationView;
    private MaterialToolbar toolbar;
    private final HapticFeedbackHelper hapticHelper;

    public DrawerController(AppCompatActivity activity, DrawerLayout drawerLayout,
            NavigationView navigationView, MaterialToolbar toolbar) {
        this.activity = activity;
        this.drawerLayout = drawerLayout;
        this.navigationView = navigationView;
        this.toolbar = toolbar;
        this.hapticHelper = HapticFeedbackHelper.getInstance(activity);

        setupDrawer();
    }

    private void setupDrawer() {
        // Only set up if toolbar is not null
        if (toolbar != null) {
            // Set hamburger icon and make it clickable
            toolbar.setNavigationIcon(R.drawable.ic_menu);
            toolbar.setNavigationOnClickListener(v -> {
                hapticHelper.vibrateClick();
                drawerLayout.openDrawer(GravityCompat.START);
            });
        }

        // Setup custom header layout click handlers
        setupCustomDrawerMenu();
    }

    private void setupCustomDrawerMenu() {
        // Inflate custom drawer content and add as body (not header)
        LayoutInflater inflater = LayoutInflater.from(activity);
        View drawerContent = inflater.inflate(R.layout.nav_drawer_header, navigationView, false);
        navigationView.addView(drawerContent);

        // Setup close button
        ImageButton closeButton = drawerContent.findViewById(R.id.buttonCloseDrawer);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> {
                hapticHelper.vibrateClick();
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        }

        // Setup menu item clicks
        setupMenuItemClick(drawerContent, R.id.menu_profile, this::handleProfileNavigation);
        setupMenuItemClick(drawerContent, R.id.menu_settings, this::handleSettingsNavigation);
        setupMenuItemClick(drawerContent, R.id.menu_history, this::handleHistoryNavigation);
        setupMenuItemClick(drawerContent, R.id.menu_logout, this::handleLogoutNavigation);
    }

    private void setupMenuItemClick(View headerView, int menuItemId, Runnable action) {
        View menuItem = headerView.findViewById(menuItemId);
        if (menuItem != null) {
            menuItem.setOnClickListener(v -> {
                hapticHelper.vibrateClick();
                action.run();
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        }
    }

    private void handleProfileNavigation() {
        if (activity instanceof MainActivity) {
            // If we're in MainActivity, switch to Profile tab
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.switchToProfileTab();
        } else {
            // Navigate to MainActivity and switch to Profile tab
            Intent intent = new Intent(activity, MainActivity.class);
            intent.putExtra("destination", "profile");
            activity.startActivity(intent);
        }
    }

    private void handleSettingsNavigation() {
        // TODO: Implement SettingsActivity
        Toast.makeText(activity, "Settings coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void handleHistoryNavigation() {
        if (activity instanceof HistoryActivity) {
            // Already in HistoryActivity, just close drawer
            return;
        }

        Intent intent = new Intent(activity, HistoryActivity.class);
        activity.startActivity(intent);
    }

    private void handleLogoutNavigation() {
        // Clear user session/preferences
        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(activity);
        prefsManager.logout();

        // Navigate to LoginActivity
        Intent intent = new Intent(activity, com.sendajapan.sendasnap.activities.auth.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);

        // Finish current activity
        activity.finish();
    }

    public void setToolbarTitle(String title) {
        toolbar.setTitle(title);
    }

    public void setToolbarLogo(int logoResId) {
        if (toolbar != null) {
            toolbar.setLogo(logoResId);
        }
    }

    public void updateToolbar(MaterialToolbar newToolbar) {
        this.toolbar = newToolbar;
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.ic_menu);
            toolbar.setNavigationOnClickListener(v -> {
                hapticHelper.vibrateClick();
                drawerLayout.openDrawer(GravityCompat.START);
            });
        }
    }
}
