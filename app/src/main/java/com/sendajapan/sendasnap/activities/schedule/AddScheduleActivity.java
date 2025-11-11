package com.sendajapan.sendasnap.activities.schedule;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.adapters.TaskAttachmentAdapter;
import com.sendajapan.sendasnap.adapters.UserDropdownAdapter;
import com.sendajapan.sendasnap.databinding.ActivityAddTaskBinding;
import com.sendajapan.sendasnap.models.Task;
import com.sendajapan.sendasnap.models.TaskAttachment;
import com.sendajapan.sendasnap.models.UserData;
import com.sendajapan.sendasnap.networking.ApiCallback;
import com.sendajapan.sendasnap.networking.ApiManager;
import com.sendajapan.sendasnap.utils.AlarmHelper;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.SharedPrefsManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AddScheduleActivity extends AppCompatActivity {

    private static final int FILE_PICKER_REQUEST_CODE = 1001;

    private ActivityAddTaskBinding binding;
    private HapticFeedbackHelper hapticHelper;
    private SharedPrefsManager prefsManager;
    private UserData currentUser;
    private ApiManager apiManager;

    private final List<UserData> users = new ArrayList<>();
    private final List<TaskAttachment> attachments = new ArrayList<>();
    private final Calendar selectedDate = Calendar.getInstance();
    private final Calendar selectedTime = Calendar.getInstance();
    private final List<String> selectedAssignees = new ArrayList<>();

    private Task editingTask;
    private boolean isEditMode = false;
    private boolean isRestrictedEditMode = false;
    private PopupWindow userDropdownPopup;
    private TaskAttachmentAdapter attachmentAdapter;
    private boolean isShowingDropdown = false;
    private UserDropdownAdapter userDropdownAdapter;

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
        checkEditMode();
        checkUserRole();
        setupRecyclerView();
        setupListeners();
        setupInitialValues();
    }

    private void initHelpers() {
        hapticHelper = HapticFeedbackHelper.getInstance(this);
        prefsManager = SharedPrefsManager.getInstance(this);
        currentUser = prefsManager.getUser();
        apiManager = ApiManager.getInstance(this);
    }

    private void checkUserRole() {
        if (currentUser != null && isEditMode) {
            String role = currentUser.getRole();
            // Check if user is not admin or manager
            if (role == null || (!role.equalsIgnoreCase("admin") && !role.equalsIgnoreCase("manager"))) {
                isRestrictedEditMode = true;
                // Disable all fields except status
                disableFieldsExceptStatus();
            }
        }
    }

    private void checkEditMode() {
        // Check if we're editing an existing task
        editingTask = (Task) getIntent().getSerializableExtra("task");
        if (editingTask != null) {
            isEditMode = true;
            // Update toolbar title and button text for edit mode
            binding.toolbar.setTitle("Edit Schedule");
            binding.buttonSave.setText("Update");
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

    private void setupRecyclerView() {
        attachmentAdapter = new TaskAttachmentAdapter(attachments, true);
        attachmentAdapter.setOnAttachmentRemoveListener((position, attachment) -> {
            hapticHelper.vibrateClick();
            attachments.remove(position);
            attachmentAdapter.notifyItemRemoved(position);
            attachmentAdapter.notifyItemRangeChanged(position, attachments.size());
            updateFileDisplay();
        });
        binding.recyclerViewAttachments.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        binding.recyclerViewAttachments.setAdapter(attachmentAdapter);
    }

    private void setupListeners() {
        binding.editTextDate.setOnClickListener(v -> showDatePicker());
        binding.editTextTime.setOnClickListener(v -> showTimePicker());

        // Handle assignee field click to show custom dropdown
        binding.editTextAssignee.setOnClickListener(v -> {
            if (!isShowingDropdown) {
                hapticHelper.vibrateClick();
                // Hide keyboard
                android.view.inputmethod.InputMethodManager imm = (InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                showUserDropdown();
            }
        });
        
        // Also handle focus to show dropdown
        binding.editTextAssignee.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && !isShowingDropdown) {
                hapticHelper.vibrateClick();
                // Hide keyboard
                android.view.inputmethod.InputMethodManager imm = (InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                // Post to avoid immediate dismissal
                v.post(() -> showUserDropdown());
            }
        });

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
            populateFieldsForEdit();
        } else {
            setDefaultValues();
        }

        // Fetch users from API
        fetchUsers();

        binding.editTextTitle.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                hapticHelper.vibrateClick();
        });

        binding.editTextDescription.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                hapticHelper.vibrateClick();
        });
    }

    private void fetchUsers() {
        apiManager.getUsers(new ApiCallback<List<UserData>>() {
            @Override
            public void onSuccess(List<UserData> userList) {
                users.clear();
                users.addAll(userList);
                
                // If in edit mode, update selected users in dropdown
                if (isEditMode && editingTask != null) {
                    if (userDropdownAdapter != null) {
                        userDropdownAdapter.setSelectedUsers(selectedAssignees);
                    }
                }
            }

            @Override
            public void onError(String message, int errorCode) {
                Toast.makeText(AddScheduleActivity.this, "Failed to load users: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUserDropdown() {
        if (users.isEmpty()) {
            Toast.makeText(this, "No users available", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userDropdownPopup != null && userDropdownPopup.isShowing()) {
            userDropdownPopup.dismiss();
            isShowingDropdown = false;
            return;
        }

        isShowingDropdown = true;

        LayoutInflater inflater = LayoutInflater.from(AddScheduleActivity.this);
        View popupView = inflater.inflate(R.layout.dialog_user_dropdown, null);

        RecyclerView recyclerView = popupView.findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(AddScheduleActivity.this));

        userDropdownAdapter = new UserDropdownAdapter(users);
        userDropdownAdapter.setSelectedUsers(selectedAssignees);
        userDropdownAdapter.setOnUserClickListener((user, isSelected) -> {
            hapticHelper.vibrateClick();
            if (isSelected) {
                if (!selectedAssignees.contains(user.getName())) {
                    selectedAssignees.add(user.getName());
                }
            } else {
                selectedAssignees.remove(user.getName());
            }

            updateAssigneeChips();
        });

        recyclerView.setAdapter(userDropdownAdapter);

        binding.editTextAssignee.post(() -> {
            int width = binding.editTextAssignee.getWidth();

            userDropdownPopup = new PopupWindow(
                    popupView,
                    width,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    true
            );

            userDropdownPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            userDropdownPopup.setElevation(8f);
            userDropdownPopup.setOutsideTouchable(true);
            userDropdownPopup.setFocusable(false);

            userDropdownPopup.showAsDropDown(binding.editTextAssignee, 0, 0);

            userDropdownPopup.setOnDismissListener(() -> {
                isShowingDropdown = false;
                binding.editTextAssignee.clearFocus();
            });
        });
    }

    private void updateAssigneeChips() {
        binding.chipGroupAssignees.removeAllViews();
        
        if (selectedAssignees.isEmpty()) {
            binding.editTextAssignee.setText("");
            binding.chipGroupAssignees.setVisibility(View.GONE);
        } else {
            binding.editTextAssignee.setText(selectedAssignees.size() + " assignee(s) selected");
            binding.chipGroupAssignees.setVisibility(View.VISIBLE);
            
            for (String assigneeName : selectedAssignees) {
                Chip chip = new Chip(this);
                chip.setText(assigneeName);
                chip.setCloseIconVisible(true);
                chip.setCloseIconTint(getColorStateList(R.color.text_primary));
                chip.setOnCloseIconClickListener(v -> {
                    hapticHelper.vibrateClick();
                    selectedAssignees.remove(assigneeName);
                    updateAssigneeChips();
                    if (userDropdownAdapter != null) {
                        userDropdownAdapter.setSelectedUsers(selectedAssignees);
                    }
                });
                binding.chipGroupAssignees.addView(chip);
            }
        }
    }

    private void populateFieldsForEdit() {
        binding.editTextTitle.setText(editingTask.getTitle());
        binding.editTextDescription.setText(editingTask.getDescription());
        
        // Load assignees
        selectedAssignees.clear();
        if (editingTask.getAssignees() != null && !editingTask.getAssignees().isEmpty()) {
            selectedAssignees.addAll(editingTask.getAssignees());
        } else if (editingTask.getAssignee() != null && !editingTask.getAssignee().isEmpty()) {
            // Migrate from old single assignee field
            selectedAssignees.add(editingTask.getAssignee());
        }

        updateAssigneeChips();

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            selectedDate.setTime(inputFormat.parse(editingTask.getWorkDate()));
            binding.editTextDate.setText(displayFormat.format(selectedDate.getTime()));
        } catch (Exception e) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            binding.editTextDate.setText(dateFormat.format(selectedDate.getTime()));
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            selectedTime.setTime(inputFormat.parse(editingTask.getWorkTime()));
            binding.editTextTime.setText(displayFormat.format(selectedTime.getTime()));
        } catch (Exception e) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            binding.editTextTime.setText(timeFormat.format(selectedTime.getTime()));
        }

        setStatusChip(editingTask.getStatus());

        // Set priority chip
        if (editingTask.getPriority() != null) {
            setPriorityChip(editingTask.getPriority());
        } else {
            setPriorityChip(Task.TaskPriority.NORMAL);
        }

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

        // Set default priority to Medium
        setPriorityChip(Task.TaskPriority.NORMAL);
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

    private void setPriorityChip(Task.TaskPriority priority) {
        // Clear all chips first
        binding.chipGroupPriority.clearCheck();

        // Set the appropriate chip based on priority
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

    private void disableFieldsExceptStatus() {
        binding.editTextTitle.setEnabled(false);
        binding.editTextDescription.setEnabled(false);
        binding.editTextAssignee.setEnabled(false);
        binding.editTextDate.setEnabled(false);
        binding.editTextTime.setEnabled(false);
        binding.buttonAddFile.setVisibility(View.GONE);

        // Disable priority chips
        for (int i = 0; i < binding.chipGroupPriority.getChildCount(); i++) {
            View child = binding.chipGroupPriority.getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setClickable(false);
                ((Chip) child).setEnabled(false);
            }
        }

        // Keep status chips enabled
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.CustomDatePickerDialog,
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    binding.editTextDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.setOnShowListener(dialog -> {
            datePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.primary));

            datePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.error_dark));
        });

        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                R.style.CustomTimePickerDialog,
                (view, hourOfDay, minute) -> {
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm", Locale.getDefault());
                    binding.editTextTime.setText(timeFormat.format(selectedTime.getTime()));
                },
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE),
                false);

        timePickerDialog.setOnShowListener(dialog -> {
            timePickerDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.primary));

            timePickerDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setTextColor(ContextCompat.getColor(this, R.color.error_dark));
        });

        timePickerDialog.show();
    }

    private void saveTask() {
        String title = Objects.requireNonNull(binding.editTextTitle.getText()).toString().trim();
        String description = Objects.requireNonNull(binding.editTextDescription.getText()).toString().trim();

        if (title.isEmpty()) {
            binding.editTextTitle.setError("Title is required");
            return;
        }

        // Get selected status
        Task.TaskStatus status = getSelectedStatus();

        // Get selected priority
        Task.TaskPriority priority = getSelectedPriority();

        // Format date and time
        String workDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());
        String workTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime.getTime());

        Task task;
        if (isEditMode && editingTask != null) {
            // Update existing task
            task = editingTask;
            if (!isRestrictedEditMode) {
                // Only update fields if user has permission
                task.setTitle(title);
                task.setDescription(description);
                task.setWorkDate(workDate);
                task.setWorkTime(workTime);
                task.setAssignees(new ArrayList<>(selectedAssignees));
                task.setAttachments(attachments);
                task.setPriority(priority);
            }
            // Always allow status update
            task.setStatus(status);
            task.setUpdatedAt(System.currentTimeMillis());
        } else {
            // Create new task
            task = new Task(
                    String.valueOf(System.currentTimeMillis()),
                    title,
                    description,
                    workDate,
                    workTime,
                    status);
            task.setAssignees(new ArrayList<>(selectedAssignees));
            task.setPriority(priority);
            task.setAttachments(attachments);
        }

        // Set alarm for the task
        AlarmHelper.setTaskAlarm(this, task);

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

        return Task.TaskStatus.RUNNING;
    }

    private Task.TaskPriority getSelectedPriority() {
        int checkedId = binding.chipGroupPriority.getCheckedChipId();

        if (checkedId == R.id.chipPriorityLow) {
            return Task.TaskPriority.LOW;
        } else if (checkedId == R.id.chipPriorityNormal) {
            return Task.TaskPriority.NORMAL;
        } else if (checkedId == R.id.chipPriorityHigh) {
            return Task.TaskPriority.HIGH;
        }

        return Task.TaskPriority.NORMAL;
    }

    private void showFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select File"), FILE_PICKER_REQUEST_CODE);
    }

    private String getFileExtension(String fileName) {
        if (fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    private void updateFileDisplay() {
        if (attachments.isEmpty()) {
            binding.recyclerViewAttachments.setVisibility(View.GONE);
            binding.textNoFiles.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerViewAttachments.setVisibility(View.VISIBLE);
            binding.textNoFiles.setVisibility(View.GONE);
            attachmentAdapter.notifyDataSetChanged();
        }
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
            attachmentAdapter.notifyItemInserted(attachments.size() - 1);

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
