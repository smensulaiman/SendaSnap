package com.sendajapan.sendasnap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.adapters.VehicleAdapter;
import com.sendajapan.sendasnap.databinding.ActivityHistoryBinding;
import com.sendajapan.sendasnap.models.Vehicle;
import com.sendajapan.sendasnap.utils.CookieBarToastHelper;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.VehicleCache;

import java.util.ArrayList;
import java.util.List;

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

        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_color, getTheme()));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.navigation_bar_color, getTheme()));

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
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.recyclerViewHistory.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.recyclerViewHistory.setVisibility(View.VISIBLE);
        }
    }

    private void showClearHistoryDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Clear History")
                .setMessage("Are you sure you want to clear all search history? This action cannot be undone.")
                .setPositiveButton("Clear", (dialog, which) -> {
                    vehicleCache.clearCache();
                    allVehicles.clear();
                    filteredVehicles.clear();
                    vehicleAdapter.updateVehicles(filteredVehicles);
                    updateEmptyState();

                    CookieBarToastHelper.showSuccess(this, "History Cleared",
                            "All search history has been removed",
                            CookieBarToastHelper.LONG_DURATION);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onVehicleClick(Vehicle vehicle) {
        hapticHelper.vibrateClick();
        Intent intent = new Intent(this, VehicleDetailsActivity.class);
        intent.putExtra("vehicle", vehicle);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
