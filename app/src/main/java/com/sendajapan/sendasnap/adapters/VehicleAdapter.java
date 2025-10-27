package com.sendajapan.sendasnap.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.models.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    private List<Vehicle> vehicles;
    private OnVehicleClickListener listener;

    public interface OnVehicleClickListener {
        void onVehicleClick(Vehicle vehicle);
    }

    public VehicleAdapter(OnVehicleClickListener listener) {
        this.vehicles = new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicles.get(position);
        holder.bind(vehicle);
    }

    @Override
    public int getItemCount() {
        return vehicles.size();
    }

    public void updateVehicles(List<Vehicle> newVehicles) {
        this.vehicles.clear();
        this.vehicles.addAll(newVehicles);
        notifyDataSetChanged();
    }

    class VehicleViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgVehicle;
        private TextView txtVehicleName;
        private TextView txtMakeModel;
        private TextView txtChassisNumber;
        private TextView txtBuyingPrice;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);

            imgVehicle = itemView.findViewById(R.id.imgVehicle);
            txtVehicleName = itemView.findViewById(R.id.txtVehicleName);
            txtMakeModel = itemView.findViewById(R.id.txtMakeModel);
            txtChassisNumber = itemView.findViewById(R.id.txtChassisNumber);
            txtBuyingPrice = itemView.findViewById(R.id.txtBuyingPrice);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onVehicleClick(vehicles.get(position));
                }
            });
        }

        public void bind(Vehicle vehicle) {
            loadVehicleImage(vehicle);

            int modelLabelColor = Color.parseColor("#FFF8DC");
            int chassisLabelColor = Color.parseColor("#E0FFFF");

            txtVehicleName.setText(vehicle.getDisplayName());

            String modelLabel = "Model  ";
            String chassisLabel = "Chassis";
            int maxLabelLength = Math.max(modelLabel.length(), chassisLabel.length());

            String paddedModelLabel = String.format("%-" + maxLabelLength + "s", modelLabel);
            String paddedChassisLabel = String.format("%-" + maxLabelLength + "s", chassisLabel);

            SpannableString modelSpannable = new SpannableString(paddedModelLabel + " " + vehicle.getModel());
            modelSpannable.setSpan(
                    new BackgroundColorSpan(modelLabelColor),
                    0,
                    paddedModelLabel.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            modelSpannable.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    0,
                    paddedModelLabel.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            txtMakeModel.setText(modelSpannable);

            String chassisValue = vehicle.getChassisModel() + "-" + vehicle.getSerialNumber();
            SpannableString chassisSpannable = new SpannableString(paddedChassisLabel + " " + chassisValue);
            chassisSpannable.setSpan(
                    new BackgroundColorSpan(chassisLabelColor),
                    0,
                    paddedChassisLabel.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            chassisSpannable.setSpan(
                    new StyleSpan(Typeface.BOLD),
                    0,
                    paddedChassisLabel.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            txtChassisNumber.setText(chassisSpannable);

            String buyingPriceStr = vehicle.getBuyingPrice() != null ? vehicle.getBuyingPrice() : "0";
            txtBuyingPrice.setText("￥" + buyingPriceStr);
        }

        private void loadVehicleImage(Vehicle vehicle) {
            if (vehicle.getVehiclePhotos() != null && !vehicle.getVehiclePhotos().isEmpty()) {
                // Load first photo
                String imageUrl = vehicle.getVehiclePhotos().get(0);
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.no_image)
                        .error(R.drawable.no_image)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(8)))
                        .into(imgVehicle);
            } else {
                // Use placeholder
                imgVehicle.setImageResource(R.drawable.no_image);
            }
        }

        private double parsePrice(String priceStr) {
            try {
                // Remove any non-numeric characters except decimal point
                String cleanPrice = priceStr.replaceAll("[^0-9.]", "");
                return Double.parseDouble(cleanPrice);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }

    }
}
