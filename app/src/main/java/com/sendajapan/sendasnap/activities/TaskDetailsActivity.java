package com.sendajapan.sendasnap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.databinding.ActivityTaskDetailsBinding;
import com.sendajapan.sendasnap.models.Task;
import com.sendajapan.sendasnap.models.TaskAttachment;
import com.sendajapan.sendasnap.activities.AddTaskActivity;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.MotionToastHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import www.sanju.motiontoast.MotionToast;

public class TaskDetailsActivity extends AppCompatActivity {

    private static final int EDIT_TASK_REQUEST_CODE = 1002;
    private ActivityTaskDetailsBinding binding;
    private Task task;
    private HapticFeedbackHelper hapticHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set status bar and navigation bar colors
        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_color, getTheme()));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.navigation_bar_color, getTheme()));

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
            task.setAssignee("John Doe"); // Set mock assignee
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

        // Display assignee
        String assignee = task.getAssignee();
        if (assignee == null || assignee.isEmpty()) {
            binding.textTaskAssignee.setText("Not assigned");
        } else {
            binding.textTaskAssignee.setText(assignee);
        }

        // Set status chip
        setStatusChip(task.getStatus());

        // Display file attachments
        displayAttachments();
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

    private void displayAttachments() {
        if (task.getAttachments() != null && !task.getAttachments().isEmpty()) {
            binding.layoutAttachments.setVisibility(View.VISIBLE);
            binding.textAttachmentCount.setText(task.getAttachments().size() + " file(s) attached");

            // Clear existing file items
            binding.layoutAttachedFiles.removeAllViews();

            // Add file items for each attachment
            for (TaskAttachment attachment : task.getAttachments()) {
                addAttachmentItem(attachment);
            }
        } else {
            binding.layoutAttachments.setVisibility(View.GONE);
        }
    }

    private void addAttachmentItem(TaskAttachment attachment) {
        // Create file item layout
        LinearLayout fileItem = new LinearLayout(this);
        fileItem.setOrientation(LinearLayout.HORIZONTAL);
        fileItem.setGravity(android.view.Gravity.CENTER_VERTICAL);
        fileItem.setPadding(48, 48, 48, 48); // 12dp padding
        fileItem.setBackgroundColor(getColor(R.color.surface));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 32; // 8dp margin
        fileItem.setLayoutParams(params);

        // Add file icon
        ImageView fileIcon = new ImageView(this);
        fileIcon.setLayoutParams(new LinearLayout.LayoutParams(96, 96)); // 24dp
        fileIcon.setImageResource(R.drawable.ic_file);
        fileIcon.setColorFilter(getColor(R.color.primary));
        fileIcon.setPadding(0, 0, 48, 0); // 12dp margin end
        fileItem.addView(fileIcon);

        // Add file info
        LinearLayout fileInfo = new LinearLayout(this);
        fileInfo.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams infoParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        fileInfo.setLayoutParams(infoParams);

        TextView fileName = new TextView(this);
        fileName.setText(attachment.getFileName());
        fileName.setTextAppearance(R.style.TextAppearance_Montserrat_BodyMedium);
        fileName.setTextColor(getColor(R.color.text_primary));
        fileName.setTextSize(12);
        fileName.setTypeface(null, android.graphics.Typeface.BOLD);
        fileInfo.addView(fileName);

        TextView fileSize = new TextView(this);
        fileSize.setText(attachment.getFormattedFileSize());
        fileSize.setTextAppearance(R.style.TextAppearance_Montserrat_BodySmall);
        fileSize.setTextColor(getColor(R.color.text_secondary));
        fileSize.setTextSize(10);
        fileInfo.addView(fileSize);

        fileItem.addView(fileInfo);

        // Add click listener to open file
        fileItem.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            openFile(attachment);
        });

        binding.layoutAttachedFiles.addView(fileItem);
    }

    private void openFile(TaskAttachment attachment) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(android.net.Uri.parse(attachment.getFilePath()), attachment.getMimeType());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open File"));
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        Intent intent = new Intent(this, AddTaskActivity.class);
        intent.putExtra("task", task);
        startActivityForResult(intent, EDIT_TASK_REQUEST_CODE);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_TASK_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Task updatedTask = (Task) data.getSerializableExtra("task");
            if (updatedTask != null) {
                // Update the current task with new data
                task.setTitle(updatedTask.getTitle());
                task.setDescription(updatedTask.getDescription());
                task.setWorkDate(updatedTask.getWorkDate());
                task.setWorkTime(updatedTask.getWorkTime());
                task.setStatus(updatedTask.getStatus());
                task.setAssignee(updatedTask.getAssignee());

                displayTask();
                MotionToastHelper.showSuccess(this, "Task Updated",
                        "Task has been updated successfully",
                        MotionToast.GRAVITY_BOTTOM, MotionToast.LONG_DURATION);
            }
        }
    }
}
