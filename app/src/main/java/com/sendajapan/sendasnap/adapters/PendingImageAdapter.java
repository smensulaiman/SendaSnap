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

public class PendingImageAdapter extends RecyclerView.Adapter<PendingImageAdapter.PendingImageViewHolder> {

    private final List<String> pendingImagePaths;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public PendingImageAdapter(List<String> pendingImagePaths) {
        this.pendingImagePaths = pendingImagePaths;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    @NonNull
    @Override
    public PendingImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pending_image, parent, false);
        return new PendingImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PendingImageViewHolder holder, int position) {
        holder.bind(pendingImagePaths.get(position), position);
    }

    @Override
    public int getItemCount() {
        return pendingImagePaths.size();
    }

    class PendingImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgPendingPhoto;
        private ImageView btnDeletePending;

        public PendingImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPendingPhoto = itemView.findViewById(R.id.imgPendingPhoto);
            btnDeletePending = itemView.findViewById(R.id.btnDeletePending);
        }

        public void bind(String imagePath, int position) {
            // Load image from local path
            Glide.with(itemView.getContext())
                    .load(imagePath)
                    .placeholder(R.drawable.car_placeholder)
                    .error(R.drawable.car_placeholder)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(16)))
                    .into(imgPendingPhoto);

            btnDeletePending.setOnClickListener(v -> {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(position);
                }
            });
        }
    }
}
