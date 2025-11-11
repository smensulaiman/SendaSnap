package com.sendajapan.sendasnap.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.view.Gravity;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

import com.sendajapan.sendasnap.R;

import org.aviran.cookiebar2.CookieBar;
import org.aviran.cookiebar2.CookieBarDismissListener;


public class CookieBarToastHelper {

    // Duration constants
    public static final long SHORT_DURATION = 2000L;
    public static final long LONG_DURATION = 3500L;
    public static final long EXTRA_LONG_DURATION = 5000L;

    /**
     * Get Activity from Context
     */
    private static Activity getActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    /**
     * Helper method to build CookieBar
     */
    private static CookieBar.Builder buildCookie(@NonNull Context context, String title, String message,
                                                  @ColorRes int backgroundColorRes, int iconRes,
                                                  long duration) {
        Activity activity = getActivity(context);
        if (activity == null) {
            throw new IllegalArgumentException("Context must be an Activity or contain an Activity");
        }
        
        return CookieBar.build(activity)
                .setTitle(title)
                .setMessage(message)
                .setDuration(duration)
                .setBackgroundColor(backgroundColorRes)
                .setIcon(iconRes)
                .setIconAnimation(R.animator.fade_in)
                .setCookiePosition(Gravity.TOP)
                .setSwipeToDismiss(true)
                .setEnableAutoDismiss(true);
    }

    private static CookieBar.Builder buildCookieWithListener(@NonNull Context context, String title, String message,
                                                 @ColorRes int backgroundColorRes, int iconRes,
                                                 long duration, CookieBarDismissListener cookieBarDismissListener) {
        Activity activity = getActivity(context);
        if (activity == null) {
            throw new IllegalArgumentException("Context must be an Activity or contain an Activity");
        }

        return CookieBar.build(activity)
                .setTitle(title)
                .setMessage(message)
                .setDuration(duration)
                .setBackgroundColor(backgroundColorRes)
                .setIcon(iconRes)
                .setIconAnimation(R.animator.fade_in)
                .setCookiePosition(Gravity.TOP)
                .setSwipeToDismiss(true)
                .setEnableAutoDismiss(true)
                .setCookieListener(cookieBarDismissListener);
    }

    /**
     * Show success message
     */
    public static void showSuccess(@NonNull Context context, String title, String message) {
        showSuccess(context, title, message, SHORT_DURATION);
    }

    public static void showSuccess(@NonNull Context context, String title, String message, long duration) {
        buildCookie(context, title, message, R.color.success_dark, R.drawable.ic_check_circle, duration).show();
    }

    public static void showSuccessWithListener(@NonNull Context context, String title, String message,
                                               CookieBarDismissListener cookieBarDismissListener) {
        showSuccessWithListener(context, title, message, SHORT_DURATION, cookieBarDismissListener);
    }

    public static void showSuccessWithListener(@NonNull Context context, String title, String message, long duration, CookieBarDismissListener cookieBarDismissListener) {
        buildCookieWithListener(context, title, message, R.color.success_dark, R.drawable.ic_check_circle, duration, cookieBarDismissListener).show();
    }

    /**
     * Show error message
     */
    public static void showError(@NonNull Context context, String title, String message) {
        showError(context, title, message, SHORT_DURATION);
    }

    public static void showError(@NonNull Context context, String title, String message, long duration) {
        buildCookie(context, title, message, R.color.error_dark, R.drawable.ic_error, duration).show();
    }

    /**
     * Show info message
     */
    public static void showInfo(@NonNull Context context, String title, String message) {
        showInfo(context, title, message, SHORT_DURATION);
    }

    public static void showInfo(@NonNull Context context, String title, String message, long duration) {
        buildCookie(context, title, message, R.color.primary, R.drawable.ic_info, duration).show();
    }

    /**
     * Show warning message
     */
    public static void showWarning(@NonNull Context context, String title, String message) {
        showWarning(context, title, message, SHORT_DURATION);
    }

    public static void showWarning(@NonNull Context context, String title, String message, long duration) {
        buildCookie(context, title, message, R.color.warning_dark, R.drawable.ic_warning, duration).show();
    }

    /**
     * Show no internet connection message
     */
    public static void showNoInternet(@NonNull Context context) {
        buildCookie(context, "No Internet Connection", 
                "Please check your network settings and try again",
                R.color.error_dark, R.drawable.ic_wifi_off, LONG_DURATION).show();
    }

    /**
     * Show with custom action button
     */
    public static void showWithAction(@NonNull Context context, String title, String message,
                                      String actionText, Runnable action, int type) {
        Activity activity = getActivity(context);
        if (activity == null) return;
        
        int backgroundColorRes = getColorResForType(type);
        int icon = getIconForType(type);
        
        CookieBar.build(activity)
                .setTitle(title)
                .setMessage(message)
                .setDuration(EXTRA_LONG_DURATION)
                .setBackgroundColor(backgroundColorRes)
                .setActionColor(R.color.white)
                .setIcon(icon)
                .setIconAnimation(R.animator.fade_in)
                .setAction(actionText, action::run)
                .setCookiePosition(Gravity.TOP)
                .setSwipeToDismiss(true)
                .setEnableAutoDismiss(true)
                .show();
    }

    /**
     * Show persistent message (must be dismissed manually)
     */
    public static void showPersistent(@NonNull Context context, String title, String message,
                                      String dismissText, int type) {
        Activity activity = getActivity(context);
        if (activity == null) return;
        
        int backgroundColorRes = getColorResForType(type);
        int icon = getIconForType(type);
        
        CookieBar.build(activity)
                .setTitle(title)
                .setMessage(message)
                .setBackgroundColor(backgroundColorRes)
                .setActionColor(R.color.white)
                .setIcon(icon)
                .setIconAnimation(R.animator.fade_in)
                .setAction(dismissText, () -> {})
                .setCookiePosition(Gravity.TOP)
                .setSwipeToDismiss(true)
                .setEnableAutoDismiss(false)
                .show();
    }

    /**
     * Show minimal style (no icon, compact)
     */
    public static void showMinimal(@NonNull Context context, String message, int type) {
        Activity activity = getActivity(context);
        if (activity == null) return;
        
        int backgroundColorRes = getColorResForType(type);
        
        CookieBar.build(activity)
                .setMessage(message)
                .setDuration(SHORT_DURATION)
                .setBackgroundColor(backgroundColorRes)
                .setCookiePosition(Gravity.TOP)
                .setSwipeToDismiss(true)
                .setEnableAutoDismiss(true)
                .show();
    }

    /**
     * Show custom styled message
     */
    public static void showCustom(@NonNull Context context, String title, String message, 
                                   int backgroundColor, int iconRes, long duration) {
        Activity activity = getActivity(context);
        if (activity == null) return;
        
        CookieBar.build(activity)
                .setTitle(title)
                .setMessage(message)
                .setDuration(duration)
                .setBackgroundColor(backgroundColor)
                .setIcon(iconRes)
                .setIconAnimation(R.animator.fade_in)
                .setCookiePosition(Gravity.TOP)
                .setSwipeToDismiss(true)
                .setEnableAutoDismiss(true)
                .show();
    }

    // Type constants
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_ERROR = 1;
    public static final int TYPE_INFO = 2;
    public static final int TYPE_WARNING = 3;

    @ColorRes
    private static int getColorResForType(int type) {
        switch (type) {
            case TYPE_SUCCESS:
                return R.color.success_dark;
            case TYPE_ERROR:
                return R.color.error_dark;
            case TYPE_WARNING:
                return R.color.warning_dark;
            case TYPE_INFO:
            default:
                return R.color.primary;
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
