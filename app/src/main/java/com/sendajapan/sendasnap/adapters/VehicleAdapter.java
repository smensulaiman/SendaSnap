package com.sendajapan.sendasnap.adapters;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
        private TextView txtYear;
        private TextView txtColor;
        private TextView txtSearchDate;
        private TextView txtBuyingPrice;
        private TextView txtSellingPrice;
        private TextView txtProfit;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);

            imgVehicle = itemView.findViewById(R.id.imgVehicle);
            txtVehicleName = itemView.findViewById(R.id.txtVehicleName);
            txtMakeModel = itemView.findViewById(R.id.txtMakeModel);
            txtChassisNumber = itemView.findViewById(R.id.txtChassisNumber);
            txtYear = itemView.findViewById(R.id.txtYear);
            txtColor = itemView.findViewById(R.id.txtColor);
            txtSearchDate = itemView.findViewById(R.id.txtSearchDate);
            txtBuyingPrice = itemView.findViewById(R.id.txtBuyingPrice);
            txtSellingPrice = itemView.findViewById(R.id.txtSellingPrice);
            txtProfit = itemView.findViewById(R.id.txtProfit);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onVehicleClick(vehicles.get(position));
                }
            });
        }

        public void bind(Vehicle vehicle) {
            // Load vehicle image
            loadVehicleImage(vehicle);

            // Set vehicle name
            txtVehicleName.setText(vehicle.getDisplayName());

            // Set make and model
            txtMakeModel.setText(vehicle.getMake() + " " + vehicle.getModel() + " " + vehicle.getYear());

            // Set chassis number
            txtChassisNumber.setText("Chassis: " + vehicle.getSerialNumber());

            // Set year and color
            txtYear.setText(vehicle.getYear());
            txtColor.setText(vehicle.getColor());

            // Set financial information
            String buyingPriceStr = vehicle.getBuyingPrice() != null ? vehicle.getBuyingPrice() : "0";
            double buyingPrice = parsePrice(buyingPriceStr);
            txtBuyingPrice.setText("Buy: $" + formatPrice(buyingPrice));

            // Mock selling price (in real app this would come from API)
            double sellingPrice = buyingPrice * 1.2; // 20% markup
            txtSellingPrice.setText("Sell: $" + formatPrice(sellingPrice));

            // Calculate and set profit
            double profit = sellingPrice - buyingPrice;
            txtProfit.setText("Profit: $" + formatPrice(profit));

            // Set search date (mock - in real app this would be actual search time)
            txtSearchDate.setText("Searched " + getTimeAgo());
        }

        private void loadVehicleImage(Vehicle vehicle) {
            if (vehicle.getVehiclePhotos() != null && !vehicle.getVehiclePhotos().isEmpty()) {
                // Load first photo
                String imageUrl = vehicle.getVehiclePhotos().get(0);
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_car_placeholder)
                        .error(R.drawable.ic_car_placeholder)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(8)))
                        .into(imgVehicle);
            } else {
                // Use placeholder
                imgVehicle.setImageResource(R.drawable.ic_car_placeholder);
            }
        }

        private String formatPrice(double price) {
            return String.format("%,.0f", price);
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

        private String getTimeAgo() {
            // Mock time ago - in real app this would be calculated from actual search time
            String[] timeOptions = { "1 hour ago", "2 hours ago", "1 day ago", "2 days ago", "1 week ago" };
            return timeOptions[(int) (Math.random() * timeOptions.length)];
        }
    }
}
