package com.sendajapan.sendasnap.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.models.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> tasks;
    private final OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardView;
        private final View statusIndicator;
        private final TextView textTaskTitle;
        private final TextView textTaskDescription;
        private final TextView textTaskTime;
        private final TextView badgeNew;
        private final Chip chipTaskStatus;
        private final ImageView imgAttachment;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = (MaterialCardView) itemView;
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            textTaskTitle = itemView.findViewById(R.id.textTaskTitle);
            textTaskDescription = itemView.findViewById(R.id.textTaskDescription);
            textTaskTime = itemView.findViewById(R.id.textTaskTime);
            badgeNew = itemView.findViewById(R.id.badgeNew);
            chipTaskStatus = itemView.findViewById(R.id.chipTaskStatus);
            imgAttachment = itemView.findViewById(R.id.imgAttachment);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(tasks.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Task task) {
            textTaskTitle.setText(task.getTitle());
            textTaskDescription.setText(task.getDescription());
            textTaskTime.setText(task.getWorkTime());

            setStatusColors(task.getStatus());

            if (task.isNew()) {
                badgeNew.setVisibility(View.VISIBLE);
            } else {
                badgeNew.setVisibility(View.GONE);
            }

            if (task.getAttachments() != null && !task.getAttachments().isEmpty()) {
                imgAttachment.setVisibility(View.VISIBLE);
            } else {
                imgAttachment.setVisibility(View.GONE);
            }
        }

        private void setStatusColors(Task.TaskStatus status) {
            Context context = itemView.getContext();

            switch (status) {
                case RUNNING:
                    // Card colors
                    cardView.setCardBackgroundColor(context.getColor(R.color.primary_light));
                    cardView.setStrokeColor(context.getColor(R.color.primary));
                    // Status indicator
                    statusIndicator.setBackgroundColor(context.getColor(R.color.primary));
                    // Chip
                    chipTaskStatus.setText("Running");
                    chipTaskStatus.setTextColor(context.getColorStateList(R.color.white));
                    chipTaskStatus.setChipBackgroundColor(context.getColorStateList(R.color.primary));
                    chipTaskStatus.setChipStrokeColor(context.getColorStateList(R.color.primary));
                    break;
                case PENDING:
                    // Card colors
                    cardView.setCardBackgroundColor(context.getColor(R.color.warning_light));
                    cardView.setStrokeColor(context.getColor(R.color.warning_dark));
                    // Status indicator
                    statusIndicator.setBackgroundColor(context.getColor(R.color.warning_dark));
                    // Chip
                    chipTaskStatus.setText("Pending");
                    chipTaskStatus.setTextColor(context.getColorStateList(R.color.black));
                    chipTaskStatus.setChipBackgroundColor(context.getColorStateList(R.color.warning_medium));
                    chipTaskStatus.setChipStrokeColor(context.getColorStateList(R.color.warning_dark));
                    break;
                case COMPLETED:
                    // Card colors
                    cardView.setCardBackgroundColor(context.getColor(R.color.success_light));
                    cardView.setStrokeColor(context.getColor(R.color.success_dark));
                    // Status indicator
                    statusIndicator.setBackgroundColor(context.getColor(R.color.success_dark));
                    // Chip
                    chipTaskStatus.setText("Completed");
                    chipTaskStatus.setChipBackgroundColor(context.getColorStateList(R.color.success_light));
                    chipTaskStatus.setChipStrokeColor(context.getColorStateList(R.color.success_dark));
                    break;
                case CANCELLED:
                    // Card colors
                    cardView.setCardBackgroundColor(context.getColor(R.color.error_light));
                    cardView.setStrokeColor(context.getColor(R.color.error_dark));
                    // Status indicator
                    statusIndicator.setBackgroundColor(context.getColor(R.color.error_dark));
                    // Chip
                    chipTaskStatus.setText("Cancelled");
                    chipTaskStatus.setChipBackgroundColor(context.getColorStateList(R.color.error_light));
                    chipTaskStatus.setChipStrokeColor(context.getColorStateList(R.color.error_dark));
                    break;
            }
        }
    }
}
