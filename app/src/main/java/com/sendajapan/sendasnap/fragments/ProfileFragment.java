package com.sendajapan.sendasnap.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.activities.auth.LoginActivity;
import com.sendajapan.sendasnap.databinding.FragmentProfileBinding;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.MotionToastHelper;
import com.sendajapan.sendasnap.utils.SharedPrefsManager;

import www.sanju.motiontoast.MotionToast;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SharedPrefsManager prefsManager;
    private HapticFeedbackHelper hapticHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initHelpers();
        setupClickListeners();
        loadUserData();
    }

    private void initHelpers() {
        prefsManager = SharedPrefsManager.getInstance(requireContext());
        hapticHelper = HapticFeedbackHelper.getInstance(requireContext());
    }

    private void setupClickListeners() {
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hapticHelper.vibrateClick();
            prefsManager.setNotificationsEnabled(isChecked);
            if (isChecked) {
                MotionToastHelper.showInfo(requireContext(), "Notifications Enabled",
                        "You will receive push notifications", MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);
            } else {
                MotionToastHelper.showInfo(requireContext(), "Notifications Disabled",
                        "You will not receive push notifications", MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION);
            }
        });

        binding.switchHaptic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hapticHelper.vibrateClick();
            prefsManager.setHapticEnabled(isChecked);
            if (isChecked) {
                MotionToastHelper.showInfo(requireContext(), "Haptic Feedback Enabled",
                        "You will feel vibrations on interactions", MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION);
            } else {
                MotionToastHelper.showInfo(requireContext(), "Haptic Feedback Disabled", "No vibrations will be felt",
                        MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);
            }
        });

        binding.btnClearCache.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            clearCache();
        });

        binding.btnLogout.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            logout();
        });
    }

    private void loadUserData() {
        String username = prefsManager.getUsername();
        String email = prefsManager.getEmail();

        if (!username.isEmpty()) {
            binding.txtUserName.setText(username);
        }

        if (!email.isEmpty()) {
            binding.txtUserEmail.setText(email);
        }

        // Load settings
        binding.switchNotifications.setChecked(prefsManager.isNotificationsEnabled());
        binding.switchHaptic.setChecked(prefsManager.isHapticEnabled());
    }

    private void clearCache() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Clear Cache")
                .setMessage(
                        "Are you sure you want to clear all cached data? This will remove all vehicle search history.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    prefsManager.clearVehicleCache();
                    MotionToastHelper.showSuccess(requireContext(), "Cache Cleared",
                            "All cached data has been removed", MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {
                    prefsManager.logout();
                    MotionToastHelper.showInfo(requireContext(), "Logged Out", "You have been logged out successfully",
                            MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);

                    // Navigate to login activity
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
