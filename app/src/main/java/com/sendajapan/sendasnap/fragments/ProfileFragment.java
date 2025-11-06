package com.sendajapan.sendasnap.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sendajapan.sendasnap.activities.HistoryActivity;
import com.sendajapan.sendasnap.databinding.FragmentProfileBinding;
import com.sendajapan.sendasnap.models.UserData;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.SharedPrefsManager;

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
        });

        binding.switchHaptic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            hapticHelper.vibrateClick();
            prefsManager.setHapticEnabled(isChecked);
        });

        binding.layoutHistory.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            openHistory();
        });
    }

    private void loadUserData() {
        UserData user = prefsManager.getUser();

        if (user != null) {
            if (user.getName() != null && !user.getName().isEmpty()) {
                binding.txtUserName.setText(user.getName());
            } else if (user.getName() != null && !user.getName().isEmpty()) {
                binding.txtUserName.setText(user.getName());
            }

            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                binding.txtUserEmail.setText(user.getEmail());
            }
        } else {
            String username = prefsManager.getUsername();
            String email = prefsManager.getEmail();

            if (username != null && !username.isEmpty()) {
                binding.txtUserName.setText(username);
            }

            if (email != null && !email.isEmpty()) {
                binding.txtUserEmail.setText(email);
            }
        }

        // Load settings
        binding.switchNotifications.setChecked(prefsManager.isNotificationsEnabled());
        binding.switchHaptic.setChecked(prefsManager.isHapticEnabled());
    }

    private void openHistory() {
        Intent intent = new Intent(requireContext(), HistoryActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
