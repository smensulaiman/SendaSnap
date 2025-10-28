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

    private List<Task> tasks;
    private OnTaskClickListener listener;

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
        private MaterialCardView cardView;
        private View statusIndicator;
        private TextView textTaskTitle;
        private TextView textTaskDescription;
        private TextView textTaskTime;
        private TextView badgeNew;
        private Chip chipTaskStatus;
        private ImageView imgAttachment;

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

            // Set status colors and text
            setStatusColors(task.getStatus());

            // Show NEW badge if task is new
            if (task.isNew()) {
                badgeNew.setVisibility(View.VISIBLE);
            } else {
                badgeNew.setVisibility(View.GONE);
            }

            // Show attachment icon if task has attachments
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
                    cardView.setCardBackgroundColor(context.getColor(R.color.status_running_light));
                    cardView.setStrokeColor(context.getColor(R.color.status_running));
                    // Status indicator
                    statusIndicator.setBackgroundColor(context.getColor(R.color.status_running));
                    // Chip
                    chipTaskStatus.setText("Running");
                    chipTaskStatus.setChipBackgroundColor(context.getColorStateList(R.color.status_running_light));
                    chipTaskStatus.setChipStrokeColor(context.getColorStateList(R.color.status_running));
                    break;
                case PENDING:
                    // Card colors
                    cardView.setCardBackgroundColor(context.getColor(R.color.status_pending_light));
                    cardView.setStrokeColor(context.getColor(R.color.status_pending));
                    // Status indicator
                    statusIndicator.setBackgroundColor(context.getColor(R.color.status_pending));
                    // Chip
                    chipTaskStatus.setText("Pending");
                    chipTaskStatus.setChipBackgroundColor(context.getColorStateList(R.color.status_pending_light));
                    chipTaskStatus.setChipStrokeColor(context.getColorStateList(R.color.status_pending));
                    break;
                case COMPLETED:
                    // Card colors
                    cardView.setCardBackgroundColor(context.getColor(R.color.status_completed_light));
                    cardView.setStrokeColor(context.getColor(R.color.status_completed));
                    // Status indicator
                    statusIndicator.setBackgroundColor(context.getColor(R.color.status_completed));
                    // Chip
                    chipTaskStatus.setText("Completed");
                    chipTaskStatus.setChipBackgroundColor(context.getColorStateList(R.color.status_completed_light));
                    chipTaskStatus.setChipStrokeColor(context.getColorStateList(R.color.status_completed));
                    break;
                case CANCELLED:
                    // Card colors
                    cardView.setCardBackgroundColor(context.getColor(R.color.status_cancelled_light));
                    cardView.setStrokeColor(context.getColor(R.color.status_cancelled));
                    // Status indicator
                    statusIndicator.setBackgroundColor(context.getColor(R.color.status_cancelled));
                    // Chip
                    chipTaskStatus.setText("Cancelled");
                    chipTaskStatus.setChipBackgroundColor(context.getColorStateList(R.color.status_cancelled_light));
                    chipTaskStatus.setChipStrokeColor(context.getColorStateList(R.color.status_cancelled));
                    break;
            }
        }
    }
}
