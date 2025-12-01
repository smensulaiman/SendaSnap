package com.sendajapan.sendasnap.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.activities.VehicleDetailsActivity;
import com.sendajapan.sendasnap.databinding.ItemVehicleBinding;
import com.sendajapan.sendasnap.models.Vehicle;
import com.sendajapan.sendasnap.utils.CurrencyFormatter;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying vehicle items in a RecyclerView.
 * Handles vehicle list display with proper view binding and click interactions.
 */
public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    private static final String CURRENCY_SYMBOL = "￥";
    private static final String PHOTO_COUNT_FORMAT = "%d Photo(s)";
    private static final String CHASSIS_SEPARATOR = "-";
    private static final int FIRST_IMAGE_INDEX = 0;

    private final List<Vehicle> vehicles;
    private final OnVehicleClickListener listener;
    private final HapticFeedbackHelper hapticHelper;

    /**
     * Interface for handling vehicle item click events.
     */
    public interface OnVehicleClickListener {
        /**
         * Called when a vehicle item is clicked.
         *
         * @param vehicle The clicked vehicle object
         */
        void onVehicleClick(Vehicle vehicle);
    }

    /**
     * Constructor for VehicleAdapter.
     *
     * @param listener The click listener for vehicle items
     */
    public VehicleAdapter(OnVehicleClickListener listener) {
        this.vehicles = new ArrayList<>();
        this.listener = listener;
        this.hapticHelper = null; // Will be initialized per view holder context
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemVehicleBinding binding = ItemVehicleBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);
        return new VehicleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        if (position >= 0 && position < vehicles.size()) {
            Vehicle vehicle = vehicles.get(position);
            holder.bind(vehicle);
        }
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    /**
     * Updates the vehicle list and notifies the adapter.
     *
     * @param newVehicles The new list of vehicles to display
     */
    public void updateVehicles(@NonNull List<Vehicle> newVehicles) {
        this.vehicles.clear();
        this.vehicles.addAll(newVehicles);
        notifyDataSetChanged();
    }

    /**
     * ViewHolder for vehicle items.
     * Uses View Binding for type-safe view access.
     */
    class VehicleViewHolder extends RecyclerView.ViewHolder {

        private final ItemVehicleBinding binding;
        private HapticFeedbackHelper viewHapticHelper;

        VehicleViewHolder(@NonNull ItemVehicleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.viewHapticHelper = HapticFeedbackHelper.getInstance(binding.getRoot().getContext());
            setupClickListener();
        }

        /**
         * Sets up the click listener for the vehicle item.
         */
        private void setupClickListener() {
            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < vehicles.size()) {
                    Vehicle vehicle = vehicles.get(position);
                    if (vehicle != null) {
                        viewHapticHelper.vibrateClick();
                        openVehicleDetails(vehicle);
                    }
                }
            });
        }

        /**
         * Binds vehicle data to the view holder.
         *
         * @param vehicle The vehicle object to bind
         */
        @SuppressLint("SetTextI18n")
        void bind(@NonNull Vehicle vehicle) {
            loadVehicleImage(vehicle);
            setVehicleDetails(vehicle);
        }

        /**
         * Loads the vehicle image using Glide.
         *
         * @param vehicle The vehicle object containing image URLs
         */
        private void loadVehicleImage(@NonNull Vehicle vehicle) {
            binding.imgVehicle.setImageResource(R.drawable.car_placeholder);

            List<String> vehiclePhotos = vehicle.getVehiclePhotos();
            if (vehiclePhotos != null && !vehiclePhotos.isEmpty()) {
                String imageUrl = vehiclePhotos.get(FIRST_IMAGE_INDEX);
                if (isValidImageUrl(imageUrl)) {
                    Glide.with(binding.getRoot().getContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.car_placeholder)
                            .error(R.drawable.car_placeholder)
                            .centerCrop()
                            .into(binding.imgVehicle);
                }
            }
        }

        /**
         * Sets all vehicle details to the corresponding views.
         *
         * @param vehicle The vehicle object containing the details
         */
        private void setVehicleDetails(@NonNull Vehicle vehicle) {
            // Vehicle name and model
            binding.txtVehicleName.setText(vehicle.getDisplayName());
            binding.txtMakeModel.setText(vehicle.getModel());

            // Chassis number and vehicle ID
            String chassisNumber = formatChassisNumber(vehicle);
            binding.txtChassisNumber.setText(chassisNumber);
            binding.txtVehicleId.setText(vehicle.getId());

            // Buying information
            binding.txtBuyingDate.setText(vehicle.getVehicleBuyDate());
            String formattedPrice = CURRENCY_SYMBOL + CurrencyFormatter.formatBuyingPrice(vehicle.getBuyingPrice());
            binding.txtBuyingPrice.setText(formattedPrice);

            // Photo count
            int photoCount = getPhotoCount(vehicle);
            binding.txtPhotoCount.setText(String.format(PHOTO_COUNT_FORMAT, photoCount));
        }

        /**
         * Formats the chassis number from vehicle model and serial number.
         *
         * @param vehicle The vehicle object
         * @return Formatted chassis number string
         */
        private String formatChassisNumber(@NonNull Vehicle vehicle) {
            String chassisModel = vehicle.getChassisModel();
            String serialNumber = vehicle.getSerialNumber();

            if (chassisModel != null && serialNumber != null) {
                return chassisModel + CHASSIS_SEPARATOR + serialNumber;
            } else if (chassisModel != null) {
                return chassisModel;
            } else if (serialNumber != null) {
                return serialNumber;
            }
            return "";
        }

        /**
         * Gets the photo count from vehicle, handling null safety.
         *
         * @param vehicle The vehicle object
         * @return The number of photos (0 if null or empty)
         */
        private int getPhotoCount(@NonNull Vehicle vehicle) {
            List<String> photos = vehicle.getVehiclePhotos();
            return photos != null ? photos.size() : 0;
        }

        /**
         * Checks if the image URL is valid and not empty.
         *
         * @param imageUrl The image URL to validate
         * @return true if the URL is valid, false otherwise
         */
        private boolean isValidImageUrl(String imageUrl) {
            return imageUrl != null && !imageUrl.trim().isEmpty();
        }

        /**
         * Opens the VehicleDetailsActivity for the selected vehicle.
         *
         * @param vehicle The vehicle to display details for
         */
        private void openVehicleDetails(@NonNull Vehicle vehicle) {
            Intent intent = new Intent(binding.getRoot().getContext(), VehicleDetailsActivity.class);
            intent.putExtra("vehicle", vehicle);
            binding.getRoot().getContext().startActivity(intent);
        }
    }
}
