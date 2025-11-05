package com.sendajapan.sendasnap.activities.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.chip.Chip;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.databinding.ActivityAddTaskBinding;
import com.sendajapan.sendasnap.models.Task;
import com.sendajapan.sendasnap.models.TaskAttachment;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ScheduleDetailActivity extends AppCompatActivity {

    private ActivityAddTaskBinding binding;
    private HapticFeedbackHelper hapticHelper;
    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set status bar and navigation bar colors
        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_color, getTheme()));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.navigation_bar_color, getTheme()));

        // Handle system UI insets properly
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set status bar appearance
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
        }

        initHelpers();
        setupToolbar();
        loadTask();
        populateFields();
        makeFieldsReadOnly();
    }

    private void initHelpers() {
        hapticHelper = HapticFeedbackHelper.getInstance(this);
    }

    private void loadTask() {
        task = (Task) getIntent().getSerializableExtra("task");
        if (task == null) {
            finish();
            return;
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Task Details");
        }

        // Add edit icon to toolbar
        binding.toolbar.inflateMenu(R.menu.menu_schedule_detail);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit) {
                hapticHelper.vibrateClick();
                openEditMode();
                return true;
            }
            return false;
        });

        binding.toolbar.setNavigationOnClickListener(v -> {
            hapticHelper.vibrateClick();
            finish();
        });
    }

    private void openEditMode() {
        Intent intent = new Intent(this, AddScheduleActivity.class);
        intent.putExtra("task", task);
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            Task updatedTask = (Task) data.getSerializableExtra("task");
            if (updatedTask != null) {
                task = updatedTask;
                populateFields();
            }
        }
    }

    private void populateFields() {
        if (task == null) return;

        binding.editTextTitle.setText(task.getTitle());
        binding.editTextDescription.setText(task.getDescription());
        binding.editTextAssignee.setText(task.getAssignee());

        // Format and display date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(inputFormat.parse(task.getWorkDate()));
            binding.editTextDate.setText(displayFormat.format(dateCalendar.getTime()));
        } catch (Exception e) {
            binding.editTextDate.setText(task.getWorkDate());
        }

        // Format and display time
        binding.editTextTime.setText(task.getWorkTime());

        // Set status chip
        setStatusChip(task.getStatus());

        // Set priority chip if available
        if (task.getPriority() != null) {
            setPriorityChip(task.getPriority());
        }

        // Display attachments
        if (task.getAttachments() != null && !task.getAttachments().isEmpty()) {
            displayAttachments(task.getAttachments());
        } else {
            binding.layoutAttachedFiles.setVisibility(View.GONE);
            binding.textNoFiles.setVisibility(View.VISIBLE);
        }

        // Hide action buttons
        binding.buttonCancel.setVisibility(View.GONE);
        binding.buttonSave.setVisibility(View.GONE);
        binding.buttonAddFile.setVisibility(View.GONE);
    }

    private void setStatusChip(Task.TaskStatus status) {
        binding.chipGroupStatus.clearCheck();
        switch (status) {
            case RUNNING:
                Chip runningChip = binding.chipGroupStatus.findViewById(R.id.chipRunning);
                if (runningChip != null) {
                    runningChip.setChecked(true);
                }
                break;
            case PENDING:
                Chip pendingChip = binding.chipGroupStatus.findViewById(R.id.chipPending);
                if (pendingChip != null) {
                    pendingChip.setChecked(true);
                }
                break;
            case COMPLETED:
                Chip completedChip = binding.chipGroupStatus.findViewById(R.id.chipCompleted);
                if (completedChip != null) {
                    completedChip.setChecked(true);
                }
                break;
            case CANCELLED:
                Chip cancelledChip = binding.chipGroupStatus.findViewById(R.id.chipCancelled);
                if (cancelledChip != null) {
                    cancelledChip.setChecked(true);
                }
                break;
        }
    }

    private void setPriorityChip(Task.TaskPriority priority) {

        binding.chipGroupPriority.clearCheck();
        
        switch (priority) {
            case LOW:
                Chip lowChip = binding.chipGroupPriority.findViewById(R.id.chipPriorityLow);
                if (lowChip != null) {
                    lowChip.setChecked(true);
                }
                break;
            case NORMAL:
                Chip mediumChip = binding.chipGroupPriority.findViewById(R.id.chipPriorityNormal);
                if (mediumChip != null) {
                    mediumChip.setChecked(true);
                }
                break;
            case HIGH:
                Chip highChip = binding.chipGroupPriority.findViewById(R.id.chipPriorityHigh);
                if (highChip != null) {
                    highChip.setChecked(true);
                }
                break;
        }
    }

    private void displayAttachments(java.util.List<TaskAttachment> attachments) {
        binding.layoutAttachedFiles.setVisibility(View.VISIBLE);
        binding.textNoFiles.setVisibility(View.GONE);
        binding.layoutAttachedFiles.removeAllViews();

        for (TaskAttachment attachment : attachments) {
            addFileItemView(attachment);
        }
    }

    private void addFileItemView(TaskAttachment attachment) {
        LinearLayout fileItem = new LinearLayout(this);
        fileItem.setOrientation(LinearLayout.HORIZONTAL);
        fileItem.setGravity(android.view.Gravity.CENTER_VERTICAL);
        fileItem.setPadding(48, 48, 48, 48);
        fileItem.setBackgroundColor(getColor(R.color.surface));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = 32;
        fileItem.setLayoutParams(params);

        // File icon
        ImageView fileIcon = new ImageView(this);
        fileIcon.setLayoutParams(new LinearLayout.LayoutParams(96, 96));
        fileIcon.setImageResource(R.drawable.ic_file);
        fileIcon.setColorFilter(getColor(R.color.primary));
        fileIcon.setPadding(0, 0, 48, 0);
        fileItem.addView(fileIcon);

        // File info
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
        binding.layoutAttachedFiles.addView(fileItem);
    }

    private void makeFieldsReadOnly() {
        binding.editTextTitle.setEnabled(false);
        binding.editTextDescription.setEnabled(false);
        binding.editTextAssignee.setEnabled(false);
        binding.editTextDate.setEnabled(false);
        binding.editTextTime.setEnabled(false);

        // Disable all chips
        for (int i = 0; i < binding.chipGroupStatus.getChildCount(); i++) {
            View child = binding.chipGroupStatus.getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setClickable(false);
            }
        }
        
        // Disable priority chips if they exist
        if (binding.chipGroupPriority != null) {
            for (int i = 0; i < binding.chipGroupPriority.getChildCount(); i++) {
                View child = binding.chipGroupPriority.getChildAt(i);
                if (child instanceof Chip) {
                    ((Chip) child).setClickable(false);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

