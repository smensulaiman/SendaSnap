package com.sendajapan.sendasnap.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.models.Task;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskBottomSheet extends BottomSheetDialogFragment {

    private TextInputEditText editTextTitle;
    private TextInputEditText editTextDescription;
    private TextInputEditText editTextDate;
    private TextInputEditText editTextTime;
    private ChipGroup chipGroupStatus;
    private MaterialButton buttonCancel;
    private MaterialButton buttonSave;

    private OnTaskSavedListener listener;
    private Calendar selectedDate = Calendar.getInstance();
    private Calendar selectedTime = Calendar.getInstance();

    public interface OnTaskSavedListener {
        void onTaskSaved(Task task);
    }

    public void setOnTaskSavedListener(OnTaskSavedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_add_task, container, false);

        initViews(view);
        setupListeners();
        setupInitialValues();

        return view;
    }

    private void initViews(View view) {
        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextDescription = view.findViewById(R.id.editTextDescription);
        editTextDate = view.findViewById(R.id.editTextDate);
        editTextTime = view.findViewById(R.id.editTextTime);
        chipGroupStatus = view.findViewById(R.id.chipGroupStatus);
        buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonSave = view.findViewById(R.id.buttonSave);
    }

    private void setupListeners() {
        editTextDate.setOnClickListener(v -> showDatePicker());
        editTextTime.setOnClickListener(v -> showTimePicker());

        buttonCancel.setOnClickListener(v -> dismiss());
        buttonSave.setOnClickListener(v -> saveTask());
    }

    private void setupInitialValues() {
        // Set default date to today
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        editTextDate.setText(dateFormat.format(selectedDate.getTime()));

        // Set default time to current time
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        editTextTime.setText(timeFormat.format(selectedTime.getTime()));

        // Set default status to Running
        Chip runningChip = chipGroupStatus.findViewById(R.id.chipRunning);
        if (runningChip != null) {
            runningChip.setChecked(true);
        }
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDate.set(year, month, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    editTextDate.setText(dateFormat.format(selectedDate.getTime()));
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedTime.set(Calendar.MINUTE, minute);
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    editTextTime.setText(timeFormat.format(selectedTime.getTime()));
                },
                selectedTime.get(Calendar.HOUR_OF_DAY),
                selectedTime.get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void saveTask() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();

        if (title.isEmpty()) {
            editTextTitle.setError("Title is required");
            return;
        }

        // Get selected status
        Task.TaskStatus status = getSelectedStatus();

        // Format date and time
        String workDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.getTime());
        String workTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(selectedTime.getTime());

        // Create task
        Task task = new Task(
                String.valueOf(System.currentTimeMillis()), // Simple ID generation
                title,
                description,
                workDate,
                workTime,
                status);

        if (listener != null) {
            listener.onTaskSaved(task);
        }

        dismiss();
    }

    private Task.TaskStatus getSelectedStatus() {
        int checkedId = chipGroupStatus.getCheckedChipId();

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
}
