package com.sendajapan.sendasnap.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.sendajapan.sendasnap.models.TaskAttachment;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
    private List<TaskAttachment> attachments = new ArrayList<>();
    private static final int FILE_PICKER_REQUEST_CODE = 1001;

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

        binding.buttonAddFile.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            showFilePicker();
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

        // Load existing attachments
        if (editingTask.getAttachments() != null) {
            attachments.clear();
            attachments.addAll(editingTask.getAttachments());
            updateFileDisplay();
        }
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
            task.setAttachments(attachments);
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
            task.setAttachments(attachments);
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

    private void showFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_PICKER_REQUEST_CODE);
    }

    private void addMockFile() {
        // Create a mock file attachment
        String[] mockFiles = {
                "document.pdf", "image.jpg", "spreadsheet.xlsx", "presentation.pptx", "text.txt"
        };
        String[] mockSizes = { "2.4 MB", "1.2 MB", "856 KB", "3.1 MB", "45 KB" };

        int randomIndex = (int) (Math.random() * mockFiles.length);
        String fileName = mockFiles[randomIndex];
        String fileSize = mockSizes[randomIndex];

        TaskAttachment attachment = new TaskAttachment(
                String.valueOf(System.currentTimeMillis()),
                fileName,
                "/mock/path/" + fileName,
                getFileExtension(fileName),
                parseFileSize(fileSize),
                getMimeType(fileName));

        attachments.add(attachment);
        updateFileDisplay();
    }

    private String getFileExtension(String fileName) {
        if (fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    private long parseFileSize(String sizeStr) {
        // Simple parsing for demo - in real app, you'd get actual file size
        if (sizeStr.contains("MB")) {
            return (long) (Double.parseDouble(sizeStr.replace(" MB", "")) * 1024 * 1024);
        } else if (sizeStr.contains("KB")) {
            return (long) (Double.parseDouble(sizeStr.replace(" KB", "")) * 1024);
        } else {
            return Long.parseLong(sizeStr.replace(" B", ""));
        }
    }

    private String getMimeType(String fileName) {
        String extension = getFileExtension(fileName);
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "pptx":
                return "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            case "txt":
                return "text/plain";
            default:
                return "application/octet-stream";
        }
    }

    private void updateFileDisplay() {
        if (attachments.isEmpty()) {
            binding.layoutAttachedFiles.setVisibility(View.GONE);
            binding.textNoFiles.setVisibility(View.VISIBLE);
        } else {
            binding.layoutAttachedFiles.setVisibility(View.VISIBLE);
            binding.textNoFiles.setVisibility(View.GONE);

            // Clear existing file items (except template)
            binding.layoutAttachedFiles.removeAllViews();

            // Add file items for each attachment
            for (int i = 0; i < attachments.size(); i++) {
                TaskAttachment attachment = attachments.get(i);
                addFileItem(attachment, i);
            }
        }
    }

    private void addFileItem(TaskAttachment attachment, int index) {
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

        // Add remove button
        MaterialButton removeButton = new MaterialButton(this, null,
                com.google.android.material.R.style.Widget_Material3_Button_TextButton);
        removeButton.setIconResource(R.drawable.ic_delete);
        removeButton.setIconSize(56); // 14dp
        removeButton.setIconTint(getColorStateList(R.color.error));
        removeButton.setIconPadding(0);
        removeButton.setMinWidth(48); // 48dp minimum width for touch target
        removeButton.setMinHeight(48); // 48dp minimum height for touch target

        removeButton.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            attachments.remove(index);
            updateFileDisplay();
        });

        fileItem.addView(removeButton);
        binding.layoutAttachedFiles.addView(fileItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                addFileFromUri(fileUri);
            }
        }
    }

    private void addFileFromUri(Uri fileUri) {
        try {
            // Get file information
            String fileName = getFileName(fileUri);
            String mimeType = getContentResolver().getType(fileUri);
            long fileSize = getFileSize(fileUri);

            // Create attachment
            TaskAttachment attachment = new TaskAttachment(
                    String.valueOf(System.currentTimeMillis()),
                    fileName,
                    fileUri.toString(),
                    getFileExtension(fileName),
                    fileSize,
                    mimeType);

            attachments.add(attachment);
            updateFileDisplay();

            Toast.makeText(this, "File added: " + fileName, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error adding file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileName(Uri uri) {
        String fileName = null;
        try {
            String[] projection = { android.provider.MediaStore.MediaColumns.DISPLAY_NAME };
            android.database.Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (fileName == null) {
            fileName = "Unknown File";
        }

        return fileName;
    }

    private long getFileSize(Uri uri) {
        try {
            android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(android.provider.MediaStore.MediaColumns.SIZE);
                if (sizeIndex != -1) {
                    long size = cursor.getLong(sizeIndex);
                    cursor.close();
                    return size;
                }
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
