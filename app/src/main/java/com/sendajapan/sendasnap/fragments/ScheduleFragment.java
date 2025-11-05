package com.sendajapan.sendasnap.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.activities.AddTaskActivity;
import com.sendajapan.sendasnap.activities.TaskDetailsActivity;
import com.sendajapan.sendasnap.adapters.TaskAdapter;
import com.sendajapan.sendasnap.databinding.FragmentScheduleBinding;
import com.sendajapan.sendasnap.models.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleFragment extends Fragment implements TaskAdapter.OnTaskClickListener {

    private static final int ADD_TASK_REQUEST_CODE = 1001;

    private final List<Task> allTasks = new ArrayList<>();
    private final List<Task> filteredTasks = new ArrayList<>();

    private FragmentScheduleBinding binding;
    private TaskAdapter taskAdapter;

    private String selectedDate;
    private Task.TaskStatus currentFilter = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentScheduleBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToolbar();
        setupRecyclerView();
        setupCalendar();
        setupFilterChips();
        setupFAB();
        setupSelectedDate();

        // Load mock data for demonstration
        loadMockTasks();
    }

    private void setupToolbar() {
        // Set up toolbar with drawer
        if (getActivity() instanceof com.sendajapan.sendasnap.activities.MainActivity) {
            com.sendajapan.sendasnap.activities.MainActivity mainActivity = (com.sendajapan.sendasnap.activities.MainActivity) getActivity();

            // Set title only
            binding.toolbar.setTitle("Schedule");

            // Update drawer controller with this fragment's toolbar
            if (mainActivity.drawerController != null) {
                mainActivity.drawerController.updateToolbar(binding.toolbar);
            }
        }
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(filteredTasks, this);
        binding.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewTasks.setAdapter(taskAdapter);
    }

    private void setupCalendar() {
        // Apply theme color to calendar selected date
        applyThemeColorToCalendar();

        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            updateSelectedDateText();
            filterTasks();
        });

        // Set initial date to today
        Calendar today = Calendar.getInstance();
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.getTime());
        updateSelectedDateText();
    }

    private void applyThemeColorToCalendar() {
        try {
            // Apply theme color using CalendarView's built-in attributes
            // The custom theme should handle most of the styling
            binding.calendarView.setBackgroundColor(getResources().getColor(R.color.white, null));

            // Try to set the accent color programmatically
            int primaryColor = getResources().getColor(R.color.primary, null);

            // Use reflection to access CalendarView's internal color settings
            try {
                java.lang.reflect.Field field = binding.calendarView.getClass().getDeclaredField("mDelegate");
                field.setAccessible(true);
                Object delegate = field.get(binding.calendarView);

                if (delegate != null) {
                    // Try to set the accent color
                    java.lang.reflect.Method setAccentColor = delegate.getClass().getDeclaredMethod("setAccentColor",
                            int.class);
                    setAccentColor.setAccessible(true);
                    setAccentColor.invoke(delegate, primaryColor);
                }
            } catch (Exception reflectionException) {
                android.util.Log.d("ScheduleFragment", "Reflection approach failed, theme should handle styling");
            }

        } catch (Exception e) {
            android.util.Log.e("ScheduleFragment", "Could not apply theme color to calendar", e);
        }
    }

    private void setupFilterChips() {
        binding.chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentFilter = null;
            } else {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                if (selectedChip != null) {
                    String chipText = selectedChip.getText().toString();
                    switch (chipText) {
                        case "Running":
                            currentFilter = Task.TaskStatus.RUNNING;
                            break;
                        case "Pending":
                            currentFilter = Task.TaskStatus.PENDING;
                            break;
                        case "Completed":
                            currentFilter = Task.TaskStatus.COMPLETED;
                            break;
                        case "Cancelled":
                            currentFilter = Task.TaskStatus.CANCELLED;
                            break;
                        default:
                            currentFilter = null;
                            break;
                    }
                }
            }
            filterTasks();
        });
    }

    private void setupFAB() {
        binding.fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddTaskActivity.class);
            startActivityForResult(intent, ADD_TASK_REQUEST_CODE);
        });
    }

    private void setupSelectedDate() {
        updateSelectedDateText();
    }

    private void updateSelectedDateText() {
        if (selectedDate != null) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(selectedDate);
                String formattedDate = outputFormat.format(date);
                binding.textSelectedDate.setText(formattedDate);
            } catch (Exception e) {
                binding.textSelectedDate.setText("Today's Tasks");
            }
        }
    }

    private void loadMockTasks() {
        // Mock data for demonstration
        allTasks.clear();

        Calendar calendar = Calendar.getInstance();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

        allTasks.add(new Task("1", "Vehicle Inspection", "Complete safety inspection for Toyota Camry", today, "09:00",
                Task.TaskStatus.RUNNING));
        allTasks.add(new Task("2", "Paperwork Review", "Review and process vehicle documentation", today, "14:00",
                Task.TaskStatus.PENDING));
        allTasks.add(new Task("3", "Client Meeting", "Meet with client for vehicle delivery", today, "16:30",
                Task.TaskStatus.COMPLETED));

        filterTasks();
    }

    private void filterTasks() {
        filteredTasks.clear();

        for (Task task : allTasks) {
            // Filter by date
            if (selectedDate != null && task.getWorkDate().equals(selectedDate)) {
                // Filter by status
                if (currentFilter == null || task.getStatus() == currentFilter) {
                    filteredTasks.add(task);
                }
            }
        }

        taskAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredTasks.isEmpty()) {
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.recyclerViewTasks.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.recyclerViewTasks.setVisibility(View.VISIBLE);
        }
    }

    private void onTaskSaved(Task task) {
        allTasks.add(task);
        filterTasks();
        Toast.makeText(getContext(), "Task added successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskClick(Task task) {
        Intent intent = new Intent(getContext(), TaskDetailsActivity.class);
        intent.putExtra("task", task);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_TASK_REQUEST_CODE && resultCode == getActivity().RESULT_OK && data != null) {
            Task task = (Task) data.getSerializableExtra("task");
            if (task != null) {
                onTaskSaved(task);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
