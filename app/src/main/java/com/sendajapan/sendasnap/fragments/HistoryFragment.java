package com.sendajapan.sendasnap.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.activities.VehicleDetailsActivity;
import com.sendajapan.sendasnap.adapters.VehicleAdapter;
import com.sendajapan.sendasnap.databinding.FragmentHistoryBinding;
import com.sendajapan.sendasnap.models.Vehicle;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.MotionToastHelper;
import com.sendajapan.sendasnap.utils.VehicleCache;

import www.sanju.motiontoast.MotionToast;

import java.util.List;

public class HistoryFragment extends Fragment implements VehicleAdapter.OnVehicleClickListener {

    private FragmentHistoryBinding binding;
    private VehicleAdapter vehicleAdapter;
    private VehicleCache vehicleCache;
    private HapticFeedbackHelper hapticHelper;
    private List<Vehicle> allVehicles;
    private List<Vehicle> filteredVehicles;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initHelpers();
        setupRecyclerView();
        setupClickListeners();
        loadHistory();
    }

    private void initHelpers() {
        vehicleCache = VehicleCache.getInstance(requireContext());
        hapticHelper = HapticFeedbackHelper.getInstance(requireContext());
    }

    private void setupRecyclerView() {
        vehicleAdapter = new VehicleAdapter(this);
        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewHistory.setAdapter(vehicleAdapter);
    }

    private void setupClickListeners() {
        binding.btnClearHistory.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            clearHistory();
        });

        // Add text change listener for search filter
        binding.etSearchFilter.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterVehicles(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    private void loadHistory() {
        allVehicles = vehicleCache.getAllVehicles();
        filteredVehicles = allVehicles;
        updateUI();
    }

    private void filterVehicles(String query) {
        if (TextUtils.isEmpty(query)) {
            filteredVehicles = allVehicles;
        } else {
            filteredVehicles = vehicleCache.searchVehicles(query);
        }
        updateUI();
    }

    private void updateUI() {
        if (filteredVehicles.isEmpty()) {
            binding.recyclerViewHistory.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewHistory.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            vehicleAdapter.updateVehicles(filteredVehicles);
        }
    }

    private void clearHistory() {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Clear History")
                .setMessage("Are you sure you want to clear all search history? This action cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    vehicleCache.clearCache();
                    loadHistory();
                    MotionToastHelper.showDelete(requireContext(), "History Cleared",
                            "All search history has been removed", MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onVehicleClick(Vehicle vehicle) {
        hapticHelper.vibrateClick();

        Intent intent = new Intent(requireContext(), VehicleDetailsActivity.class);
        intent.putExtra("vehicle", vehicle);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
