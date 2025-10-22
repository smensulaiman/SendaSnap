package com.sendajapan.sendasnap.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.adapters.VehiclePhotoAdapter;
import com.sendajapan.sendasnap.databinding.ActivityVehicleDetailsBinding;
import com.sendajapan.sendasnap.models.Vehicle;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.MotionToastHelper;

import www.sanju.motiontoast.MotionToast;

import java.util.ArrayList;
import java.util.List;

public class VehicleDetailsActivity extends AppCompatActivity {

    private ActivityVehicleDetailsBinding binding;
    private Vehicle vehicle;
    private VehiclePhotoAdapter photoAdapter;
    private HapticFeedbackHelper hapticHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityVehicleDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        setupPhotoViewPager();
    }

    private void initHelpers() {
        hapticHelper = HapticFeedbackHelper.getInstance(this);
    }

    private void getVehicleData() {
        vehicle = (Vehicle) getIntent().getSerializableExtra("vehicle");
        if (vehicle == null) {
            MotionToastHelper.showError(this, "Error", "Vehicle data not found", MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION);
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
        binding.fabAddImages.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            showAddImagesDialog();
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
        binding.txtPrice.setText(vehicle.getBuyingPrice() != null ? "$" + vehicle.getBuyingPrice() : "N/A");
        binding.txtAuctionShip.setText(vehicle.getAuctionShipNumber() != null ? vehicle.getAuctionShipNumber() : "N/A");

        // Shipping Information
        binding.txtExpectedYardDate
                .setText(vehicle.getExpectedYardDate() != null ? vehicle.getExpectedYardDate() : "N/A");
        binding.txtRiksoFrom.setText(vehicle.getRiksoFrom() != null ? vehicle.getRiksoFrom() : "N/A");
        binding.txtRiksoTo.setText(vehicle.getRiksoTo() != null ? vehicle.getRiksoTo() : "N/A");
        binding.txtRiksoCost.setText(vehicle.getRiksoCost() != null ? "$" + vehicle.getRiksoCost() : "N/A");
    }

    private void setupPhotoViewPager() {
        List<String> photos = vehicle.getVehiclePhotos();
        if (photos == null || photos.isEmpty()) {
            // Use placeholder images
            photos = new ArrayList<>();
            photos.add("placeholder1");
            photos.add("placeholder2");
        }

        photoAdapter = new VehiclePhotoAdapter(photos);
        binding.viewPagerPhotos.setAdapter(photoAdapter);

        // Setup photo indicators
        setupPhotoIndicators(photos.size());
    }

    private void setupPhotoIndicators(int photoCount) {
        binding.layoutPhotoIndicators.removeAllViews();

        for (int i = 0; i < photoCount; i++) {
            View indicator = new View(this);
            indicator.setBackgroundResource(R.drawable.photo_indicator_unselected);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    getResources().getDimensionPixelSize(R.dimen.photo_indicator_size),
                    getResources().getDimensionPixelSize(R.dimen.photo_indicator_size));
            params.setMargins(4, 0, 4, 0);
            indicator.setLayoutParams(params);

            binding.layoutPhotoIndicators.addView(indicator);
        }

        // Update indicators based on current page
        binding.viewPagerPhotos.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updatePhotoIndicators(position);
            }
        });
    }

    private void updatePhotoIndicators(int selectedPosition) {
        for (int i = 0; i < binding.layoutPhotoIndicators.getChildCount(); i++) {
            View indicator = binding.layoutPhotoIndicators.getChildAt(i);
            if (i == selectedPosition) {
                indicator.setBackgroundResource(R.drawable.photo_indicator_selected);
            } else {
                indicator.setBackgroundResource(R.drawable.photo_indicator_unselected);
            }
        }
    }

    private void showAddImagesDialog() {
        // TODO: Implement image picker bottom sheet
        MotionToastHelper.showInfo(this, "Add Images", "Image picker will be implemented here",
                MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
