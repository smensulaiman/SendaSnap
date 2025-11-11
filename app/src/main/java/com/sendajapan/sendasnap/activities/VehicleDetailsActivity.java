package com.sendajapan.sendasnap.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.adapters.PendingImageAdapter;
import com.sendajapan.sendasnap.adapters.VehicleImageGridAdapter;
import com.sendajapan.sendasnap.databinding.ActivityVehicleDetailsBinding;
import com.sendajapan.sendasnap.models.Vehicle;
import com.sendajapan.sendasnap.networking.ApiService;
import com.sendajapan.sendasnap.networking.RetrofitClient;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.CookieBarToastHelper;
import com.sendajapan.sendasnap.utils.LoadingDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class VehicleDetailsActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int STORAGE_PERMISSION_REQUEST = 101;

    private ActivityVehicleDetailsBinding binding;

    private ApiService apiService;

    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;

    private VehicleImageGridAdapter vehicleImageAdapter;
    private PendingImageAdapter pendingImageAdapter;
    private HapticFeedbackHelper hapticHelper;
    private final List<String> pendingImagePaths = new ArrayList<>();

    private Vehicle vehicle;
    private Uri cameraImageUri;
    private String cameraImagePath;

    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityVehicleDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set status bar and navigation bar colors
        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_color, getTheme()));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.navigation_bar_color, getTheme()));

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initHelpers();
        getVehicleData();
        setupToolbar();
        setupClickListeners();
        populateVehicleData();
        setupImageGrids();
        setupImagePickers();
    }

    private void initHelpers() {
        hapticHelper = HapticFeedbackHelper.getInstance(this);
        apiService = RetrofitClient.getInstance(this).getApiService();

        loadingDialog = new LoadingDialog.Builder(VehicleDetailsActivity.this)
                .setMessage("Uploading Data!")
                .setSubtitle("Please wait while uploading images...")
                .setCancelable(false)
                .setShowProgressIndicator(true)
                .build();
    }

    private void getVehicleData() {
        vehicle = (Vehicle) getIntent().getSerializableExtra("vehicle");
        if (vehicle == null) {
            CookieBarToastHelper.showError(this, "Error", "Vehicle data not found",
                    CookieBarToastHelper.LONG_DURATION);
            finish();
        }
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            hapticHelper.vibrateClick();
            finish();
        });
    }

    private void setupClickListeners() {
        binding.btnAddPhotos.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            showImagePickerBottomSheet();
        });

        binding.btnUploadPhotos.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            uploadPendingImages();
        });
    }

    private void populateVehicleData() {
        if (vehicle == null)
            return;

        // Basic Information
        binding.txtMake.setText(vehicle.getMake() != null ? vehicle.getMake() : "N/A");
        binding.txtModel.setText(vehicle.getModel() != null ? vehicle.getModel() : "N/A");
        binding.txtYear.setText(vehicle.getYear() != null ? vehicle.getYear() : "N/A");
        binding.txtColor.setText(vehicle.getColor() != null ? vehicle.getColor() : "N/A");
        binding.txtChassis.setText(vehicle.getSerialNumber() != null ? vehicle.getSerialNumber() : "N/A");

        // Purchase Information
        binding.txtBuyDate.setText(vehicle.getVehicleBuyDate() != null ? vehicle.getVehicleBuyDate() : "N/A");
        binding.txtPrice.setText(vehicle.getBuyingPrice() != null ? "￥" + vehicle.getBuyingPrice() : "N/A");
        binding.txtAuctionShip.setText(vehicle.getAuctionShipNumber() != null ? vehicle.getAuctionShipNumber() : "N/A");

        // Shipping Information
        binding.txtExpectedYardDate
                .setText(vehicle.getExpectedYardDate() != null ? vehicle.getExpectedYardDate() : "N/A");
        binding.txtRiksoFrom.setText(vehicle.getRiksoFrom() != null ? vehicle.getRiksoFrom() : "N/A");
        binding.txtRiksoTo.setText(vehicle.getRiksoTo() != null ? vehicle.getRiksoTo() : "N/A");
        binding.txtRiksoCost.setText(vehicle.getRiksoCost() != null ? "￥" + vehicle.getRiksoCost() : "N/A");
    }

    private void setupImageGrids() {
        // Setup vehicle images grid
        List<String> vehiclePhotos = vehicle.getVehiclePhotos();
        if (vehiclePhotos == null) {
            vehiclePhotos = new ArrayList<>();
        }

        vehicleImageAdapter = new VehicleImageGridAdapter(vehiclePhotos);
        GridLayoutManager vehicleGridLayoutManager = new GridLayoutManager(this, 3);
        binding.recyclerViewVehicleImages.setLayoutManager(vehicleGridLayoutManager);
        binding.recyclerViewVehicleImages.setAdapter(vehicleImageAdapter);

        // Setup pending images grid
        pendingImageAdapter = new PendingImageAdapter(pendingImagePaths);
        GridLayoutManager pendingGridLayoutManager = new GridLayoutManager(this, 3);
        binding.recyclerViewPendingImages.setLayoutManager(pendingGridLayoutManager);
        binding.recyclerViewPendingImages.setAdapter(pendingImageAdapter);

        // Setup delete listener for pending images
        pendingImageAdapter.setOnDeleteClickListener(position -> {
            hapticHelper.vibrateClick();
            pendingImagePaths.remove(position);
            pendingImageAdapter.notifyItemRemoved(position);
            updatePendingImagesVisibility();
        });
    }

    private void setupImagePickers() {
        // Camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                result -> {
                    if (result && cameraImagePath != null) {
                        // Use the stored file path directly
                        if (new File(cameraImagePath).exists()) {
                            pendingImagePaths.add(cameraImagePath);
                            pendingImageAdapter.notifyItemInserted(pendingImagePaths.size() - 1);
                            updatePendingImagesVisibility();

                            CookieBarToastHelper.showSuccess(this,
                                    "Success", "Photo added successfully",
                                    CookieBarToastHelper.SHORT_DURATION);
                        } else {
                            CookieBarToastHelper.showError(this, "Error", "Failed to save photo",
                                    CookieBarToastHelper.LONG_DURATION);
                        }
                    } else if (!result) {
                        CookieBarToastHelper.showInfo(this, "Cancelled", "Photo capture cancelled",
                                CookieBarToastHelper.SHORT_DURATION);
                    }
                });

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        for (Uri uri : uris) {
                            String imagePath = getRealPathFromURI(uri);
                            if (imagePath != null) {
                                pendingImagePaths.add(imagePath);
                            }
                        }

                        pendingImageAdapter.notifyDataSetChanged();
                        updatePendingImagesVisibility();
                    }
                });
    }

    private void updatePendingImagesVisibility() {
        if (pendingImagePaths.isEmpty()) {
            binding.recyclerViewPendingImages.setVisibility(View.GONE);
            binding.btnUploadPhotos.setVisibility(View.GONE);
        } else {
            binding.recyclerViewPendingImages.setVisibility(View.VISIBLE);
            binding.btnUploadPhotos.setVisibility(View.VISIBLE);
        }
    }

    private void showImagePickerBottomSheet() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_image_picker, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        View layoutCamera = bottomSheetView.findViewById(R.id.layoutCamera);
        View layoutGallery = bottomSheetView.findViewById(R.id.layoutGallery);

        layoutCamera.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            bottomSheetDialog.dismiss();
            openCamera();
        });

        layoutGallery.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            bottomSheetDialog.dismiss();
            openGallery();
        });

        bottomSheetDialog.show();
    }

    private void openCamera() {
        String[] permissions;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+ (API 33+), we don't need storage permission for camera
            permissions = new String[]{Manifest.permission.CAMERA};
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    CookieBarToastHelper.showInfo(this, "Permission Required",
                            "Camera permission is needed to take photos", CookieBarToastHelper.LONG_DURATION);
                }
                ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION_REQUEST);
                return;
            }
        } else {
            // For older versions, we need both camera and storage permissions
            permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    ||
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) ||
                        ActivityCompat.shouldShowRequestPermissionRationale(this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    CookieBarToastHelper.showInfo(this, "Permission Required",
                            "Camera and storage permissions are needed to take photos", CookieBarToastHelper.LONG_DURATION);
                }
                ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION_REQUEST);
                return;
            }
        }

        try {
            cameraImageUri = createImageFile();
            if (cameraImageUri != null) {
                // Test if camera app is available
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    cameraLauncher.launch(cameraImageUri);
                } else {
                    CookieBarToastHelper.showError(this, "Error", "No camera app available on this device",
                            CookieBarToastHelper.LONG_DURATION);
                }
            } else {
                CookieBarToastHelper.showError(this, "Error", "Failed to create image file", CookieBarToastHelper.LONG_DURATION);
            }
        } catch (Exception e) {
            CookieBarToastHelper.showError(this, "Error", "Failed to open camera: " + e.getMessage(),
                    CookieBarToastHelper.LONG_DURATION);
        }
    }

    private void openGallery() {
        String[] permissions;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // For Android 13+ (API 33+), use READ_MEDIA_IMAGES
            permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)) {
                    CookieBarToastHelper.showInfo(this, "Permission Required",
                            "Storage permission is needed to access photos", CookieBarToastHelper.LONG_DURATION);
                }
                ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSION_REQUEST);
                return;
            }
        } else {
            // For older versions, use READ_EXTERNAL_STORAGE
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    CookieBarToastHelper.showInfo(this, "Permission Required",
                            "Storage permission is needed to access photos", CookieBarToastHelper.LONG_DURATION);
                }
                ActivityCompat.requestPermissions(this, permissions, STORAGE_PERMISSION_REQUEST);
                return;
            }
        }

        galleryLauncher.launch("image/*");
    }

    private Uri createImageFile() {
        try {
            String timeStamp = String.valueOf(System.currentTimeMillis());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);

            // Create directory if it doesn't exist
            if (storageDir != null && !storageDir.exists()) {
                storageDir.mkdirs();
            }

            File image = new File(storageDir, imageFileName + ".jpg");

            // Create the file
            if (!image.exists()) {
                image.createNewFile();
            }

            // Store the file path for later use
            cameraImagePath = image.getAbsolutePath();

            // Use FileProvider for better compatibility
            return FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", image);
        } catch (Exception e) {
            CookieBarToastHelper.showError(this, "Error", "Failed to create image file: " + e.getMessage(),
                    CookieBarToastHelper.LONG_DURATION);
            return null;
        }
    }

    private String getRealPathFromURI(Uri uri) {
        try {
            if (uri.getScheme().equals("file")) {
                return uri.getPath();
            } else if (uri.getScheme().equals("content")) {
                // For FileProvider URIs, we need to get the actual file path
                String[] projection = {MediaStore.Images.Media.DATA};
                android.database.Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
                if (cursor != null) {
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String path = cursor.getString(column_index);
                    cursor.close();
                    return path;
                }
            }
        } catch (Exception e) {
            // Fallback to URI path
            return uri.getPath();
        }
        return uri.getPath();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                openCamera();
            } else {
                CookieBarToastHelper.showError(this, "Permission Denied", "Camera permission is required",
                        CookieBarToastHelper.LONG_DURATION);
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                openGallery();
            } else {
                CookieBarToastHelper.showError(this, "Permission Denied", "Storage permission is required",
                        CookieBarToastHelper.LONG_DURATION);
            }
        }
    }

    private void uploadPendingImages() {
        if (pendingImagePaths.isEmpty()) {
            CookieBarToastHelper.showInfo(this, "No Photos", "No photos to upload", CookieBarToastHelper.SHORT_DURATION);
            return;
        }

        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }

        new Handler().postDelayed(() -> {

            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }

            List<String> currentServerImages = vehicle.getVehiclePhotos();
            if (currentServerImages == null) {
                currentServerImages = new ArrayList<>();
            }

//            currentServerImages.addAll(pendingImagePaths);
//            vehicle.setVehiclePhotos(currentServerImages);
//
//            vehicleImageAdapter.notifyDataSetChanged();
//
//            pendingImagePaths.clear();
//            pendingImageAdapter.notifyDataSetChanged();
//            updatePendingImagesVisibility();

            CookieBarToastHelper.showSuccessWithListener(VehicleDetailsActivity.this,
                    "Success", "Photos uploaded successfully!",
                    CookieBarToastHelper.SHORT_DURATION,
                    i -> finish());
        }, 2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
