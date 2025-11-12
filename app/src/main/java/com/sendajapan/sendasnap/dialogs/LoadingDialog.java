package com.sendajapan.sendasnap.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.sendajapan.sendasnap.R;


public class LoadingDialog {
    
    private final Dialog dialog;
    private final TextView txtMessage;
    private final TextView txtSubtitle;
    private final LinearProgressIndicator progressIndicator;
    private final LottieAnimationView lottieAnimation;
    
    private LoadingDialog(Builder builder) {
        dialog = new Dialog(builder.context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCancelable(builder.cancelable);
        
        // Set transparent background for rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        // Initialize views
        txtMessage = dialog.findViewById(R.id.txtLoadingMessage);
        txtSubtitle = dialog.findViewById(R.id.txtLoadingSubtitle);
        progressIndicator = dialog.findViewById(R.id.progressIndicator);
        lottieAnimation = dialog.findViewById(R.id.lottieAnimation);
        
        // Apply builder settings
        if (builder.message != null) {
            txtMessage.setText(builder.message);
        }
        
        if (builder.subtitle != null) {
            txtSubtitle.setText(builder.subtitle);
            txtSubtitle.setVisibility(View.VISIBLE);
        }
        
        if (builder.showProgressIndicator) {
            progressIndicator.setVisibility(View.VISIBLE);
        }
        
        if (builder.onCancelListener != null) {
            dialog.setOnCancelListener(builder.onCancelListener);
        }
        
        if (builder.onDismissListener != null) {
            dialog.setOnDismissListener(builder.onDismissListener);
        }
    }
    
    /**
     * Show the loading dialog
     */
    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }
    
    /**
     * Dismiss the loading dialog
     */
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
    
    /**
     * Check if dialog is showing
     */
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
    
    /**
     * Update the loading message
     */
    public void setMessage(String message) {
        if (txtMessage != null && message != null) {
            txtMessage.setText(message);
        }
    }
    
    /**
     * Update the subtitle
     */
    public void setSubtitle(String subtitle) {
        if (txtSubtitle != null && subtitle != null) {
            txtSubtitle.setText(subtitle);
            txtSubtitle.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Get the underlying Dialog object
     */
    public Dialog getDialog() {
        return dialog;
    }
    
    /**
     * Builder class for LoadingDialog
     */
    public static class Builder {
        private final Context context;
        private String message;
        private String subtitle;
        private boolean cancelable = false;
        private boolean showProgressIndicator = false;
        private android.content.DialogInterface.OnCancelListener onCancelListener;
        private android.content.DialogInterface.OnDismissListener onDismissListener;
        
        public Builder(@NonNull Context context) {
            this.context = context;
        }
        
        /**
         * Set the loading message
         */
        public Builder setMessage(@Nullable String message) {
            this.message = message;
            return this;
        }
        
        /**
         * Set the subtitle text (optional)
         */
        public Builder setSubtitle(@Nullable String subtitle) {
            this.subtitle = subtitle;
            return this;
        }
        
        /**
         * Set whether the dialog is cancelable
         */
        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }
        
        /**
         * Show linear progress indicator below the message
         */
        public Builder setShowProgressIndicator(boolean show) {
            this.showProgressIndicator = show;
            return this;
        }
        
        /**
         * Set cancel listener
         */
        public Builder setOnCancelListener(@Nullable android.content.DialogInterface.OnCancelListener listener) {
            this.onCancelListener = listener;
            return this;
        }
        
        /**
         * Set dismiss listener
         */
        public Builder setOnDismissListener(@Nullable android.content.DialogInterface.OnDismissListener listener) {
            this.onDismissListener = listener;
            return this;
        }
        
        /**
         * Build the LoadingDialog
         */
        public LoadingDialog build() {
            return new LoadingDialog(this);
        }
        
        /**
         * Build and show the dialog immediately
         */
        public LoadingDialog show() {
            LoadingDialog dialog = build();
            dialog.show();
            return dialog;
        }
    }
}

