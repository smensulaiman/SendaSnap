package com.sendajapan.sendasnap.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.gson.Gson;
import com.sendajapan.sendasnap.models.ErrorResponse;
import com.sendajapan.sendasnap.models.Vehicle;
import com.sendajapan.sendasnap.models.VehicleImageUploadResponse;
import com.sendajapan.sendasnap.networking.ApiService;
import com.sendajapan.sendasnap.networking.RetrofitClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleImageUploadService {

    private static final String TAG = "VehicleImageUpload";
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB in bytes

    private final ApiService apiService;
    private final Context context;

    public interface UploadCallback {
        void onSuccess(Vehicle vehicle);

        void onError(String errorMessage, int errorCode);
    }

    public VehicleImageUploadService(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = RetrofitClient.getInstance(context).getApiService();
    }

    /**
     * Upload vehicle images to the server
     * 
     * @param vehicleId  The vehicle ID (integer from external database)
     * @param imagePaths List of image file paths
     * @param callback   Callback for success/error handling
     */
    public void uploadImages(int vehicleId, List<String> imagePaths, UploadCallback callback) {
        // Validate inputs
        if (imagePaths == null || imagePaths.isEmpty()) {
            callback.onError("No images to upload", 422);
            return;
        }

        // Validate and prepare image files
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        List<File> imageFiles = new ArrayList<>();
        List<File> tempFiles = new ArrayList<>(); // Track temporary compressed files

        for (String imagePath : imagePaths) {
            File imageFile = new File(imagePath);

            // Check if file exists
            if (!imageFile.exists() || !imageFile.isFile()) {
                callback.onError("Image file not found: " + imageFile.getName(), 422);
                return;
            }

            // Compress image if it's larger than 2MB
            File fileToUpload = imageFile;
            if (imageFile.length() > MAX_FILE_SIZE) {
                try {
                    fileToUpload = compressImage(imageFile);
                    if (fileToUpload != null && fileToUpload.exists()) {
                        tempFiles.add(fileToUpload); // Track for cleanup
                        Log.d(TAG, "Compressed image: " + imageFile.getName() +
                                " from " + (imageFile.length() / 1024) + "KB to " +
                                (fileToUpload.length() / 1024) + "KB");
                    } else {
                        // Compression failed, use original
                        Log.w(TAG, "Compression failed for: " + imageFile.getName() + ", using original");
                        fileToUpload = imageFile;
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error compressing image: " + imageFile.getName(), e);
                    // Use original file if compression fails
                    fileToUpload = imageFile;
                }
            }

            // Final check: if still too large after compression, return error
            if (fileToUpload.length() > MAX_FILE_SIZE) {
                // Clean up temp files before returning
                cleanupTempFiles(tempFiles);
                callback.onError("Image file too large (max 2MB) even after compression: " + imageFile.getName(), 422);
                return;
            }

            imageFiles.add(fileToUpload);
        }

        // Create multipart parts for images
        for (File imageFile : imageFiles) {
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("image/*"), imageFile);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                    "images[]", imageFile.getName(), requestFile);
            imageParts.add(imagePart);
        }

        // Create vehicle_id part
        RequestBody vehicleIdBody = RequestBody.create(
                MediaType.parse("text/plain"), String.valueOf(vehicleId));

        // Make API call
        Call<VehicleImageUploadResponse> call = apiService.uploadVehicleImagesNew(
                vehicleIdBody, imageParts);

        call.enqueue(new Callback<VehicleImageUploadResponse>() {
            @Override
            public void onResponse(Call<VehicleImageUploadResponse> call,
                    Response<VehicleImageUploadResponse> response) {
                // Clean up temporary compressed files
                cleanupTempFiles(tempFiles);

                if (response.isSuccessful() && response.body() != null) {
                    VehicleImageUploadResponse uploadResponse = response.body();

                    if (uploadResponse.getSuccess() != null && uploadResponse.getSuccess()) {
                        // Success
                        Vehicle vehicle = uploadResponse.getData() != null
                                ? uploadResponse.getData().getVehicle()
                                : null;
                        callback.onSuccess(vehicle);
                    } else {
                        // API returned success: false
                        String errorMessage = uploadResponse.getMessage() != null
                                ? uploadResponse.getMessage()
                                : "Upload failed";
                        callback.onError(errorMessage, response.code());
                    }
                } else {
                    // Handle error response
                    String errorMessage = parseErrorMessage(response);
                    callback.onError(errorMessage, response.code());
                }
            }

            @Override
            public void onFailure(Call<VehicleImageUploadResponse> call, Throwable t) {
                // Clean up temporary compressed files
                cleanupTempFiles(tempFiles);

                Log.e(TAG, "Upload failed", t);
                String errorMessage = "Network error. Please check your connection and try again.";
                if (t.getMessage() != null) {
                    errorMessage = t.getMessage();
                }
                callback.onError(errorMessage, 0);
            }
        });
    }

    /**
     * Parse error message from error response
     */
    private String parseErrorMessage(Response<VehicleImageUploadResponse> response) {
        String defaultMessage = "An error occurred. Please try again.";

        if (response.errorBody() != null) {
            try {
                String errorBodyStr = response.errorBody().string();

                Gson gson = new Gson();
                ErrorResponse errorResponse = gson.fromJson(errorBodyStr, ErrorResponse.class);

                if (errorResponse != null) {
                    // Use formatted error message if available
                    if (errorResponse.getErrors() != null && !errorResponse.getErrors().isEmpty()) {
                        return errorResponse.getFormattedErrorMessage();
                    } else if (errorResponse.getMessage() != null) {
                        return errorResponse.getMessage();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing error response", e);
            }
        }

        // Fallback to HTTP status code message
        if (response.code() == 404) {
            return "Vehicle not found in external database";
        } else if (response.code() == 422) {
            return "Validation failed. Please check your input.";
        } else if (response.code() == 502) {
            return "External database query failed";
        }

        return defaultMessage;
    }

    /**
     * Compress image file if it's larger than 2MB
     * 
     * @param imageFile Original image file
     * @return Compressed image file, or null if compression fails
     */
    private File compressImage(File imageFile) {
        try {
            // Decode bitmap with reduced sample size to save memory
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

            // Calculate sample size to reduce memory usage
            int sampleSize = calculateInSampleSize(options, 1920, 1920); // Max dimensions
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;

            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
            if (bitmap == null) {
                Log.e(TAG, "Failed to decode bitmap: " + imageFile.getName());
                return null;
            }

            // Compress bitmap
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int quality = 85;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

            // Reduce quality if still too large
            while (baos.toByteArray().length > MAX_FILE_SIZE && quality > 30) {
                baos.reset();
                quality -= 10;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
            }

            // If still too large, resize the bitmap
            if (baos.toByteArray().length > MAX_FILE_SIZE) {
                int maxDimension = Math.max(bitmap.getWidth(), bitmap.getHeight());
                int targetSize = 1200; // Target max dimension

                if (maxDimension > targetSize) {
                    float scale = (float) targetSize / maxDimension;
                    int newWidth = (int) (bitmap.getWidth() * scale);
                    int newHeight = (int) (bitmap.getHeight() * scale);

                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                    bitmap.recycle();
                    bitmap = resizedBitmap;

                    // Try compressing again with resized bitmap
                    baos.reset();
                    quality = 80;
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);

                    // Reduce quality further if needed
                    while (baos.toByteArray().length > MAX_FILE_SIZE && quality > 30) {
                        baos.reset();
                        quality -= 10;
                        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
                    }
                }
            }

            // Create temporary file for compressed image
            File compressedFile = new File(context.getCacheDir(),
                    "compressed_" + System.currentTimeMillis() + "_" + imageFile.getName());

            // Write compressed bitmap to file
            FileOutputStream fos = new FileOutputStream(compressedFile);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();

            // Clean up
            bitmap.recycle();
            baos.close();

            return compressedFile;

        } catch (IOException e) {
            Log.e(TAG, "Error compressing image: " + imageFile.getName(), e);
            return null;
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "Out of memory while compressing image: " + imageFile.getName(), e);
            return null;
        }
    }

    /**
     * Calculate sample size for bitmap decoding to reduce memory usage
     */
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Clean up temporary compressed files
     */
    private void cleanupTempFiles(List<File> tempFiles) {
        for (File tempFile : tempFiles) {
            try {
                if (tempFile != null && tempFile.exists()) {
                    boolean deleted = tempFile.delete();
                    if (!deleted) {
                        Log.w(TAG, "Failed to delete temp file: " + tempFile.getName());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting temp file: " + tempFile.getName(), e);
            }
        }
        tempFiles.clear();
    }
}
