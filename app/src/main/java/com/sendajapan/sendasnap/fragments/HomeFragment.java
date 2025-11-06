package com.sendajapan.sendasnap.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.widget.LinearLayout;

import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.activities.VehicleDetailsActivity;
import com.sendajapan.sendasnap.adapters.VehicleAdapter;
import com.sendajapan.sendasnap.databinding.FragmentHomeBinding;
import com.sendajapan.sendasnap.models.Vehicle;
import com.sendajapan.sendasnap.models.VehicleSearchResponse;
import com.sendajapan.sendasnap.networking.ApiService;
import com.sendajapan.sendasnap.networking.NetworkUtils;
import com.sendajapan.sendasnap.networking.RetrofitClient;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.MotionToastHelper;
import com.sendajapan.sendasnap.utils.SharedPrefsManager;
import com.sendajapan.sendasnap.utils.VehicleCache;
import com.sendajapan.sendasnap.utils.LoadingDialog;
import com.sendajapan.sendasnap.utils.VehicleSearchDialog;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import www.sanju.motiontoast.MotionToast;

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
        apiService = RetrofitClient.getInstance().getApiService();
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
            MotionToastHelper.showNoInternet(requireContext());
            hapticHelper.vibrateError();
            return;
        }

        // Show progress dialog
        showLoadingDialog("Searching vehicles...");

        // Perform API call
        Call<VehicleSearchResponse> call = apiService.searchVehicles(searchType, searchQuery);
        call.enqueue(new Callback<VehicleSearchResponse>() {
            @Override
            public void onResponse(Call<VehicleSearchResponse> call, Response<VehicleSearchResponse> response) {
                hideLoadingDialog();
                
                if (response.isSuccessful() && response.body() != null) {
                    VehicleSearchResponse searchResponse = response.body();
                    
                    if (searchResponse.getSuccess() != null && searchResponse.getSuccess()) {
                        VehicleSearchResponse.VehicleSearchData data = searchResponse.getData();
                        if (data != null && data.getVehicles() != null) {
                            List<Vehicle> vehicles = data.getVehicles();
                            
                            if (vehicles.isEmpty()) {
                                MotionToastHelper.showInfo(requireContext(), "No Results", 
                                        "No vehicles found matching your search criteria",
                                        MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);
                            } else if (vehicles.size() == 1) {
                                // Single vehicle - navigate directly to detail page
                                Vehicle vehicle = vehicles.get(0);
                                vehicleCache.addVehicle(vehicle);
                                
                                Intent intent = new Intent(requireContext(), VehicleDetailsActivity.class);
                                intent.putExtra("vehicle", vehicle);
                                startActivity(intent);
                                
                                MotionToastHelper.showSuccess(requireContext(), "Vehicle Found", 
                                        "Vehicle details loaded successfully!",
                                        MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);
                            } else {
                                // Multiple vehicles - show results dialog
                                showSearchResultsDialog(vehicles);
                            }
                        } else {
                            MotionToastHelper.showError(requireContext(), "Error", 
                                    "No vehicle data received",
                                    MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);
                        }
                    } else {
                        String message = searchResponse.getMessage() != null ? 
                                searchResponse.getMessage() : "Search failed";
                        MotionToastHelper.showError(requireContext(), "Error", message,
                                MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);
                    }
                } else {
                    MotionToastHelper.showError(requireContext(), "Error", 
                            "Failed to search vehicles. Please try again.",
                            MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);
                }
            }

            @Override
            public void onFailure(Call<VehicleSearchResponse> call, Throwable t) {
                hideLoadingDialog();
                MotionToastHelper.showError(requireContext(), "Error", 
                        "Failed to connect to server. Please check your internet connection.",
                        MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);
                hapticHelper.vibrateError();
            }
        });
    }

    private void showSearchResultsDialog(List<Vehicle> vehicles) {
        Dialog resultsDialog = new Dialog(requireContext());
        resultsDialog.setContentView(R.layout.dialog_vehicle_search_results);
        if (resultsDialog.getWindow() != null) {
            resultsDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        // Get views
        androidx.recyclerview.widget.RecyclerView recyclerViewResults = 
                resultsDialog.findViewById(R.id.recyclerViewResults);
        LinearLayout emptyState = resultsDialog.findViewById(R.id.layoutEmptyState);
        com.google.android.material.button.MaterialButton btnClose = 
                resultsDialog.findViewById(R.id.btnClose);

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
        // Show shimmer
        showShimmer();
        
        // Simulate loading delay for shimmer effect
        new android.os.Handler().postDelayed(() -> {
            // Check if fragment is still attached and binding is not null
            if (!isAdded() || binding == null) {
                return;
            }
            
            // Create dummy vehicles based on the JSON response structure
            List<Vehicle> dummyVehicles = createDummyVehicles();
            
            // Hide shimmer
            hideShimmer();

            if (dummyVehicles.isEmpty()) {
                binding.recyclerViewVehicles.setVisibility(View.GONE);
                binding.layoutEmptyState.setVisibility(View.VISIBLE);
            } else {
                binding.recyclerViewVehicles.setVisibility(View.VISIBLE);
                binding.layoutEmptyState.setVisibility(View.GONE);
                vehicleAdapter.updateVehicles(dummyVehicles);
            }
        }, 1500); // Simulated loading time
    }
    
    private void showShimmer() {
        if (binding == null) return;
        binding.shimmerVehicles.setVisibility(View.VISIBLE);
        binding.shimmerVehicles.startShimmer();
        binding.recyclerViewVehicles.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.GONE);
    }
    
    private void hideShimmer() {
        if (binding == null) return;
        binding.shimmerVehicles.stopShimmer();
        binding.shimmerVehicles.setVisibility(View.GONE);
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
        hideLoadingDialog();
        binding = null;
    }
}
