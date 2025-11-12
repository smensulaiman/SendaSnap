package com.sendajapan.sendasnap.activities.schedule;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.activities.ChatActivity;
import com.sendajapan.sendasnap.adapters.TaskAttachmentAdapter;
import com.sendajapan.sendasnap.databinding.ActivityAddTaskBinding;
import com.sendajapan.sendasnap.domain.usecase.DeleteTaskUseCase;
import com.sendajapan.sendasnap.domain.usecase.GetTaskUseCase;
import com.sendajapan.sendasnap.domain.usecase.UpdateTaskStatusUseCase;
import com.sendajapan.sendasnap.models.Task;
import com.sendajapan.sendasnap.models.TaskAttachment;
import com.sendajapan.sendasnap.models.UserData;
import com.sendajapan.sendasnap.networking.ApiCallback;
import com.sendajapan.sendasnap.networking.ApiManager;
import com.sendajapan.sendasnap.services.ChatService;
import com.sendajapan.sendasnap.utils.FirebaseUtils;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ScheduleDetailActivity extends AppCompatActivity {

    private ActivityAddTaskBinding binding;
    private HapticFeedbackHelper hapticHelper;
    private Task task;
    private TaskAttachmentAdapter attachmentAdapter;
    private ChatService chatService;
    private ApiManager apiManager;
    private MenuItem chatMenuItem;
    private ValueEventListener unreadCountListener;
    private List<UserData> allUsers = new ArrayList<>();
    private TextView badgeTextView;
    private GetTaskUseCase getTaskUseCase;
    private UpdateTaskStatusUseCase updateTaskStatusUseCase;
    private DeleteTaskUseCase deleteTaskUseCase;
    private Integer taskId;

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
        setupRecyclerView();
        populateFields();
        makeFieldsReadOnly();
        fetchUsers();
    }

    private void initHelpers() {
        hapticHelper = HapticFeedbackHelper.getInstance(this);
        chatService = ChatService.getInstance();
        apiManager = ApiManager.getInstance(this);
        getTaskUseCase = new GetTaskUseCase(this);
        updateTaskStatusUseCase = new UpdateTaskStatusUseCase(this);
        deleteTaskUseCase = new DeleteTaskUseCase(this);
    }

    private void setupRecyclerView() {
        java.util.List<TaskAttachment> attachmentList = (task != null && task.getAttachments() != null) 
                ? task.getAttachments() 
                : new java.util.ArrayList<>();
        attachmentAdapter = new TaskAttachmentAdapter(attachmentList, false); // No remove button in detail view
        binding.recyclerViewAttachments.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        binding.recyclerViewAttachments.setAdapter(attachmentAdapter);
    }

    private void loadTask() {
        task = (Task) getIntent().getSerializableExtra("task");
        if (task != null) {
            taskId = task.getId();
        } else {
            // Try to load by ID if task object not passed
            taskId = getIntent().getIntExtra("taskId", -1);
            if (taskId == -1) {
                finish();
                return;
            }
            loadTaskFromApi();
        }
    }

    private void loadTaskFromApi() {
        if (taskId == null) return;
        
        getTaskUseCase.execute(taskId, new GetTaskUseCase.UseCaseCallback<Task>() {
            @Override
            public void onSuccess(Task loadedTask) {
                task = loadedTask;
                populateFields();
            }

            @Override
            public void onError(String message, int errorCode) {
                Toast.makeText(ScheduleDetailActivity.this, 
                        getErrorMessage(message, errorCode), Toast.LENGTH_SHORT).show();
                if (errorCode == 404) {
                    finish();
                }
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
                return "Task not found.";
            case 422:
                return "Validation error: " + message;
            default:
                return message != null ? message : "An error occurred. Please try again.";
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Task Details");
        }

        binding.toolbar.setNavigationOnClickListener(v -> {
            hapticHelper.vibrateClick();
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_schedule_detail, menu);
        chatMenuItem = menu.findItem(R.id.action_chat);
        setupChatIcon();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_edit) {
            hapticHelper.vibrateClick();
            openEditMode();
            return true;
        } else if (itemId == R.id.action_chat) {
            hapticHelper.vibrateClick();
            openTaskChat();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateStatus(Task.TaskStatus newStatus) {
        if (taskId == null) {
            Toast.makeText(this, "Task ID not available", Toast.LENGTH_SHORT).show();
            return;
        }

        updateTaskStatusUseCase.execute(taskId, newStatus, new UpdateTaskStatusUseCase.UseCaseCallback<Task>() {
            @Override
            public void onSuccess(Task updatedTask) {
                task = updatedTask;
                populateFields();
                Toast.makeText(ScheduleDetailActivity.this, "Status updated successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message, int errorCode) {
                Toast.makeText(ScheduleDetailActivity.this, 
                        getErrorMessage(message, errorCode), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteTask())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTask() {
        if (taskId == null) {
            Toast.makeText(this, "Task ID not available", Toast.LENGTH_SHORT).show();
            return;
        }

        deleteTaskUseCase.execute(taskId, new DeleteTaskUseCase.UseCaseCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(ScheduleDetailActivity.this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String message, int errorCode) {
                Toast.makeText(ScheduleDetailActivity.this, 
                        getErrorMessage(message, errorCode), Toast.LENGTH_SHORT).show();
            }
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
        
        // Display assignees
        displayAssignees();

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
            attachmentAdapter = new TaskAttachmentAdapter(task.getAttachments(), false);
            binding.recyclerViewAttachments.setAdapter(attachmentAdapter);
            binding.recyclerViewAttachments.setVisibility(View.VISIBLE);
            binding.textNoFiles.setVisibility(View.GONE);
        } else {
            binding.recyclerViewAttachments.setVisibility(View.GONE);
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


    private void displayAssignees() {
        binding.chipGroupAssignees.removeAllViews();
        
        List<UserData> assignees = task.getAssignees();
        if (assignees != null && !assignees.isEmpty()) {
            binding.editTextAssignee.setText(assignees.size() + " assignee(s)");
            binding.chipGroupAssignees.setVisibility(View.VISIBLE);
            
            for (UserData assignee : assignees) {
                if (assignee == null) continue;
                Chip chip = new Chip(this);
                chip.setText(assignee.getName() != null ? assignee.getName() : "");
                chip.setCloseIconVisible(false);
                chip.setClickable(false);
                binding.chipGroupAssignees.addView(chip);
            }
        } else if (task.getAssignee() != null) {
            // Fallback to old single assignee field
            UserData assignee = task.getAssignee();
            binding.editTextAssignee.setText(assignee.getName() != null ? assignee.getName() : "");
            Chip chip = new Chip(this);
            chip.setText(assignee.getName() != null ? assignee.getName() : "");
            chip.setCloseIconVisible(false);
            chip.setClickable(false);
            binding.chipGroupAssignees.addView(chip);
            binding.chipGroupAssignees.setVisibility(View.VISIBLE);
        } else {
            binding.editTextAssignee.setText("No assignees");
            binding.chipGroupAssignees.setVisibility(View.GONE);
        }
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

    private void fetchUsers() {
        apiManager.getUsers(new ApiCallback<List<UserData>>() {
            @Override
            public void onSuccess(List<UserData> userList) {
                allUsers.clear();
                allUsers.addAll(userList);
                // Load unread count after users are fetched
                loadUnreadCount();
            }

            @Override
            public void onError(String message, int errorCode) {
                // Silently fail, users might not be critical for viewing task
            }
        });
    }

    private void setupChatIcon() {
        if (chatMenuItem != null) {
            // Create badge view
            View actionView = getLayoutInflater().inflate(R.layout.menu_chat_badge, null);
            chatMenuItem.setActionView(actionView);
            badgeTextView = actionView.findViewById(R.id.badge_text);
            if (badgeTextView == null) {
                // Fallback: create badge programmatically
                badgeTextView = new TextView(this);
                badgeTextView.setBackgroundResource(R.drawable.badge_background);
                badgeTextView.setTextColor(getResources().getColor(R.color.white, null));
                badgeTextView.setTextSize(10);
                badgeTextView.setPadding(4, 2, 4, 2);
                badgeTextView.setVisibility(View.GONE);
            }
        }
    }

    private String getTaskChatId() {
        return "task_" + String.valueOf(task.getId());
    }

    private List<String> getTaskParticipants() {
        Set<String> participantIds = new HashSet<>();
        String currentUserId = FirebaseUtils.getCurrentUserId(this);
        
        // Add creator if exists - need to find user by ID and get email
        int creatorId = task.getCreatedByUserId();
        if (creatorId > 0 && allUsers != null) {
            for (UserData user : allUsers) {
                if (user != null && user.getId() == creatorId && user.getEmail() != null && !user.getEmail().isEmpty()) {
                    String userId = FirebaseUtils.sanitizeEmailForKey(user.getEmail());
                    participantIds.add(userId);
                    break;
                }
            }
        }
        
        // Add current user if not already added
        if (!currentUserId.isEmpty()) {
            participantIds.add(currentUserId);
        }
        
        // Get assignee user IDs directly from UserData objects
        List<UserData> assignees = task.getAssignees();
        if (assignees != null && !assignees.isEmpty()) {
            for (UserData assignee : assignees) {
                if (assignee != null && assignee.getEmail() != null && !assignee.getEmail().isEmpty()) {
                    String userId = FirebaseUtils.sanitizeEmailForKey(assignee.getEmail());
                    participantIds.add(userId);
                }
            }
        } else if (task.getAssignee() != null) {
            // Fallback to old single assignee field
            UserData assignee = task.getAssignee();
            if (assignee.getEmail() != null && !assignee.getEmail().isEmpty()) {
                String userId = FirebaseUtils.sanitizeEmailForKey(assignee.getEmail());
                participantIds.add(userId);
            }
        }
        
        return new ArrayList<>(participantIds);
    }

    private void loadUnreadCount() {
        if (task == null) return;
        
        String chatId = getTaskChatId();
        String currentUserId = FirebaseUtils.getCurrentUserId(this);
        
        if (currentUserId.isEmpty()) return;
        
        chatService.getGroupChatUnreadCount(chatId, currentUserId, new ChatService.UnreadCountCallback() {
            @Override
            public void onSuccess(int unreadCount) {
                updateBadge(unreadCount);
            }

            @Override
            public void onFailure(Exception e) {
                // Silently fail
            }
        });
    }

    private void updateBadge(int unreadCount) {
        if (badgeTextView != null) {
            if (unreadCount > 0) {
                badgeTextView.setText(String.valueOf(unreadCount > 99 ? "99+" : unreadCount));
                badgeTextView.setVisibility(View.VISIBLE);
            } else {
                badgeTextView.setVisibility(View.GONE);
            }
        }
    }

    private void openTaskChat() {
        if (task == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        List<String> participantIds = getTaskParticipants();
        if (participantIds.isEmpty()) {
            Toast.makeText(this, "No participants found for this task", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String chatId = getTaskChatId();
        chatService.createOrGetGroupChat(String.valueOf(task.getId()), task.getTitle(), participantIds, new ChatService.GroupChatCallback() {
            @Override
            public void onSuccess(com.sendajapan.sendasnap.models.Chat chat) {
                Intent intent = new Intent(ScheduleDetailActivity.this, ChatActivity.class);
                intent.putExtra("chatId", chat.getChatId());
                intent.putExtra("isGroupChat", true);
                intent.putExtra("taskId", String.valueOf(task.getId()));
                intent.putExtra("taskTitle", task.getTitle());
                startActivity(intent);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ScheduleDetailActivity.this, "Failed to open chat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove Firebase listeners
        if (unreadCountListener != null) {
            // Note: getGroupChatUnreadCount uses addValueEventListener which needs to be removed
            // We'll need to store the reference to remove it properly
        }
        binding = null;
    }
}

