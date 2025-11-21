package com.sendajapan.sendasnap.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sendajapan.sendasnap.activities.schedule.AddScheduleActivity;
import com.sendajapan.sendasnap.activities.schedule.ScheduleDetailActivity;
import com.sendajapan.sendasnap.adapters.TaskAdapter;
import com.sendajapan.sendasnap.data.dto.PagedResult;
import com.sendajapan.sendasnap.data.dto.StatusUpdateRequest;
import com.sendajapan.sendasnap.data.dto.TaskResponseDto;
import com.sendajapan.sendasnap.data.mapper.TaskMapper;
import com.sendajapan.sendasnap.databinding.DialogTaskStatusBinding;
import com.sendajapan.sendasnap.databinding.FragmentScheduleBinding;
import com.sendajapan.sendasnap.dialogs.LoadingDialog;
import com.sendajapan.sendasnap.models.ApiResponse;
import com.sendajapan.sendasnap.models.Task;
import com.sendajapan.sendasnap.models.UserData;
import com.sendajapan.sendasnap.networking.ApiService;
import com.sendajapan.sendasnap.networking.RetrofitClient;
import com.sendajapan.sendasnap.utils.CookieBarToastHelper;
import com.sendajapan.sendasnap.utils.SharedPrefsManager;
import com.sendajapan.sendasnap.viewmodel.TaskViewModel;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleFragment extends Fragment
        implements TaskAdapter.OnTaskClickListener, TaskAdapter.OnTaskLongClickListener {

    private static final int ADD_TASK_REQUEST_CODE = 1001;

    private FragmentScheduleBinding binding;
    private SharedPrefsManager prefsManager;
    private TaskAdapter taskAdapter;
    private TaskViewModel taskViewModel;
    private ApiService apiService;
    private LoadingDialog loadingDialog;

    private Task.TaskStatus currentFilter = null;
    private String selectedDate;

    private boolean isFirstLoad = true;
    private boolean isPreventingDeselection = false;

    private int lastCheckedChipId = -1;

    private final List<Task> allTasks = new ArrayList<>();
    private final List<Task> filteredTasks = new ArrayList<>();

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
        taskViewModel = new ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().getApplication()))
                .get(TaskViewModel.class);
        apiService = RetrofitClient.getInstance(requireContext()).getApiService();

        setupRecyclerView();
        setupCalendar();
        setupFilterChips();
        setupFAB();
        setupSelectedDate();

        loadTasksFromApi();
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(filteredTasks, this);
        taskAdapter.setOnTaskLongClickListener(this);
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
        int initialCheckedId = binding.chipGroupStatus.getCheckedChipId();
        if (initialCheckedId != View.NO_ID) {
            lastCheckedChipId = initialCheckedId;
        }

        binding.chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (isPreventingDeselection) {
                return;
            }

            if (checkedIds.isEmpty()) {
                if (lastCheckedChipId != -1) {
                    isPreventingDeselection = true;
                    Chip lastChip = group.findViewById(lastCheckedChipId);
                    if (lastChip != null) {
                        lastChip.post(() -> {
                            lastChip.setChecked(true);
                            isPreventingDeselection = false;
                        });
                    } else {
                        isPreventingDeselection = false;
                    }
                    return;
                }
                currentFilter = null;
            } else {
                int newCheckedId = checkedIds.get(0);
                if (newCheckedId == lastCheckedChipId) {
                    return;
                }
                lastCheckedChipId = newCheckedId;

                Chip selectedChip = group.findViewById(newCheckedId);
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
                        case "All":
                            currentFilter = null;
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
            Calendar today = Calendar.getInstance();
            selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today.getTime());
        }

        showShimmer();

        taskViewModel.listTasks(selectedDate, selectedDate, new TaskViewModel.TaskCallback<PagedResult<Task>>() {
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
        if (binding == null)
            return;
        binding.shimmerTasks.setVisibility(View.VISIBLE);
        binding.shimmerTasks.startShimmer();
        binding.recyclerViewTasks.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.INVISIBLE);
    }

    private void hideShimmer() {
        if (binding == null)
            return;
        binding.shimmerTasks.stopShimmer();
        binding.shimmerTasks.setVisibility(View.GONE);
        binding.recyclerViewTasks.setVisibility(View.VISIBLE);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterTasks() {
        filteredTasks.clear();

        for (Task task : allTasks) {
            String taskDate = extractDateFromWorkDate(task.getWorkDate());

            if (taskDate != null && taskDate.equals(selectedDate)) {
                if (currentFilter == null || task.getStatus() == currentFilter) {
                    filteredTasks.add(task);
                }
            }
        }

        taskAdapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private String extractDateFromWorkDate(String workDate) {
        if (workDate == null || workDate.isEmpty()) {
            return null;
        }

        if (workDate.contains("T")) {
            return workDate.substring(0, 10);
        }

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
    public void onTaskLongClick(Task task) {
        showTaskStatusDialog(task);
    }

    private void showTaskStatusDialog(Task task) {
        DialogTaskStatusBinding dialogBinding = DialogTaskStatusBinding.inflate(getLayoutInflater());

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext());
        dialogBuilder.setView(dialogBinding.getRoot());
        dialogBuilder.setCancelable(true);

        AlertDialog dialog = dialogBuilder.create();

        dialogBinding.btnStatusPending.setOnClickListener(v -> {
            updateTaskStatus(task, "pending", dialog);
        });

        dialogBinding.btnStatusRunning.setOnClickListener(v -> {
            updateTaskStatus(task, "running", dialog);
        });

        dialogBinding.btnStatusCompleted.setOnClickListener(v -> {
            updateTaskStatus(task, "completed", dialog);
        });

        dialogBinding.btnStatusCancelled.setOnClickListener(v -> {
            updateTaskStatus(task, "cancelled", dialog);
        });

        dialogBinding.btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            showDeleteConfirmationDialog(task);
        });

        dialogBinding.btnClose.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateTaskStatus(Task task, String status, AlertDialog dialog) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            return;
        }

        loadingDialog = new LoadingDialog.Builder(requireContext())
                .setMessage("Updating task status...")
                .setCancelable(false)
                .setShowProgressIndicator(true)
                .build();
        loadingDialog.show();

        StatusUpdateRequest request = new StatusUpdateRequest(status);
        Call<ApiResponse<TaskResponseDto>> call = apiService.updateTaskStatus(task.getId(), request);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ApiResponse<TaskResponseDto>> call,
                    Response<ApiResponse<TaskResponseDto>> response) {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    TaskResponseDto taskResponseDto = response.body().getData();
                    if (taskResponseDto.getTask() != null) {
                        Task updatedTask = TaskMapper.toDomain(taskResponseDto.getTask());

                        updateTaskInList(updatedTask);
                        dialog.dismiss();

                        CookieBarToastHelper.showSuccess(requireContext(), "Success",
                                "Task status updated successfully", CookieBarToastHelper.SHORT_DURATION);
                    } else {
                        CookieBarToastHelper.showError(requireContext(), "Error",
                                "Invalid response from server", CookieBarToastHelper.LONG_DURATION);
                    }
                } else {
                    String errorMessage = "Failed to update task status";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            errorMessage = "Failed to update task status";
                        }
                    }
                    CookieBarToastHelper.showError(requireContext(), "Error", errorMessage,
                            CookieBarToastHelper.LONG_DURATION);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<TaskResponseDto>> call, @NonNull Throwable t) {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }

                CookieBarToastHelper.showError(requireContext(), "Error",
                        "Network error. Please check your connection and try again.",
                        CookieBarToastHelper.LONG_DURATION);
            }
        });
    }

    private void updateTaskInList(Task updatedTask) {
        for (int i = 0; i < allTasks.size(); i++) {
            if (allTasks.get(i).getId() == updatedTask.getId()) {
                allTasks.set(i, updatedTask);
                break;
            }
        }
        filterTasks();
    }

    private void showDeleteConfirmationDialog(Task task) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteTask(task);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTask(Task task) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            return;
        }

        loadingDialog = new LoadingDialog.Builder(requireContext())
                .setMessage("Deleting task...")
                .setCancelable(false)
                .setShowProgressIndicator(true)
                .build();
        loadingDialog.show();

        Call<ApiResponse<Object>> call = apiService.deleteTask(task.getId());

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Object>> call,
                    @NonNull Response<ApiResponse<Object>> response) {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();
                    if (apiResponse.getSuccess() != null && apiResponse.getSuccess()) {
                        allTasks.removeIf(t -> t.getId() == task.getId());
                        filterTasks();
                        CookieBarToastHelper.showSuccess(requireContext(), "Success",
                                apiResponse.getMessage() != null ? apiResponse.getMessage()
                                        : "Task deleted successfully",
                                CookieBarToastHelper.SHORT_DURATION);
                    } else {
                        String errorMessage = apiResponse.getMessage() != null
                                ? apiResponse.getMessage()
                                : "Failed to delete task";
                        CookieBarToastHelper.showError(requireContext(), "Error", errorMessage,
                                CookieBarToastHelper.LONG_DURATION);
                    }
                } else {
                    String errorMessage = "Failed to delete task";
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {
                            errorMessage = "Failed to delete task";
                        }
                    }
                    if (response.code() == 404) {
                        errorMessage = "Task not found";
                    }
                    CookieBarToastHelper.showError(requireContext(), "Error", errorMessage,
                            CookieBarToastHelper.LONG_DURATION);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Object>> call, @NonNull Throwable t) {
                if (loadingDialog != null && loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }

                CookieBarToastHelper.showError(requireContext(), "Error",
                        "Network error. Please check your connection and try again.",
                        CookieBarToastHelper.LONG_DURATION);
            }
        });
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
    public void onResume() {
        super.onResume();
        if (isAdded() && binding != null && !isFirstLoad) {
            loadTasksFromApi();
        }
        isFirstLoad = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
