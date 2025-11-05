package com.sendajapan.sendasnap.utils;

import android.app.Activity;
import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Usage Examples for LoadingOverlayManager
 * 
 * This class demonstrates how to use LoadingOverlayManager in different scenarios.
 * 
 * EXAMPLE 1: Using in an Activity with API call
 * 
 * LoadingOverlayManager loadingManager = LoadingOverlayManager.getInstance();
 * 
 * // Show loading before API call
 * loadingManager.showLoading(this, "Loading vehicles...");
 * 
 * // Make API call
 * Call<List<Vehicle>> call = apiService.getRecentVehicles();
 * call.enqueue(new Callback<List<Vehicle>>() {
 *     @Override
 *     public void onResponse(Call<List<Vehicle>> call, Response<List<Vehicle>> response) {
 *         // Hide loading on success
 *         loadingManager.hideLoading();
 *         
 *         if (response.isSuccessful()) {
 *             // Handle success
 *         }
 *     }
 * 
 *     @Override
 *     public void onFailure(Call<List<Vehicle>> call, Throwable t) {
 *         // Hide loading on error
 *         loadingManager.hideLoading();
 *         // Handle error
 *     }
 * });
 * 
 * 
 * EXAMPLE 2: Using in a Fragment
 * 
 * LoadingOverlayManager loadingManager = LoadingOverlayManager.getInstance();
 * 
 * // Show loading
 * loadingManager.showLoading(this, "Please wait...");
 * 
 * // Perform operation
 * performOperation();
 * 
 * // Hide loading when done
 * loadingManager.hideLoading();
 * 
 * 
 * EXAMPLE 3: With custom message
 * 
 * loadingManager.showLoading(activity, "Uploading images...");
 * 
 * 
 * EXAMPLE 4: Check if already showing
 * 
 * if (!loadingManager.isShowing()) {
 *     loadingManager.showLoading(activity);
 * }
 * 
 * 
 * EXAMPLE 5: Update message while loading
 * 
 * loadingManager.showLoading(activity, "Starting...");
 * // ... some operation ...
 * loadingManager.updateMessage("Almost done...");
 * 
 */
public class LoadingOverlayManagerUsageExample {
    // This is a documentation-only class
    // Delete this file after reading the examples above
}

