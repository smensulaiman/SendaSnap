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
import com.sendajapan.sendasnap.activities.schedule.AddScheduleActivity;
import com.sendajapan.sendasnap.activities.schedule.ScheduleDetailActivity;
import com.sendajapan.sendasnap.adapters.TaskAdapter;
import com.sendajapan.sendasnap.databinding.FragmentScheduleBinding;
import com.sendajapan.sendasnap.data.dto.PagedResult;
import com.sendajapan.sendasnap.domain.usecase.ListTasksUseCase;
import com.sendajapan.sendasnap.models.Task;
import com.sendajapan.sendasnap.models.UserData;
import com.sendajapan.sendasnap.utils.SharedPrefsManager;

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
    private SharedPrefsManager prefsManager;
    private ListTasksUseCase listTasksUseCase;

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

        prefsManager = SharedPrefsManager.getInstance(requireContext());
        listTasksUseCase = new ListTasksUseCase(requireContext());
        
        setupRecyclerView();
        setupCalendar();
        setupFilterChips();
        setupFAB();
        setupSelectedDate();

        loadTasksFromApi();
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(filteredTasks, this);
        binding.recyclerViewTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewTasks.setAdapter(taskAdapter);
    }

    private void setupCalendar() {

        binding.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            updateSelectedDateText();
            loadTasksFromApi();
        });

        Calendar today = Calendar.getInstance();
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.getTime());
        updateSelectedDateText();
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
                        case "In Progress":
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
            loadTasksFromApi();
        });
    }

    private void setupFAB() {
        UserData currentUser = prefsManager.getUser();
        boolean canCreateTask = false;
        
        if (currentUser != null) {
            String role = currentUser.getRole();
            if (role != null && (role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("manager"))) {
                canCreateTask = true;
            }
        }
        
        if (canCreateTask) {
            binding.fabAddTask.setVisibility(View.VISIBLE);
            binding.fabAddTask.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), AddScheduleActivity.class);
                startActivityForResult(intent, ADD_TASK_REQUEST_CODE);
            });
        } else {
            binding.fabAddTask.setVisibility(View.GONE);
        }
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

    private void loadTasksFromApi() {
        if (selectedDate == null) {
            // Set default to today if not set
            Calendar today = Calendar.getInstance();
            selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.getTime());
        }

        showShimmer();

        // Use selectedDate as both from_date and to_date to get tasks for that specific date
        listTasksUseCase.execute(selectedDate, selectedDate, 1, new ListTasksUseCase.UseCaseCallback<PagedResult<Task>>() {
            @Override
            public void onSuccess(PagedResult<Task> result) {
                if (!isAdded() || binding == null) {
                    return;
                }

                allTasks.clear();
                if (result != null && result.getItems() != null) {
                    allTasks.addAll(result.getItems());
                }

                hideShimmer();
                filterTasks();
            }

            @Override
            public void onError(String message, int errorCode) {
                if (!isAdded() || binding == null) {
                    return;
                }

                hideShimmer();
                allTasks.clear();
                filterTasks();
                
                // Show error message with user-friendly text
                String errorMessage = getErrorMessage(message, errorCode);
                Toast.makeText(requireContext(), "Failed to load tasks: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String getErrorMessage(String message, int errorCode) {
        switch (errorCode) {
            case 401:
                return "Authentication required. Please login again.";
            case 403:
                return "You don't have permission to perform this action.";
            case 404:
                return "Resource not found.";
            case 422:
                return "Validation error: " + message;
            default:
                return message != null ? message : "An error occurred. Please try again.";
        }
    }
    
    private void showShimmer() {
        if (binding == null) return;
        binding.shimmerTasks.setVisibility(View.VISIBLE);
        binding.shimmerTasks.startShimmer();
        binding.recyclerViewTasks.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.INVISIBLE);
    }
    
    private void hideShimmer() {
        if (binding == null) return;
        binding.shimmerTasks.stopShimmer();
        binding.shimmerTasks.setVisibility(View.GONE);
        binding.recyclerViewTasks.setVisibility(View.VISIBLE);
    }

    private void filterTasks() {
        filteredTasks.clear();

        for (Task task : allTasks) {
            // Extract date from workDate (API returns ISO format like "2025-11-12T00:00:00.000000Z")
            String taskDate = extractDateFromWorkDate(task.getWorkDate());
            
            if (taskDate != null && taskDate.equals(selectedDate)) {
                // Status filter is already applied in API call, but keep this for safety
                if (currentFilter == null || task.getStatus() == currentFilter) {
                    filteredTasks.add(task);
                }
            }
        }

        taskAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    /**
     * Extract date in YYYY-MM-DD format from workDate string
     * Handles both ISO format (2025-11-12T00:00:00.000000Z) and simple format (2025-11-12)
     */
    private String extractDateFromWorkDate(String workDate) {
        if (workDate == null || workDate.isEmpty()) {
            return null;
        }
        
        // If it contains 'T', it's ISO format - extract date part
        if (workDate.contains("T")) {
            return workDate.substring(0, 10); // Get "YYYY-MM-DD" part
        }
        
        // If it's already in YYYY-MM-DD format, return as is
        if (workDate.length() >= 10) {
            return workDate.substring(0, 10);
        }
        
        return workDate;
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
        // Reload tasks from API to get the latest data
        loadTasksFromApi();
        Toast.makeText(getContext(), "Task added successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTaskClick(Task task) {
        Intent intent = new Intent(getContext(), ScheduleDetailActivity.class);
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
