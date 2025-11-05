package com.sendajapan.sendasnap.utils;

import android.app.Activity;

import com.sendajapan.sendasnap.R;

import org.aviran.cookiebar2.CookieBar;
import org.aviran.cookiebar2.CookieBarDismissListener;

public class CookieBarToastHelper {

    public static long DURATION_LONG = 5000L;
    public static long DURATION_SHORT = 2000L;

    protected Activity activity;
    protected CookieBar.Builder cookieBar;

    public CookieBarToastHelper(Activity activity) {
        this.activity = activity;
        cookieBar = CookieBar.build(activity);
    }

    public void showSuccessToast(String title, String message, int position, long duration) {
        cookieBar.setTitle(title);
        cookieBar.setMessage(message);
        cookieBar.setIcon(R.drawable.toast_success_green_24);
        cookieBar.setIconAnimation(R.animator.iconspin);
        cookieBar.setBackgroundColor(R.color.success_light);
        cookieBar.setTitleColor(R.color.success_dark);
        cookieBar.setMessageColor(R.color.success_dark);
        cookieBar.setCookiePosition(position);
        cookieBar.setDuration(duration);
        cookieBar.show();
    }

    public void showErrorToast(String title, String message, int position, long duration) {
        cookieBar.setTitle(title);
        cookieBar.setMessage(message);
        cookieBar.setIcon(R.drawable.toast_cancel_red_24);
        cookieBar.setIconAnimation(R.animator.iconspin);
        cookieBar.setBackgroundColor(R.color.error_light);
        cookieBar.setTitleColor(R.color.error_dark);
        cookieBar.setMessageColor(R.color.error_dark);
        cookieBar.setCookiePosition(position);
        cookieBar.setDuration(duration);
        cookieBar.show();
    }

    public void setCookieBarDismissListener(CookieBarDismissListener cookieBarDismissListener){
        cookieBar.setCookieListener(cookieBarDismissListener);
    }

}
