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
import com.sendajapan.sendasnap.activities.schedule.AddScheduleActivity;
import com.sendajapan.sendasnap.activities.schedule.ScheduleDetailActivity;
import com.sendajapan.sendasnap.adapters.TaskAdapter;
import com.sendajapan.sendasnap.databinding.FragmentScheduleBinding;
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
        
        setupRecyclerView();
        setupCalendar();
        setupFilterChips();
        setupFAB();
        setupSelectedDate();

        // Load mock data for demonstration
        loadMockTasks();
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
            binding.calendarView.setBackgroundColor(getResources().getColor(R.color.white, null));
        } catch (Exception e) {
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
        });
    }

    private void setupFAB() {
        // Check user role to show/hide FAB
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

    private void loadMockTasks() {
        showShimmer();

        new android.os.Handler().postDelayed(() -> {
            if (!isAdded() || binding == null) {
                return;
            }

            allTasks.clear();

            Calendar calendar = Calendar.getInstance();
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());

            allTasks.add(new Task("1", "Vehicle Inspection", "Complete safety inspection for Toyota Camry", today, "09:00",
                    Task.TaskStatus.RUNNING));
            allTasks.add(new Task("2", "Paperwork Review", "Review and process vehicle documentation", today, "14:00",
                    Task.TaskStatus.PENDING));
            allTasks.add(new Task("3", "Client Meeting", "Meet with client for vehicle delivery", today, "16:30",
                    Task.TaskStatus.COMPLETED));

            hideShimmer();
            
            filterTasks();
        }, 700);
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
            if (task.getWorkDate().equals(selectedDate)) {
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
