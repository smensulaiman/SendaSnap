package com.sendajapan.sendasnap.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.models.Vehicle;
import com.sendajapan.sendasnap.utils.CurrencyFormatter;

import java.util.ArrayList;
import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {

    private final List<Vehicle> vehicles;
    private final OnVehicleClickListener listener;

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
        private TextView txtVehicleId;
        private TextView txtBuyingPrice;
        private TextView txtPhotoCount;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);

            imgVehicle = itemView.findViewById(R.id.imgVehicle);
            txtVehicleName = itemView.findViewById(R.id.txtVehicleName);
            txtMakeModel = itemView.findViewById(R.id.txtMakeModel);
            txtChassisNumber = itemView.findViewById(R.id.txtChassisNumber);
            txtVehicleId = itemView.findViewById(R.id.txtVehicleId);
            txtBuyingPrice = itemView.findViewById(R.id.txtBuyingPrice);
            txtPhotoCount = itemView.findViewById(R.id.txtPhotoCount);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onVehicleClick(vehicles.get(position));
                }
            });
        }

        @SuppressLint("SetTextI18n")
        public void bind(Vehicle vehicle) {

            loadVehicleImage(vehicle);

            txtVehicleName.setText(vehicle.getDisplayName());
            txtMakeModel.setText(vehicle.getModel());
            txtChassisNumber.setText(vehicle.getChassisModel() + "-" + vehicle.getSerialNumber());
            txtVehicleId.setText(vehicle.getId());

            txtBuyingPrice.setText("￥" + CurrencyFormatter.formatBuyingPrice(vehicle.getBuyingPrice()));
            txtPhotoCount.setText(vehicle.getVehiclePhotos().size() +" Photo(s)");
        }

        private void loadVehicleImage(Vehicle vehicle) {
            imgVehicle.setImageResource(R.drawable.car_placeholder);

            if (vehicle.getVehiclePhotos() != null && !vehicle.getVehiclePhotos().isEmpty()) {
                String imageUrl = vehicle.getVehiclePhotos().get(0);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.car_placeholder)
                            .error(R.drawable.car_placeholder)
                            .centerCrop()
                            .into(imgVehicle);
                }
            }
        }

    }
}
