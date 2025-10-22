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
        private TextView txtChassisNumber;
        private TextView txtYear;
        private TextView txtColor;
        private TextView txtSearchDate;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);

            imgVehicle = itemView.findViewById(R.id.imgVehicle);
            txtVehicleName = itemView.findViewById(R.id.txtVehicleName);
            txtChassisNumber = itemView.findViewById(R.id.txtChassisNumber);
            txtYear = itemView.findViewById(R.id.txtYear);
            txtColor = itemView.findViewById(R.id.txtColor);
            txtSearchDate = itemView.findViewById(R.id.txtSearchDate);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onVehicleClick(vehicles.get(position));
                }
            });
        }

        public void bind(Vehicle vehicle) {
            // Set vehicle name
            txtVehicleName.setText(vehicle.getDisplayName());

            // Set chassis number
            txtChassisNumber.setText("Chassis: " + vehicle.getSerialNumber());

            // Set year and color
            txtYear.setText(vehicle.getYear());
            txtColor.setText(vehicle.getColor());

            // Set search date (mock - in real app this would be actual search time)
            txtSearchDate.setText("Searched " + getTimeAgo());

            // Load vehicle image
            loadVehicleImage(vehicle);
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

        private String getTimeAgo() {
            // Mock time ago - in real app this would be calculated from actual search time
            String[] timeOptions = { "1 hour ago", "2 hours ago", "1 day ago", "2 days ago", "1 week ago" };
            return timeOptions[(int) (Math.random() * timeOptions.length)];
        }
    }
}
