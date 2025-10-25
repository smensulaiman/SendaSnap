package com.sendajapan.sendasnap.utils;

import android.app.Activity;
import android.content.Context;

import androidx.core.content.res.ResourcesCompat;

import com.sendajapan.sendasnap.R;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class MotionToastHelper {

    // SUCCESS TOASTS
    public static void showSuccess(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateSuccess();
        MotionToast.Companion.createToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.SUCCESS,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    public static void showSuccessDark(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateSuccess();
        MotionToast.Companion.darkToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.SUCCESS,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    public static void showSuccessColor(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateSuccess();
        MotionToast.Companion.createColorToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.SUCCESS,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    public static void showSuccessColorDark(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateSuccess();
        MotionToast.Companion.darkColorToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.SUCCESS,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    // ERROR TOASTS
    public static void showError(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateError();
        MotionToast.Companion.createToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.ERROR,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    public static void showErrorDark(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateError();
        MotionToast.Companion.darkToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.ERROR,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    public static void showErrorColor(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateError();
        MotionToast.Companion.createColorToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.ERROR,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    public static void showErrorColorDark(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateError();
        MotionToast.Companion.darkColorToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.ERROR,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    // WARNING TOASTS
    public static void showWarning(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateWarning();
        MotionToast.Companion.createToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.WARNING,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    public static void showWarningDark(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateWarning();
        MotionToast.Companion.darkToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.WARNING,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    public static void showWarningColor(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateWarning();
        MotionToast.Companion.createColorToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.WARNING,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    public static void showWarningColorDark(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateWarning();
        MotionToast.Companion.darkColorToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.WARNING,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    // INFO TOASTS
    public static void showInfo(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateClick();
        MotionToast.Companion.createToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.INFO,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    public static void showInfoDark(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateClick();
        MotionToast.Companion.darkToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.INFO,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    public static void showInfoColor(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateClick();
        MotionToast.Companion.createColorToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.INFO,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    public static void showInfoColorDark(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateClick();
        MotionToast.Companion.darkColorToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.INFO,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    // DELETE TOASTS
    public static void showDelete(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateError();
        MotionToast.Companion.createToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.DELETE,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    public static void showDeleteDark(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateError();
        MotionToast.Companion.darkToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.DELETE,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    public static void showDeleteColor(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateError();
        MotionToast.Companion.createColorToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.DELETE,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    public static void showDeleteColorDark(Context context, String title, String message, int gravity, long duration) {
        HapticFeedbackHelper.getInstance(context).vibrateError();
        MotionToast.Companion.darkColorToast(
                (Activity) context,
                title,
                message,
                MotionToastStyle.DELETE,
                gravity,
                duration,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }

    // NO INTERNET TOAST (Special case - always at top)
    public static void showNoInternet(Context context) {
        HapticFeedbackHelper.getInstance(context).vibrateError();
        MotionToast.Companion.createToast(
                (Activity) context,
                "No Internet Connection",
                "Please check your internet connection and try again",
                MotionToastStyle.WARNING,
                MotionToast.GRAVITY_TOP,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(context, R.font.montserrat_regular));
    }
}