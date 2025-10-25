package com.sendajapan.sendasnap.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.sendajapan.sendasnap.ui.DrawerController;
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
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        setupFAB();
        loadRecentVehicles();
    }

    private void initHelpers() {
        vehicleCache = VehicleCache.getInstance(requireContext());
        hapticHelper = HapticFeedbackHelper.getInstance(requireContext());
    }

    private void setupToolbar() {
        // Set up toolbar with drawer
        if (getActivity() instanceof com.sendajapan.sendasnap.activities.MainActivity) {
            com.sendajapan.sendasnap.activities.MainActivity mainActivity = (com.sendajapan.sendasnap.activities.MainActivity) getActivity();

            // Set title only
            binding.toolbar.setTitle("Home");

            // Update drawer controller with this fragment's toolbar
            if (mainActivity.drawerController != null) {
                mainActivity.drawerController.updateToolbar(binding.toolbar);
            }
        }
    }

    private void setupRecyclerView() {
        vehicleAdapter = new VehicleAdapter(this);
        binding.recyclerViewVehicles.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewVehicles.setAdapter(vehicleAdapter);
    }

    private void setupClickListeners() {
        binding.btnViewAll.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            // Navigate to history fragment
            // This will be handled by MainActivity
        });

        binding.btnQuickSearch.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            showSearchDialog();
        });

        binding.btnViewStats.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            // TODO: Navigate to stats/analytics screen
            Toast.makeText(getContext(), "Stats feature coming soon!", Toast.LENGTH_SHORT).show();
        });

    }

    private void setupFAB() {
        binding.fabSearch.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            showSearchDialog();
        });
    }

    private void showSearchDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_vehicle_search);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        // Get views from dialog
        com.google.android.material.textfield.TextInputLayout tilChassisNumber = dialog
                .findViewById(R.id.tilChassisNumber);
        com.google.android.material.textfield.TextInputEditText etChassisNumber = dialog
                .findViewById(R.id.etChassisNumber);
        com.google.android.material.button.MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        com.google.android.material.button.MaterialButton btnSearch = dialog.findViewById(R.id.btnSearch);

        // Setup click listeners
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSearch.setOnClickListener(v -> {
            String chassisNumber = etChassisNumber.getText().toString().trim();

            // Clear previous errors
            tilChassisNumber.setError(null);

            // Validate input
            if (TextUtils.isEmpty(chassisNumber)) {
                tilChassisNumber.setError("Chassis number is required");
                etChassisNumber.requestFocus();
                hapticHelper.vibrateError();
                return;
            }

            if (chassisNumber.length() < 3) {
                tilChassisNumber.setError("Chassis number must be at least 3 characters");
                etChassisNumber.requestFocus();
                hapticHelper.vibrateError();
                return;
            }

            // Close dialog and perform search
            dialog.dismiss();
            performSearchFromDialog(chassisNumber);
        });

        dialog.show();
    }

    private void performSearchFromDialog(String chassisNumber) {
        // Perform search (mock implementation)
        searchVehicle(chassisNumber);
    }

    private void searchVehicle(String chassisNumber) {
        // Simulate API call delay
        binding.recyclerViewVehicles.postDelayed(() -> {
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

            // Show success feedback
            MotionToastHelper.showSuccess(requireContext(), "Vehicle Found", "Vehicle details loaded successfully!",
                    MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);

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
        // Create dummy vehicles based on the JSON response structure
        List<Vehicle> dummyVehicles = createDummyVehicles();

        if (dummyVehicles.isEmpty()) {
            binding.recyclerViewVehicles.setVisibility(View.GONE);
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewVehicles.setVisibility(View.VISIBLE);
            binding.layoutEmptyState.setVisibility(View.GONE);
            vehicleAdapter.updateVehicles(dummyVehicles);
        }
    }

    private List<Vehicle> createDummyVehicles() {
        List<Vehicle> vehicles = new java.util.ArrayList<>();

        // Vehicle 1 - Toyota Corolla Axio (from your JSON)
        Vehicle vehicle1 = new Vehicle();
        vehicle1.setId("251144");
        vehicle1.setSerialNumber("7250165");
        vehicle1.setMake("TOYOTA");
        vehicle1.setModel("COROLLA AXIO");
        vehicle1.setChassisModel("NKE165");
        vehicle1.setCc("1490");
        vehicle1.setYear("2021");
        vehicle1.setColor("SILVER");
        vehicle1.setVehicleBuyDate("2025-10-09");
        vehicle1.setAuctionShipNumber("2204");
        vehicle1.setNetWeight("1140");
        vehicle1.setLength("440");
        vehicle1.setHeight("146");
        vehicle1.setWidth("169");
        vehicle1.setArea("名古屋");
        vehicle1.setBuyingPrice("1292000");
        vehicle1.setRiksoCost("5000");
        vehicle1.setRiksoCompany("EIKO SHOUN");
        vehicle1.setVehiclePhotos(java.util.Arrays.asList("img_01760044939.png", "img_11760044941.png"));
        vehicles.add(vehicle1);

        // Vehicle 2 - Honda Civic
        Vehicle vehicle2 = new Vehicle();
        vehicle2.setId("251145");
        vehicle2.setSerialNumber("7250166");
        vehicle2.setMake("HONDA");
        vehicle2.setModel("CIVIC");
        vehicle2.setChassisModel("FK7");
        vehicle2.setCc("1500");
        vehicle2.setYear("2020");
        vehicle2.setColor("WHITE");
        vehicle2.setVehicleBuyDate("2025-10-08");
        vehicle2.setAuctionShipNumber("2205");
        vehicle2.setNetWeight("1200");
        vehicle2.setLength("450");
        vehicle2.setHeight("145");
        vehicle2.setWidth("170");
        vehicle2.setArea("東京");
        vehicle2.setBuyingPrice("1350000");
        vehicle2.setRiksoCost("6000");
        vehicle2.setRiksoCompany("TOKYO TRANSPORT");
        vehicle2.setVehiclePhotos(java.util.Arrays.asList("img_01760044940.png", "img_11760044942.png"));
        vehicles.add(vehicle2);

        // Vehicle 3 - Nissan Skyline
        Vehicle vehicle3 = new Vehicle();
        vehicle3.setId("251146");
        vehicle3.setSerialNumber("7250167");
        vehicle3.setMake("NISSAN");
        vehicle3.setModel("SKYLINE");
        vehicle3.setChassisModel("V36");
        vehicle3.setCc("3500");
        vehicle3.setYear("2019");
        vehicle3.setColor("BLACK");
        vehicle3.setVehicleBuyDate("2025-10-07");
        vehicle3.setAuctionShipNumber("2206");
        vehicle3.setNetWeight("1600");
        vehicle3.setLength("480");
        vehicle3.setHeight("150");
        vehicle3.setWidth("180");
        vehicle3.setArea("大阪");
        vehicle3.setBuyingPrice("2500000");
        vehicle3.setRiksoCost("8000");
        vehicle3.setRiksoCompany("OSAKA LOGISTICS");
        vehicle3.setVehiclePhotos(java.util.Arrays.asList("img_01760044943.png", "img_11760044944.png"));
        vehicles.add(vehicle3);

        // Vehicle 4 - Mazda CX-5
        Vehicle vehicle4 = new Vehicle();
        vehicle4.setId("251147");
        vehicle4.setSerialNumber("7250168");
        vehicle4.setMake("MAZDA");
        vehicle4.setModel("CX-5");
        vehicle4.setChassisModel("KF");
        vehicle4.setCc("2000");
        vehicle4.setYear("2022");
        vehicle4.setColor("RED");
        vehicle4.setVehicleBuyDate("2025-10-06");
        vehicle4.setAuctionShipNumber("2207");
        vehicle4.setNetWeight("1500");
        vehicle4.setLength("460");
        vehicle4.setHeight("170");
        vehicle4.setWidth("185");
        vehicle4.setArea("横浜");
        vehicle4.setBuyingPrice("1800000");
        vehicle4.setRiksoCost("7000");
        vehicle4.setRiksoCompany("YOKOHAMA SHIPPING");
        vehicle4.setVehiclePhotos(java.util.Arrays.asList("img_01760044945.png", "img_11760044946.png"));
        vehicles.add(vehicle4);

        // Vehicle 5 - Subaru Impreza
        Vehicle vehicle5 = new Vehicle();
        vehicle5.setId("251148");
        vehicle5.setSerialNumber("7250169");
        vehicle5.setMake("SUBARU");
        vehicle5.setModel("IMPREZA");
        vehicle5.setChassisModel("GJ");
        vehicle5.setCc("1600");
        vehicle5.setYear("2021");
        vehicle5.setColor("BLUE");
        vehicle5.setVehicleBuyDate("2025-10-05");
        vehicle5.setAuctionShipNumber("2208");
        vehicle5.setNetWeight("1400");
        vehicle5.setLength("450");
        vehicle5.setHeight("150");
        vehicle5.setWidth("175");
        vehicle5.setArea("福岡");
        vehicle5.setBuyingPrice("1400000");
        vehicle5.setRiksoCost("5500");
        vehicle5.setRiksoCompany("FUKUOKA TRANSPORT");
        vehicle5.setVehiclePhotos(java.util.Arrays.asList("img_01760044947.png", "img_11760044948.png"));
        vehicles.add(vehicle5);

        return vehicles;
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
