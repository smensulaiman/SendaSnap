package com.sendajapan.sendasnap.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.LinearLayout;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.activities.VehicleDetailsActivity;
import com.sendajapan.sendasnap.adapters.VehicleAdapter;
import com.sendajapan.sendasnap.databinding.FragmentHomeBinding;
import com.sendajapan.sendasnap.models.ErrorResponse;
import com.sendajapan.sendasnap.models.Vehicle;
import com.sendajapan.sendasnap.models.VehicleSearchResponse;
import com.sendajapan.sendasnap.networking.ApiService;
import com.sendajapan.sendasnap.networking.NetworkUtils;
import com.sendajapan.sendasnap.networking.RetrofitClient;
import com.sendajapan.sendasnap.utils.CookieBarToastHelper;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.SharedPrefsManager;
import com.sendajapan.sendasnap.utils.VehicleCache;
import com.sendajapan.sendasnap.utils.LoadingDialog;
import com.sendajapan.sendasnap.utils.VehicleSearchDialog;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements VehicleAdapter.OnVehicleClickListener {

    private FragmentHomeBinding binding;
    private VehicleAdapter vehicleAdapter;
    private VehicleCache vehicleCache;
    private HapticFeedbackHelper hapticHelper;
    private ApiService apiService;
    private NetworkUtils networkUtils;
    private LoadingDialog loadingDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initHelpers();

        binding.txtWelcome.setText("Welcome, " + SharedPrefsManager.getInstance(getContext()).getUsername() + " san!");

        setupRecyclerView();
        setupClickListeners();
        setupFAB();
        loadRecentVehicles();
    }

    private void initHelpers() {
        vehicleCache = VehicleCache.getInstance(requireContext());
        hapticHelper = HapticFeedbackHelper.getInstance(requireContext());
        apiService = RetrofitClient.getInstance(requireContext()).getApiService();
        networkUtils = NetworkUtils.getInstance(requireContext());
    }

    private void setupRecyclerView() {
        vehicleAdapter = new VehicleAdapter(this);
        binding.recyclerViewVehicles.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewVehicles.setAdapter(vehicleAdapter);
    }

    private void setupClickListeners() {
        binding.btnViewAll.setOnClickListener(v -> {
            hapticHelper.vibrateClick();

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
        new VehicleSearchDialog.Builder(requireContext())
                .setOnSearchListener(this::performSearchFromDialog)
                .setCancelable(true)
                .show();
    }

    private void showLoadingDialog(String message) {
        hideLoadingDialog();

        loadingDialog = new LoadingDialog.Builder(requireContext())
                .setMessage(message)
                .setSubtitle("Please wait while we fetch the data")
                .setCancelable(false)
                .setShowProgressIndicator(false)
                .show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
            loadingDialog = null;
        }
    }

    private void performSearchFromDialog(String searchType, String searchQuery) {
        // Check internet connection
        if (!networkUtils.isNetworkAvailable()) {
            CookieBarToastHelper.showNoInternet(requireContext());
            hapticHelper.vibrateError();
            return;
        }

        // Show progress dialog
        showLoadingDialog("Searching vehicles...");

        // Perform API call
        Call<VehicleSearchResponse> call = apiService.searchVehicles(searchType, searchQuery);
        call.enqueue(new Callback<VehicleSearchResponse>() {
            @Override
            public void onResponse(@NonNull Call<VehicleSearchResponse> call,
                    @NonNull Response<VehicleSearchResponse> response) {
                hideLoadingDialog();

                if (response.isSuccessful() && response.body() != null) {
                    VehicleSearchResponse searchResponse = response.body();

                    if (searchResponse.getSuccess() != null && searchResponse.getSuccess()) {
                        VehicleSearchResponse.VehicleSearchData data = searchResponse.getData();
                        if (data != null && data.getVehicles() != null) {
                            List<Vehicle> vehicles = data.getVehicles();

                            if (vehicles.isEmpty()) {
                                CookieBarToastHelper.showInfo(requireContext(), "No Results",
                                        "No vehicles found matching your search criteria",
                                        CookieBarToastHelper.LONG_DURATION);
                            } else if (vehicles.size() == 1) {
                                // Single vehicle - navigate directly to detail page
                                Vehicle vehicle = vehicles.get(0);
                                vehicleCache.addVehicle(vehicle);

                                Intent intent = new Intent(requireContext(), VehicleDetailsActivity.class);
                                intent.putExtra("vehicle", vehicle);
                                startActivity(intent);

                                CookieBarToastHelper.showSuccess(requireContext(), "Vehicle Found",
                                        "Vehicle details loaded successfully!",
                                        CookieBarToastHelper.LONG_DURATION);
                            } else {
                                // Multiple vehicles - show results dialog
                                showSearchResultsDialog(vehicles);
                            }
                        } else {
                            CookieBarToastHelper.showError(requireContext(), "Error",
                                    "No vehicle data received",
                                    CookieBarToastHelper.LONG_DURATION);
                        }
                    } else {
                        String message = searchResponse.getMessage() != null ? searchResponse.getMessage()
                                : "Search failed";
                        CookieBarToastHelper.showError(requireContext(), "Error", message,
                                CookieBarToastHelper.LONG_DURATION);
                    }
                } else {
                    String errorMessage = "Failed to search vehicles. Please try again.";
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyStr = response.errorBody().string();
                            Gson gson = new com.google.gson.Gson();
                            ErrorResponse errorResponse = gson.fromJson(errorBodyStr,
                                    com.sendajapan.sendasnap.models.ErrorResponse.class);
                            if (errorResponse != null && errorResponse.getMessage() != null) {
                                errorMessage = errorResponse.getMessage();
                            }
                        } catch (Exception e) {
                            // Optionally log or handle parsing exception
                        }
                    }
                    CookieBarToastHelper.showError(requireContext(), "Error",
                            errorMessage,
                            CookieBarToastHelper.LONG_DURATION);
                }
            }

            @Override
            public void onFailure(@NonNull Call<VehicleSearchResponse> call, @NonNull Throwable t) {
                hideLoadingDialog();
                CookieBarToastHelper.showError(requireContext(), "Error",
                        "Failed to connect to server. Please check your internet connection.",
                        CookieBarToastHelper.LONG_DURATION);
                hapticHelper.vibrateError();
            }
        });
    }

    private void showSearchResultsDialog(List<Vehicle> vehicles) {

        Dialog resultsDialog = new Dialog(requireContext());
        resultsDialog.setContentView(R.layout.dialog_vehicle_search_results);
        if (resultsDialog.getWindow() != null) {
            resultsDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Get views
        RecyclerView recyclerViewResults = resultsDialog.findViewById(R.id.recyclerViewResults);
        LinearLayout emptyState = resultsDialog.findViewById(R.id.layoutEmptyState);
        MaterialButton btnClose = resultsDialog.findViewById(R.id.btnClose);

        // Setup RecyclerView
        VehicleAdapter resultsAdapter = new VehicleAdapter(vehicle -> {
            hapticHelper.vibrateClick();
            resultsDialog.dismiss();

            // Add to cache
            vehicleCache.addVehicle(vehicle);

            // Navigate to detail page
            Intent intent = new Intent(requireContext(), VehicleDetailsActivity.class);
            intent.putExtra("vehicle", vehicle);
            startActivity(intent);
        });

        recyclerViewResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewResults.setAdapter(resultsAdapter);
        resultsAdapter.updateVehicles(vehicles);

        // Show empty state if no vehicles
        if (vehicles.isEmpty()) {
            recyclerViewResults.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerViewResults.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }

        btnClose.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            resultsDialog.dismiss();
        });

        resultsDialog.show();
    }

    private void loadRecentVehicles() {
        showShimmer();

        new android.os.Handler().postDelayed(() -> {
            if (!isAdded() || binding == null) {
                return;
            }

            List<Vehicle> dummyVehicles = SharedPrefsManager.getInstance(requireContext()).getRecentVehicles();

            hideShimmer();

            if (dummyVehicles.isEmpty()) {
                binding.recyclerViewVehicles.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerViewVehicles.setVisibility(View.VISIBLE);
                binding.layoutEmptyState.setVisibility(View.GONE);
                vehicleAdapter.updateVehicles(dummyVehicles);
            }
        }, 700);
    }

    private void showShimmer() {
        if (binding == null)
            return;
        binding.shimmerVehicles.setVisibility(View.VISIBLE);
        binding.shimmerVehicles.startShimmer();
        binding.recyclerViewVehicles.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
    }

    private void hideShimmer() {
        if (binding == null)
            return;
        binding.shimmerVehicles.stopShimmer();
        binding.shimmerVehicles.setVisibility(View.GONE);
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
        hideLoadingDialog();
        binding = null;
    }
}
