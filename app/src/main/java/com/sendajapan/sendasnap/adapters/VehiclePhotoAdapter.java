package com.sendajapan.sendasnap.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.sendajapan.sendasnap.R;

import java.util.List;

public class VehiclePhotoAdapter extends RecyclerView.Adapter<VehiclePhotoAdapter.PhotoViewHolder> {

    private List<String> photos;

    public VehiclePhotoAdapter(List<String> photos) {
        this.photos = photos;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vehicle_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        holder.bind(photos.get(position));
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgPhoto;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
        }

        public void bind(String photoUrl) {
            if ("placeholder1".equals(photoUrl) || "placeholder2".equals(photoUrl)) {
                // Use placeholder image
                imgPhoto.setImageResource(R.drawable.ic_car_placeholder);
            } else {
                // Load actual image with Glide
                Glide.with(itemView.getContext())
                        .load(photoUrl)
                        .placeholder(R.drawable.ic_car_placeholder)
                        .error(R.drawable.ic_car_placeholder)
                        .apply(RequestOptions.bitmapTransform(new RoundedCorners(16)))
                        .into(imgPhoto);
            }
        }
    }
}
