package com.sendajapan.sendasnap.utils;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.sendajapan.sendasnap.R;

import org.aviran.cookiebar2.CookieBar;


public class CookieBarToastHelper {

    // Duration constants
    public static final int SHORT_DURATION = 2000;
    public static final int LONG_DURATION = 3500;
    public static final int EXTRA_LONG_DURATION = 5000;

    // Position constants
    public static final int GRAVITY_TOP = Gravity.TOP;
    public static final int GRAVITY_BOTTOM = Gravity.BOTTOM;

    /**
     * Show success message with modern green style
     */
    public static void showSuccess(@NonNull Context context, String title, String message) {
        showSuccess(context, title, message, GRAVITY_TOP, SHORT_DURATION);
    }

    public static void showSuccess(@NonNull Context context, String title, String message, int gravity, int duration) {
        if (!(context instanceof Activity)) return;
        
        CookieBar.build((Activity) context)
                .setTitle(title)
                .setMessage(message)
                .setDuration(duration)
                .setBackgroundColor(R.color.success_dark)
                .setIcon(R.drawable.ic_check_circle)
                .setIconAnimation(R.animator.fade_in)
                .setCookiePosition(gravity == GRAVITY_TOP ? CookieBar.TOP : CookieBar.BOTTOM)
                .setSwipeToDismiss(true)
                .setEnableAutoDismiss(true)
                .show();
    }

    /**
     * Show error message with modern red style
     */
    public static void showError(@NonNull Context context, String title, String message) {
        showError(context, title, message, GRAVITY_TOP, SHORT_DURATION);
    }

    public static void showError(@NonNull Context context, String title, String message, int gravity, int duration) {
        if (!(context instanceof Activity)) return;
        
        CookieBar.build((Activity) context)
                .setTitle(title)
                .setMessage(message)
                .setDuration(duration)
                .setBackgroundColor(R.color.error_dark)
                .setIcon(R.drawable.ic_error)
                .setIconAnimation(R.animator.fade_in)
                .setCookiePosition(gravity == GRAVITY_TOP ? CookieBar.TOP : CookieBar.BOTTOM)
                .setSwipeToDismiss(true)
                .setEnableAutoDismiss(true)
                .show();
    }

    /**
     * Show info message with modern blue style
     */
    public static void showInfo(@NonNull Context context, String title, String message) {
        showInfo(context, title, message, GRAVITY_TOP, SHORT_DURATION);
    }

    public static void showInfo(@NonNull Context context, String title, String message, int gravity, int duration) {
        if (!(context instanceof Activity)) return;
        
        CookieBar.build((Activity) context)
                .setTitle(title)
                .setMessage(message)
                .setDuration(duration)
                .setBackgroundColor(R.color.primary)
                .setIcon(R.drawable.ic_info)
                .setIconAnimation(R.animator.fade_in)
                .setCookiePosition(gravity == GRAVITY_TOP ? CookieBar.TOP : CookieBar.BOTTOM)
                .setSwipeToDismiss(true)
                .setEnableAutoDismiss(true)
                .show();
    }

    /**
     * Show warning message with modern orange style
     */
    public static void showWarning(@NonNull Context context, String title, String message) {
        showWarning(context, title, message, GRAVITY_TOP, SHORT_DURATION);
    }

    public static void showWarning(@NonNull Context context, String title, String message, int gravity, int duration) {
        if (!(context instanceof Activity)) return;
        
        CookieBar.build((Activity) context)
                .setTitle(title)
                .setMessage(message)
                .setDuration(duration)
                .setBackgroundColor(R.color.warning_dark)
                .setIcon(R.drawable.ic_warning)
                .setIconAnimation(R.animator.fade_in)
                .setCookiePosition(gravity == GRAVITY_TOP ? CookieBar.TOP : CookieBar.BOTTOM)
                .setSwipeToDismiss(true)
                .setEnableAutoDismiss(true)
                .show();
    }

    /**
     * Show no internet connection message
     */
    public static void showNoInternet(@NonNull Context context) {
        if (!(context instanceof Activity)) return;
        
        CookieBar.build((Activity) context)
                .setTitle("No Internet Connection")
                .setMessage("Please check your network settings and try again")
                .setDuration(LONG_DURATION)
                .setBackgroundColor(R.color.error_dark)
                .setIcon(R.drawable.ic_wifi_off)
                .setIconAnimation(R.animator.fade_in)
                .setCookiePosition(CookieBar.TOP)
                .setSwipeToDismiss(true)
                .setEnableAutoDismiss(true)
                .show();
    }

    /**
     * Modern variant: Show with custom action button
     */
    public static void showWithAction(@NonNull Context context, String title, String message, 
                                      String actionText, Runnable action, int type) {
        if (!(context instanceof Activity)) return;
        
        int backgroundColor = getColorForType(context, type);
        int icon = getIconForType(type);
        
        CookieBar.build((Activity) context)
                .setTitle(title)
                .setMessage(message)
                .setDuration(EXTRA_LONG_DURATION)
                .setBackgroundColor(backgroundColor)
                .setActionColor(R.color.white)
                .setIcon(icon)
                .setIconAnimation(R.animator.fade_in)
                .setAction(actionText, action::run)
                .setCookiePosition(CookieBar.TOP)
                .setSwipeToDismiss(true)
                .setEnableAutoDismiss(true)
                .show();
    }

    /**
     * Modern variant: Show persistent message (must be dismissed manually)
     */
    public static void showPersistent(@NonNull Context context, String title, String message, 
                                      String dismissText, int type) {
        if (!(context instanceof Activity)) return;
        
        int backgroundColor = getColorForType(context, type);
        int icon = getIconForType(type);
        
        CookieBar.build((Activity) context)
                .setTitle(title)
                .setMessage(message)
                .setBackgroundColor(backgroundColor)
                .setActionColor(R.color.white)
                .setIcon(icon)
                .setIconAnimation(R.animator.fade_in)
                .setAction(dismissText, () -> {})
                .setCookiePosition(CookieBar.TOP)
                .setSwipeToDismiss(true)
                .setEnableAutoDismiss(false)
                .show();
    }

    /**
     * Modern variant: Minimal style (no icon, compact)
     */
    public static void showMinimal(@NonNull Context context, String message, int type) {
        if (!(context instanceof Activity)) return;
        
        int backgroundColor = getColorForType(context, type);
        
        CookieBar.build((Activity) context)
                .setMessage(message)
                .setDuration(SHORT_DURATION)
                .setBackgroundColor(backgroundColor)
                .setCookiePosition(CookieBar.BOTTOM)
                .setSwipeToDismiss(true)
                .setEnableAutoDismiss(true)
                .show();
    }

    /**
     * Modern variant: Custom styled message
     */
    public static void showCustom(@NonNull Context context, String title, String message, 
                                   int backgroundColor, int iconRes, int duration, int gravity) {
        if (!(context instanceof Activity)) return;
        
        CookieBar.build((Activity) context)
                .setTitle(title)
                .setMessage(message)
                .setDuration(duration)
                .setBackgroundColor(backgroundColor)
                .setIcon(iconRes)
                .setIconAnimation(R.animator.fade_in)
                .setCookiePosition(gravity == GRAVITY_TOP ? CookieBar.TOP : CookieBar.BOTTOM)
                .setSwipeToDismiss(true)
                .setEnableAutoDismiss(true)
                .show();
    }

    // Type constants
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_ERROR = 1;
    public static final int TYPE_INFO = 2;
    public static final int TYPE_WARNING = 3;

    private static int getColorForType(Context context, int type) {
        switch (type) {
            case TYPE_SUCCESS:
                return ContextCompat.getColor(context, R.color.success_dark);
            case TYPE_ERROR:
                return ContextCompat.getColor(context, R.color.error_dark);
            case TYPE_WARNING:
                return ContextCompat.getColor(context, R.color.warning_dark);
            case TYPE_INFO:
            default:
                return ContextCompat.getColor(context, R.color.primary);
        }
    }

    private static int getIconForType(int type) {
        switch (type) {
            case TYPE_SUCCESS:
                return R.drawable.ic_check_circle;
            case TYPE_ERROR:
                return R.drawable.ic_error;
            case TYPE_WARNING:
                return R.drawable.ic_warning;
            case TYPE_INFO:
            default:
                return R.drawable.ic_info;
        }
    }
}
