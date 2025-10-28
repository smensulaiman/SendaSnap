package com.sendajapan.sendasnap.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.adapters.VehicleAdapter;
import com.sendajapan.sendasnap.databinding.ActivityHistoryBinding;
import com.sendajapan.sendasnap.models.Vehicle;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.MotionToastHelper;
import com.sendajapan.sendasnap.utils.VehicleCache;

import java.util.ArrayList;
import java.util.List;

import www.sanju.motiontoast.MotionToast;

public class HistoryActivity extends AppCompatActivity implements VehicleAdapter.OnVehicleClickListener {

    private ActivityHistoryBinding binding;
    private VehicleAdapter vehicleAdapter;
    private List<Vehicle> allVehicles = new ArrayList<>();
    private List<Vehicle> filteredVehicles = new ArrayList<>();
    private VehicleCache vehicleCache;
    private HapticFeedbackHelper hapticHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set status bar and navigation bar colors
        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_color, getTheme()));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.navigation_bar_color, getTheme()));

        // Ensure light status bar (dark icons)
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
            controller.setAppearanceLightNavigationBars(true);
        }

        initHelpers();
        setupToolbar();
        setupRecyclerView();
        setupSearchFilter();
        setupClearButton();
        loadHistory();
    }

    private void initHelpers() {
        vehicleCache = VehicleCache.getInstance(this);
        hapticHelper = HapticFeedbackHelper.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        binding.toolbar.setNavigationOnClickListener(v -> {
            hapticHelper.vibrateClick();
            finish();
        });
    }

    private void setupRecyclerView() {
        vehicleAdapter = new VehicleAdapter(this);
        binding.recyclerViewHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewHistory.setAdapter(vehicleAdapter);
    }

    private void setupSearchFilter() {
        binding.etSearchFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterVehicles(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupClearButton() {
        binding.btnClearHistory.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            showClearHistoryDialog();
        });
    }

    private void loadHistory() {
        allVehicles.clear();
        allVehicles.addAll(vehicleCache.getAllVehicles());

        filteredVehicles.clear();
        filteredVehicles.addAll(allVehicles);

        vehicleAdapter.updateVehicles(filteredVehicles);
        updateEmptyState();
    }

    private void filterVehicles(String query) {
        filteredVehicles.clear();

        if (query.isEmpty()) {
            filteredVehicles.addAll(allVehicles);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Vehicle vehicle : allVehicles) {
                if (vehicle.getChassisModel().toLowerCase().contains(lowerQuery) ||
                        vehicle.getMake().toLowerCase().contains(lowerQuery) ||
                        vehicle.getModel().toLowerCase().contains(lowerQuery)) {
                    filteredVehicles.add(vehicle);
                }
            }
        }

        vehicleAdapter.updateVehicles(filteredVehicles);
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredVehicles.isEmpty()) {
            binding.layoutEmptyState.setVisibility(android.view.View.VISIBLE);
            binding.recyclerViewHistory.setVisibility(android.view.View.GONE);
        } else {
            binding.layoutEmptyState.setVisibility(android.view.View.GONE);
            binding.recyclerViewHistory.setVisibility(android.view.View.VISIBLE);
        }
    }

    private void showClearHistoryDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Clear History")
                .setMessage("Are you sure you want to clear all search history? This action cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    vehicleCache.clearCache();
                    allVehicles.clear();
                    filteredVehicles.clear();
                    vehicleAdapter.updateVehicles(filteredVehicles);
                    updateEmptyState();

                    MotionToastHelper.showSuccess(this, "History Cleared",
                            "All search history has been removed",
                            MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onVehicleClick(Vehicle vehicle) {
        hapticHelper.vibrateClick();
        // Navigate to VehicleDetailsActivity
        android.content.Intent intent = new android.content.Intent(this, VehicleDetailsActivity.class);
        intent.putExtra("vehicle", vehicle);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
