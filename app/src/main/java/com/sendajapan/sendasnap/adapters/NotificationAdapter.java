package com.sendajapan.sendasnap.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.models.Notification;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter() {
        this.notifications = new ArrayList<>();
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewMessage;
        private TextView textViewTime;
        private View viewUnreadIndicator;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            viewUnreadIndicator = itemView.findViewById(R.id.viewUnreadIndicator);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNotificationClick(notifications.get(position));
                }
            });
        }

        void bind(Notification notification) {
            viewUnreadIndicator.setVisibility(notification.isRead() ? View.GONE : View.VISIBLE);

            String title = "New Task Assigned";
            if (notification.getTaskTitle() != null && !notification.getTaskTitle().isEmpty()) {
                title = "New Task Assigned: " + notification.getTaskTitle();
            }
            textViewTitle.setText(title);

            String message = "You have been assigned a new task";
            if (notification.getCreatorName() != null && !notification.getCreatorName().isEmpty()) {
                message = notification.getCreatorName() + " assigned you a task";
                if (notification.getTaskDescription() != null && !notification.getTaskDescription().isEmpty()) {
                    message += ": " + notification.getTaskDescription();
                }
            } else if (notification.getTaskDescription() != null && !notification.getTaskDescription().isEmpty()) {
                message = "Task: " + notification.getTaskDescription();
            }
            textViewMessage.setText(message);

            String timeText = formatTime(notification.getCreatedAt());
            textViewTime.setText(timeText);
        }

        private String formatTime(long timestamp) {
            long currentTime = System.currentTimeMillis();
            long diff = currentTime - timestamp;

            if (diff < 60000) {
                return "Just now";
            } else if (diff < 3600000) {
                long minutes = diff / 60000;
                return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
            } else if (diff < 86400000) {
                long hours = diff / 3600000;
                return hours + (hours == 1 ? " hour ago" : " hours ago");
            } else if (diff < 604800000) {
                long days = diff / 86400000;
                return days + (days == 1 ? " day ago" : " days ago");
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                return sdf.format(new Date(timestamp));
            }
        }
    }
}

