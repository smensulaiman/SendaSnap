package com.sendajapan.sendasnap.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class PermissionsHelper {
    public static final int PERMISSION_REQUEST_CODE = 1001;

    // Permission constants
    public static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
    public static final String READ_MEDIA_IMAGES_PERMISSION = Manifest.permission.READ_MEDIA_IMAGES;
    public static final String READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final String INTERNET_PERMISSION = Manifest.permission.INTERNET;
    public static final String ACCESS_NETWORK_STATE_PERMISSION = Manifest.permission.ACCESS_NETWORK_STATE;
    public static final String VIBRATE_PERMISSION = Manifest.permission.VIBRATE;

    public static boolean hasCameraPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context,
                    READ_MEDIA_IMAGES_PERMISSION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context,
                    READ_EXTERNAL_STORAGE_PERMISSION) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static boolean hasInternetPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, INTERNET_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasVibratePermission(Context context) {
        return ContextCompat.checkSelfPermission(context, VIBRATE_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    public static String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[] {
                    CAMERA_PERMISSION,
                    READ_MEDIA_IMAGES_PERMISSION,
                    INTERNET_PERMISSION,
                    ACCESS_NETWORK_STATE_PERMISSION,
                    VIBRATE_PERMISSION
            };
        } else {
            return new String[] {
                    CAMERA_PERMISSION,
                    READ_EXTERNAL_STORAGE_PERMISSION,
                    INTERNET_PERMISSION,
                    ACCESS_NETWORK_STATE_PERMISSION,
                    VIBRATE_PERMISSION
            };
        }
    }

    public static String[] getMissingPermissions(Context context) {
        List<String> missingPermissions = new ArrayList<>();
        String[] requiredPermissions = getRequiredPermissions();

        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        return missingPermissions.toArray(new String[0]);
    }

    public static void requestPermissions(Activity activity) {
        String[] missingPermissions = getMissingPermissions(activity);
        if (missingPermissions.length > 0) {
            ActivityCompat.requestPermissions(activity, missingPermissions, PERMISSION_REQUEST_CODE);
        }
    }

    public static boolean shouldShowRationale(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    public static boolean areAllPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
