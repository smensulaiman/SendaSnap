package com.sendajapan.sendasnap.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.databinding.ActivityAddTaskBinding;
import com.sendajapan.sendasnap.models.Task;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private ActivityAddTaskBinding binding;
    private HapticFeedbackHelper hapticHelper;
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar selectedTime = Calendar.getInstance();
    private String[] assigneeOptions = { "John Doe", "Jane Smith", "Mike Johnson", "Sarah Wilson", "David Brown",
            "Lisa Davis" };
    private Task editingTask; // Task being edited
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handle system UI insets properly
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set status bar appearance
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false); // Dark icons on light background
        }

        initHelpers();
        setupToolbar();
        checkEditMode();
        setupListeners();
        setupInitialValues();
    }

    private void initHelpers() {
        hapticHelper = HapticFeedbackHelper.getInstance(this);
    }

    private void checkEditMode() {
        // Check if we're editing an existing task
        editingTask = (Task) getIntent().getSerializableExtra("task");
        if (editingTask != null) {
            isEditMode = true;
            // Update toolbar title and button text for edit mode
            binding.toolbar.setTitle("Edit Task");
            binding.buttonSave.setText("Update Task");
        }
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

    private void setupListeners() {
        binding.editTextDate.setOnClickListener(v -> showDatePicker());
        binding.editTextTime.setOnClickListener(v -> showTimePicker());

        binding.buttonCancel.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            finish();
        });

        binding.buttonSave.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            saveTask();
        });
    }

    private void setupInitialValues() {
        if (isEditMode && editingTask != null) {
            // Populate fields with existing task data
            populateFieldsForEdit();
        } else {
            // Set default values for new task
            setDefaultValues();
        }

        // Set up assignee AutoCompleteTextView
        ArrayAdapter<String> assigneeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, assigneeOptions);
        binding.editTextAssignee.setAdapter(assigneeAdapter);

        // Add haptic feedback to form interactions
        binding.editTextTitle.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                hapticHelper.vibrateClick();
        });

        binding.editTextDescription.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                hapticHelper.vibrateClick();
        });
    }

    private void populateFieldsForEdit() {
        // Populate title and description
        binding.editTextTitle.setText(editingTask.getTitle());
        binding.editTextDescription.setText(editingTask.getDescription());
        binding.editTextAssignee.setText(editingTask.getAssignee());

        // Parse and set date
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            selectedDate.setTime(inputFormat.parse(editingTask.getWorkDate()));
            binding.editTextDate.setText(displayFormat.format(selectedDate.getTime()));
        } catch (Exception e) {
            // Fallback to default date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            binding.editTextDate.setText(dateFormat.format(selectedDate.getTime()));
        }

        // Parse and set time
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            selectedTime.setTime(inputFormat.parse(editingTask.getWorkTime()));
            binding.editTextTime.setText(displayFormat.format(selectedTime.getTime()));
        } catch (Exception e) {
            // Fallback to default time
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            binding.editTextTime.setText(timeFormat.format(selectedTime.getTime()));
        }

        // Set status chip
        setStatusChip(editingTask.getStatus());
    }

    private void setDefaultValues() {
        // Set default date to today
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        binding.editTextDate.setText(dateFormat.format(selectedDate.getTime()));

        // Set default time to current time
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        binding.editTextTime.setText(timeFormat.format(selectedTime.getTime()));

        // Set default status to Running
        setStatusChip(Task.TaskStatus.RUNNING);
    }

    private void setStatusChip(Task.TaskStatus status) {
        // Clear all chips first
        binding.chipGroupStatus.clearCheck();

        // Set the appropriate chip based on status
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

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    binding.editTextDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    binding.editTextTime.setText(timeFormat.format(selectedTime.getTime()));
                },
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void saveTask() {
        String title = binding.editTextTitle.getText().toString().trim();
        String description = binding.editTextDescription.getText().toString().trim();
        String assignee = binding.editTextAssignee.getText().toString().trim();

        if (title.isEmpty()) {
            binding.editTextTitle.setError("Title is required");
            return;
        }

        // Get selected status
        Task.TaskStatus status = getSelectedStatus();

        // Format date and time
        String workDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());
        String workTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime.getTime());

        Task task;
        if (isEditMode && editingTask != null) {
            // Update existing task
            task = editingTask;
            task.setTitle(title);
            task.setDescription(description);
            task.setWorkDate(workDate);
            task.setWorkTime(workTime);
            task.setStatus(status);
            task.setAssignee(assignee);
            task.setUpdatedAt(System.currentTimeMillis());
        } else {
            // Create new task
            task = new Task(
                    String.valueOf(System.currentTimeMillis()), // Simple ID generation
                    title,
                    description,
                    workDate,
                    workTime,
                    status);
            task.setAssignee(assignee);
        }

        // Return result to calling activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("task", task);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private Task.TaskStatus getSelectedStatus() {
        int checkedId = binding.chipGroupStatus.getCheckedChipId();

        if (checkedId == R.id.chipRunning) {
            return Task.TaskStatus.RUNNING;
        } else if (checkedId == R.id.chipPending) {
            return Task.TaskStatus.PENDING;
        } else if (checkedId == R.id.chipCompleted) {
            return Task.TaskStatus.COMPLETED;
        } else if (checkedId == R.id.chipCancelled) {
            return Task.TaskStatus.CANCELLED;
        }

        return Task.TaskStatus.RUNNING; // Default
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
