package com.sendajapan.sendasnap.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.databinding.ActivityTaskDetailsBinding;
import com.sendajapan.sendasnap.models.Task;
import com.sendajapan.sendasnap.ui.AddTaskBottomSheet;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.MotionToastHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import www.sanju.motiontoast.MotionToast;

public class TaskDetailsActivity extends AppCompatActivity {

    private ActivityTaskDetailsBinding binding;
    private Task task;
    private HapticFeedbackHelper hapticHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ensure light status bar (dark icons)
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(true);
            controller.setAppearanceLightNavigationBars(true);
        }

        initHelpers();
        setupToolbar();
        loadTask();
        setupClickListeners();
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

    private void loadTask() {
        task = (Task) getIntent().getSerializableExtra("task");
        if (task == null) {
            // Create a mock task for demonstration
            task = new Task("1", "Vehicle Inspection", "Complete safety inspection for Toyota Camry",
                    "2024-12-15", "09:00", Task.TaskStatus.RUNNING);
        }

        displayTask();
    }

    private void displayTask() {
        binding.textTaskTitle.setText(task.getTitle());
        binding.textTaskDescription.setText(task.getDescription());

        // Format and display date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(task.getWorkDate());
            binding.textTaskDate.setText(outputFormat.format(date));
        } catch (Exception e) {
            binding.textTaskDate.setText(task.getWorkDate());
        }

        binding.textTaskTime.setText(task.getWorkTime());

        // Set status chip
        setStatusChip(task.getStatus());
    }

    private void setStatusChip(Task.TaskStatus status) {
        binding.chipTaskStatus.setText(status.toString());

        switch (status) {
            case RUNNING:
                binding.chipTaskStatus.setChipBackgroundColor(getColorStateList(R.color.status_running_light));
                binding.chipTaskStatus.setChipStrokeColor(getColorStateList(R.color.status_running));
                break;
            case PENDING:
                binding.chipTaskStatus.setChipBackgroundColor(getColorStateList(R.color.status_pending_light));
                binding.chipTaskStatus.setChipStrokeColor(getColorStateList(R.color.status_pending));
                break;
            case COMPLETED:
                binding.chipTaskStatus.setChipBackgroundColor(getColorStateList(R.color.status_completed_light));
                binding.chipTaskStatus.setChipStrokeColor(getColorStateList(R.color.status_completed));
                break;
            case CANCELLED:
                binding.chipTaskStatus.setChipBackgroundColor(getColorStateList(R.color.status_cancelled_light));
                binding.chipTaskStatus.setChipStrokeColor(getColorStateList(R.color.status_cancelled));
                break;
        }
    }

    private void setupClickListeners() {
        binding.buttonChangeStatus.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            showStatusChangeDialog();
        });

        binding.buttonEdit.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            editTask();
        });

        binding.buttonDelete.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            showDeleteDialog();
        });
    }

    private void showStatusChangeDialog() {
        String[] statusOptions = { "Running", "Pending", "Completed", "Cancelled" };
        int currentIndex = 0;

        switch (task.getStatus()) {
            case RUNNING:
                currentIndex = 0;
                break;
            case PENDING:
                currentIndex = 1;
                break;
            case COMPLETED:
                currentIndex = 2;
                break;
            case CANCELLED:
                currentIndex = 3;
                break;
        }

        new AlertDialog.Builder(this)
                .setTitle("Change Status")
                .setSingleChoiceItems(statusOptions, currentIndex, null)
                .setPositiveButton("Change", (dialog, which) -> {
                    int selectedIndex = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    Task.TaskStatus newStatus = Task.TaskStatus.values()[selectedIndex];
                    task.setStatus(newStatus);
                    setStatusChip(newStatus);

                    MotionToastHelper.showSuccess(this, "Status Updated",
                            "Task status has been changed to " + newStatus.toString(),
                            MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void editTask() {
        AddTaskBottomSheet bottomSheet = new AddTaskBottomSheet();
        bottomSheet.setOnTaskSavedListener(updatedTask -> {
            // Update the current task with new data
            task.setTitle(updatedTask.getTitle());
            task.setDescription(updatedTask.getDescription());
            task.setWorkDate(updatedTask.getWorkDate());
            task.setWorkTime(updatedTask.getWorkTime());
            task.setStatus(updatedTask.getStatus());

            displayTask();
            MotionToastHelper.showSuccess(this, "Task Updated",
                    "Task has been updated successfully",
                    MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);
        });
        bottomSheet.show(getSupportFragmentManager(), "EditTaskBottomSheet");
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // TODO: Implement actual deletion logic
                    MotionToastHelper.showSuccess(this, "Task Deleted",
                            "Task has been deleted successfully",
                            MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            hapticHelper.vibrateClick();
            editTask();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
