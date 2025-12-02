package com.sendajapan.sendasnap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sendajapan.sendasnap.MyApplication;
import com.sendajapan.sendasnap.activities.schedule.ScheduleDetailActivity;
import com.sendajapan.sendasnap.adapters.NotificationAdapter;
import com.sendajapan.sendasnap.databinding.ActivityNotificationsBinding;
import com.sendajapan.sendasnap.models.Notification;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.NotificationHelper;

import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private ActivityNotificationsBinding binding;
    private NotificationAdapter adapter;
    private HapticFeedbackHelper hapticHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityNotificationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MyApplication.applyWindowInsets(binding.getRoot());

        initHelpers();
        setupToolbar();
        setupRecyclerView();
        loadNotifications();
    }

    private void initHelpers() {
        hapticHelper = HapticFeedbackHelper.getInstance(this);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> {
            hapticHelper.vibrateClick();
            finish();
        });
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter();
        adapter.setOnNotificationClickListener(notification -> {
            hapticHelper.vibrateClick();
            openTaskDetail(notification);
        });

        binding.recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        NotificationHelper.getAllNotifications(this, new NotificationHelper.NotificationsCallback() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                runOnUiThread(() -> {
                    adapter.setNotifications(notifications);
                    updateEmptyState(notifications.isEmpty());
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> updateEmptyState(true));
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            binding.recyclerViewNotifications.setVisibility(View.GONE);
            binding.textViewEmpty.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewNotifications.setVisibility(View.VISIBLE);
            binding.textViewEmpty.setVisibility(View.GONE);
        }
    }

    private void openTaskDetail(Notification notification) {
        if (!notification.isRead() && notification.getNotificationId() != null) {
            NotificationHelper.markNotificationAsRead(this, notification.getNotificationId());
        }

        Intent intent = new Intent(this, ScheduleDetailActivity.class);
        intent.putExtra("taskId", notification.getTaskId());
        startActivity(intent);
    }
}
