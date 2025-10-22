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
import com.sendajapan.sendasnap.activities.auth.LoginActivity;
import com.sendajapan.sendasnap.adapters.VehicleAdapter;
import com.sendajapan.sendasnap.databinding.FragmentHomeBinding;
import com.sendajapan.sendasnap.models.Vehicle;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.MotionToastHelper;
import com.sendajapan.sendasnap.utils.VehicleCache;

import java.util.List;

import www.sanju.motiontoast.MotionToast;

public class HomeFragment extends Fragment implements VehicleAdapter.OnVehicleClickListener {

    private FragmentHomeBinding binding;
    private VehicleAdapter vehicleAdapter;
    private VehicleCache vehicleCache;
    private HapticFeedbackHelper hapticHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initHelpers();
        setupRecyclerView();
        setupClickListeners();
        loadRecentVehicles();
    }

    private void initHelpers() {
        vehicleCache = VehicleCache.getInstance(requireContext());
        hapticHelper = HapticFeedbackHelper.getInstance(requireContext());
    }

    private void setupRecyclerView() {
        vehicleAdapter = new VehicleAdapter(this);
        binding.recyclerViewVehicles.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewVehicles.setAdapter(vehicleAdapter);
    }

    private void setupClickListeners() {
        binding.btnSearch.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            performSearch();
        });

        binding.btnViewAll.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            // Navigate to history fragment
            // This will be handled by MainActivity
        });

        binding.fabQuickSearch.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            binding.etChassisNumber.requestFocus();
        });
    }

    private void performSearch() {
        String chassisNumber = binding.etChassisNumber.getText().toString().trim();

        // Clear previous errors
        binding.tilChassisNumber.setError(null);

        // Validate input
        if (TextUtils.isEmpty(chassisNumber)) {
            binding.tilChassisNumber.setError("Chassis number is required");
            binding.etChassisNumber.requestFocus();
            hapticHelper.vibrateError();
            return;
        }

        if (chassisNumber.length() < 3) {
            binding.tilChassisNumber.setError("Chassis number must be at least 3 characters");
            binding.etChassisNumber.requestFocus();
            hapticHelper.vibrateError();
            return;
        }

        // Perform search (mock implementation)
        searchVehicle(chassisNumber);
    }

    private void searchVehicle(String chassisNumber) {
        // Show loading state
        binding.btnSearch.setEnabled(false);
        binding.btnSearch.setText("Searching...");

        // Simulate API call delay
        binding.etChassisNumber.postDelayed(() -> {
            // Mock vehicle data
            Vehicle mockVehicle = createMockVehicle(chassisNumber);

            // Add to cache
            vehicleCache.addVehicle(mockVehicle);

            // Update UI
            loadRecentVehicles();

            // Navigate to vehicle details
            Intent intent = new Intent(requireContext(), VehicleDetailsActivity.class);
            intent.putExtra("vehicle", mockVehicle);
            startActivity(intent);

            // Reset search button
            binding.btnSearch.setEnabled(true);
            binding.btnSearch.setText("Search Vehicle");

            // Clear input
            binding.etChassisNumber.setText("");
            // Show success feedback
            MotionToastHelper.showSuccess(requireContext(), "Vehicle Found", "Vehicle details loaded successfully!", MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);

        }, 1500); // 1.5 second delay to simulate network call
    }

    private Vehicle createMockVehicle(String chassisNumber) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId("vehicle_" + System.currentTimeMillis());
        vehicle.setSerialNumber(chassisNumber);
        vehicle.setMake("Toyota");
        vehicle.setModel("Camry");
        vehicle.setChassisModel("XV70");
        vehicle.setCc("2500");
        vehicle.setYear("2020");
        vehicle.setColor("White");
        vehicle.setVehicleBuyDate("2024-01-15");
        vehicle.setAuctionShipNumber("AUC001");
        vehicle.setNetWeight("1500");
        vehicle.setArea("4.5");
        vehicle.setLength("4885");
        vehicle.setWidth("1840");
        vehicle.setHeight("1455");
        vehicle.setPlateNumber("ABC-1234");
        vehicle.setBuyingPrice("25000");
        vehicle.setExpectedYardDate("2024-02-01");
        vehicle.setRiksoFrom("Tokyo");
        vehicle.setRiksoTo("Osaka");
        vehicle.setRiksoCost("500");
        vehicle.setRiksoCompany("Fast Logistics");

        // Mock photos
        java.util.List<String> photos = new java.util.ArrayList<>();
        photos.add("https://example.com/photo1.jpg");
        photos.add("https://example.com/photo2.jpg");
        vehicle.setVehiclePhotos(photos);

        return vehicle;
    }

    private void loadRecentVehicles() {
        List<Vehicle> recentVehicles = vehicleCache.getRecentVehicles(10);

        if (recentVehicles.isEmpty()) {
            binding.recyclerViewVehicles.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewVehicles.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            vehicleAdapter.updateVehicles(recentVehicles);
        }
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
