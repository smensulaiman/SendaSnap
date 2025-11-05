package com.sendajapan.sendasnap.utils;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.sendajapan.sendasnap.R;

public class LoadingOverlayManager {
    private static LoadingOverlayManager instance;
    private View overlayView;
    private Object currentContext; // Can be Activity or Fragment
    private boolean isShowing = false;

    private LoadingOverlayManager() {
    }

    public static synchronized LoadingOverlayManager getInstance() {
        if (instance == null) {
            instance = new LoadingOverlayManager();
        }
        return instance;
    }

    /**
     * Show loading overlay on the specified activity
     * @param activity The activity to show the overlay on
     * @param message Optional custom message. If null, uses default "Please wait..."
     */
    public void showLoading(Activity activity, @Nullable String message) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        // Hide existing overlay if showing
        if (isShowing && overlayView != null) {
            hideLoading();
        }

        currentContext = activity;
        
        // Get root view
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        if (rootView == null) {
            return;
        }

        // Inflate overlay
        LayoutInflater inflater = LayoutInflater.from(activity);
        overlayView = inflater.inflate(R.layout.overlay_loading, rootView, false);

        // Set custom message if provided
        if (message != null && !message.isEmpty()) {
            TextView txtMessage = overlayView.findViewById(R.id.txtLoadingMessage);
            if (txtMessage != null) {
                txtMessage.setText(message);
            }
        }

        // Add overlay to root view
        rootView.addView(overlayView);
        isShowing = true;

        // Start animation
        LottieAnimationView lottieView = overlayView.findViewById(R.id.lottieAnimation);
        if (lottieView != null) {
            lottieView.playAnimation();
        }
    }

    /**
     * Show loading overlay on the specified fragment
     * @param fragment The fragment to show the overlay on
     * @param message Optional custom message. If null, uses default "Please wait..."
     */
    public void showLoading(Fragment fragment, @Nullable String message) {
        if (fragment == null || fragment.getActivity() == null || fragment.isDetached()) {
            return;
        }

        Activity activity = fragment.getActivity();
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        // Hide existing overlay if showing
        if (isShowing && overlayView != null) {
            hideLoading();
        }

        currentContext = fragment;
        
        // Get root view from activity
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        if (rootView == null) {
            return;
        }

        // Inflate overlay
        LayoutInflater inflater = LayoutInflater.from(activity);
        overlayView = inflater.inflate(R.layout.overlay_loading, rootView, false);

        // Set custom message if provided
        if (message != null && !message.isEmpty()) {
            TextView txtMessage = overlayView.findViewById(R.id.txtLoadingMessage);
            if (txtMessage != null) {
                txtMessage.setText(message);
            }
        }

        // Add overlay to root view
        rootView.addView(overlayView);
        isShowing = true;

        // Start animation
        LottieAnimationView lottieView = overlayView.findViewById(R.id.lottieAnimation);
        if (lottieView != null) {
            lottieView.playAnimation();
        }
    }

    /**
     * Show loading overlay with default message
     */
    public void showLoading(Activity activity) {
        showLoading(activity, null);
    }

    /**
     * Show loading overlay with default message (for Fragment)
     */
    public void showLoading(Fragment fragment) {
        showLoading(fragment, null);
    }

    /**
     * Hide loading overlay
     */
    public void hideLoading() {
        if (!isShowing || overlayView == null) {
            return;
        }

        // Stop animation
        LottieAnimationView lottieView = overlayView.findViewById(R.id.lottieAnimation);
        if (lottieView != null) {
            lottieView.cancelAnimation();
        }

        // Remove overlay from parent
        ViewGroup parent = (ViewGroup) overlayView.getParent();
        if (parent != null) {
            parent.removeView(overlayView);
        }

        overlayView = null;
        currentContext = null;
        isShowing = false;
    }

    /**
     * Check if loading overlay is currently showing
     */
    public boolean isShowing() {
        return isShowing;
    }

    /**
     * Update the loading message
     */
    public void updateMessage(String message) {
        if (overlayView != null && message != null) {
            TextView txtMessage = overlayView.findViewById(R.id.txtLoadingMessage);
            if (txtMessage != null) {
                txtMessage.setText(message);
            }
        }
    }
}

