package com.sendajapan.sendasnap.activities.schedule;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.firebase.database.ValueEventListener;
import com.sendajapan.sendasnap.MyApplication;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.activities.ChatActivity;
import com.sendajapan.sendasnap.adapters.TaskAttachmentAdapter;
import com.sendajapan.sendasnap.databinding.ActivityScheduleDetailBinding;
import com.sendajapan.sendasnap.models.Task;
import com.sendajapan.sendasnap.models.TaskAttachment;
import com.sendajapan.sendasnap.models.UserData;
import com.sendajapan.sendasnap.networking.ApiManager;
import com.sendajapan.sendasnap.networking.ApiCallback;
import com.sendajapan.sendasnap.services.ChatService;
import com.sendajapan.sendasnap.utils.FirebaseUtils;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.SharedPrefsManager;
import com.sendajapan.sendasnap.viewmodel.TaskViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class ScheduleDetailActivity extends AppCompatActivity {

    private ActivityScheduleDetailBinding binding;
    private ApiManager apiManager;
    private ChatService chatService;
    private HapticFeedbackHelper hapticHelper;
    private TaskAttachmentAdapter attachmentAdapter;
    private TaskViewModel taskViewModel;

    private MenuItem chatMenuItem;
    private TextView badgeTextView;
    private ValueEventListener unreadCountListener;
    private View chatActionView;
    private boolean isChatOpening = false;

    private Task task;
    private Integer taskId;

    private List<UserData> allUsers = new ArrayList<>();
    private SharedPrefsManager prefsManager;
    private UserData currentUser;
    
    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_MANAGER = "manager";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityScheduleDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MyApplication.applyWindowInsets(binding.getRoot());

        initHelpers();
        setupToolbar();
        loadTask();
        setupRecyclerView();
        populateFields();
        makeFieldsReadOnly();
        fetchUsers();

        if (task != null) {
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                loadUnreadCount();
            }, 500);
        }
    }

    private void initHelpers() {
        hapticHelper = HapticFeedbackHelper.getInstance(this);
        chatService = ChatService.getInstance();
        apiManager = ApiManager.getInstance(this);
        taskViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(TaskViewModel.class);
        prefsManager = SharedPrefsManager.getInstance(this);
        currentUser = prefsManager.getUser();
    }

    private void setupRecyclerView() {
        List<TaskAttachment> attachmentList = (task != null && task.getAttachments() != null)
                ? task.getAttachments()
                : new ArrayList<>();
        attachmentAdapter = new TaskAttachmentAdapter(attachmentList, false);
        binding.recyclerViewAttachments.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewAttachments.setAdapter(attachmentAdapter);
    }

    private void loadTask() {
        task = (Task) getIntent().getSerializableExtra("task");
        if (task != null) {
            taskId = task.getId();
        } else {
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

        taskViewModel.getTask(taskId, new TaskViewModel.TaskCallback<Task>() {
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
            getSupportActionBar().setTitle("Schedule Details");
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
        
        MenuItem editMenuItem = menu.findItem(R.id.action_edit);
        if (editMenuItem != null) {
            boolean canCreateTask = canUserCreateTask(currentUser);
            if (!canCreateTask) {
                editMenuItem.setEnabled(false);
                Objects.requireNonNull(editMenuItem.getIcon()).setTint(ContextCompat.getColor(ScheduleDetailActivity.this, R.color.gray_100));
                editMenuItem.getIcon().setTintMode(PorterDuff.Mode.SRC_IN);
            }
        }
        
        return true;
    }
    
    private boolean canUserCreateTask(UserData user) {
        if (user == null) {
            return false;
        }
        String role = user.getRole();
        return role != null && (role.equalsIgnoreCase(ROLE_ADMIN) || role.equalsIgnoreCase(ROLE_MANAGER));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_edit) {
            hapticHelper.vibrateClick();
            openEditMode();
            return true;
        } else if (itemId == R.id.action_chat) {
            if (!isChatOpening) {
                hapticHelper.vibrateClick();
                openTaskChat();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateStatus(Task.TaskStatus newStatus) {
        if (taskId == null) {
            Toast.makeText(this, "Task ID not available", Toast.LENGTH_SHORT).show();
            return;
        }

        taskViewModel.updateTaskStatus(taskId, newStatus, new TaskViewModel.TaskCallback<Task>() {
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

        taskViewModel.deleteTask(taskId, new TaskViewModel.TaskCallback<Void>() {
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

        binding.editTextTitle.setText(task.getTitle() != null ? task.getTitle() : "");
        binding.editTextDescription.setText(task.getDescription() != null ? task.getDescription() : "");

        displayAssignees();

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat displayFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTime(inputFormat.parse(task.getWorkDate()));
            binding.editTextDate.setText(displayFormat.format(dateCalendar.getTime()));
        } catch (Exception e) {
            binding.editTextDate.setText(task.getWorkDate());
        }

        binding.editTextTime.setText(task.getWorkTime());

        setStatusChip(task.getStatus());

        if (task.getPriority() != null) {
            setPriorityChip(task.getPriority());
        }

        if (task.getAttachments() != null && !task.getAttachments().isEmpty()) {
            attachmentAdapter = new TaskAttachmentAdapter(task.getAttachments(), false);
            binding.recyclerViewAttachments.setAdapter(attachmentAdapter);
            binding.recyclerViewAttachments.setVisibility(View.VISIBLE);
            binding.textNoFiles.setVisibility(View.GONE);
        } else {
            binding.recyclerViewAttachments.setVisibility(View.GONE);
            binding.textNoFiles.setVisibility(View.VISIBLE);
        }
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
        // Fields are already read-only TextViews in the detail layout
        // Just disable chip interactions
        for (int i = 0; i < binding.chipGroupStatus.getChildCount(); i++) {
            View child = binding.chipGroupStatus.getChildAt(i);
            if (child instanceof Chip) {
                ((Chip) child).setClickable(false);
            }
        }

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
                loadUnreadCount();
            }

            @Override
            public void onError(String message, int errorCode) {
            }
        });
    }

    private void setupChatIcon() {
        if (chatMenuItem != null) {
            chatActionView = getLayoutInflater().inflate(R.layout.menu_chat_badge, null);
            chatMenuItem.setActionView(chatActionView);
            badgeTextView = chatActionView.findViewById(R.id.badge_text);
            if (badgeTextView == null) {
                badgeTextView = new TextView(this);
                badgeTextView.setId(R.id.badge_text);
                badgeTextView.setBackgroundResource(R.drawable.badge_background);
                badgeTextView.setTextColor(getResources().getColor(R.color.white, null));
                badgeTextView.setTextSize(10);
                badgeTextView.setPadding(4, 2, 4, 2);
                badgeTextView.setMinWidth((int) (18 * getResources().getDisplayMetrics().density));
                badgeTextView.setMinHeight((int) (18 * getResources().getDisplayMetrics().density));
                badgeTextView.setGravity(android.view.Gravity.CENTER);
                badgeTextView.setVisibility(View.GONE);
            } else {
                badgeTextView.setVisibility(View.GONE);
            }

            chatActionView.setOnClickListener(v -> {
                if (!isChatOpening) {
                    hapticHelper.vibrateClick();
                    openTaskChat();
                }
            });
        }
    }

    private String getTaskChatId() {
        return "task_" + String.valueOf(task.getId());
    }

    private List<String> getTaskParticipants() {
        Set<String> participantIds = new HashSet<>();
        String currentUserId = FirebaseUtils.getCurrentUserId(this);

        UserData creator = task.getCreator();
        if (creator != null && creator.getEmail() != null && !creator.getEmail().isEmpty()) {
            String userId = FirebaseUtils.sanitizeEmailForKey(creator.getEmail());
            participantIds.add(userId);
        } else {
            int creatorId = task.getCreatedByUserId();
            if (creatorId > 0 && allUsers != null && !allUsers.isEmpty()) {
                for (UserData user : allUsers) {
                    if (user != null && user.getId() == creatorId && user.getEmail() != null && !user.getEmail().isEmpty()) {
                        String userId = FirebaseUtils.sanitizeEmailForKey(user.getEmail());
                        participantIds.add(userId);
                        break;
                    }
                }
            }
        }

        if (!currentUserId.isEmpty()) {
            participantIds.add(currentUserId);
        }

        List<UserData> assignees = task.getAssignees();
        if (assignees != null && !assignees.isEmpty()) {
            for (UserData assignee : assignees) {
                if (assignee != null && assignee.getEmail() != null && !assignee.getEmail().isEmpty()) {
                    String userId = FirebaseUtils.sanitizeEmailForKey(assignee.getEmail());
                    participantIds.add(userId);
                }
            }
        } else if (task.getAssignee() != null) {
            UserData assignee = task.getAssignee();
            if (assignee.getEmail() != null && !assignee.getEmail().isEmpty()) {
                String userId = FirebaseUtils.sanitizeEmailForKey(assignee.getEmail());
                participantIds.add(userId);
            }
        }

        return new ArrayList<>(participantIds);
    }

    private List<UserData> getTaskParticipantsAsUserData() {
        List<UserData> participants = new ArrayList<>();
        Set<String> addedEmails = new HashSet<>();

        UserData creator = task.getCreator();
        if (creator != null && creator.getEmail() != null && !creator.getEmail().isEmpty()) {
            participants.add(creator);
            addedEmails.add(creator.getEmail().toLowerCase());
        } else {
            int creatorId = task.getCreatedByUserId();
            if (creatorId > 0 && allUsers != null && !allUsers.isEmpty()) {
                for (UserData user : allUsers) {
                    if (user != null && user.getId() == creatorId && user.getEmail() != null && !user.getEmail().isEmpty()) {
                        String emailLower = user.getEmail().toLowerCase();
                        if (!addedEmails.contains(emailLower)) {
                            participants.add(user);
                            addedEmails.add(emailLower);
                        }
                        break;
                    }
                }
            }
        }

        List<UserData> assignees = task.getAssignees();
        if (assignees != null && !assignees.isEmpty()) {
            for (UserData assignee : assignees) {
                if (assignee != null && assignee.getEmail() != null && !assignee.getEmail().isEmpty()) {
                    String emailLower = assignee.getEmail().toLowerCase();
                    if (!addedEmails.contains(emailLower)) {
                        participants.add(assignee);
                        addedEmails.add(emailLower);
                    }
                }
            }
        } else if (task.getAssignee() != null) {
            UserData assignee = task.getAssignee();
            if (assignee.getEmail() != null && !assignee.getEmail().isEmpty()) {
                String emailLower = assignee.getEmail().toLowerCase();
                if (!addedEmails.contains(emailLower)) {
                    participants.add(assignee);
                    addedEmails.add(emailLower);
                }
            }
        }

        SharedPrefsManager prefsManager = SharedPrefsManager.getInstance(this);
        UserData currentUser = prefsManager.getUser();
        if (currentUser != null && currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
            String emailLower = currentUser.getEmail().toLowerCase();
            if (!addedEmails.contains(emailLower)) {
                participants.add(currentUser);
                addedEmails.add(emailLower);
            }
        }

        return participants;
    }

    private void loadUnreadCount() {
        if (task == null) {
            return;
        }

        String chatId = getTaskChatId();
        String currentUserId = FirebaseUtils.getCurrentUserId(this);

        if (currentUserId.isEmpty()) {
            return;
        }

        if (unreadCountListener != null) {
            chatService.removeUnreadCountListener(chatId, currentUserId, unreadCountListener);
            unreadCountListener = null;
        }

        chatService.getGroupChatUnreadCount(chatId, currentUserId, new ChatService.UnreadCountCallback() {
            @Override
            public void onSuccess(int unreadCount) {
                updateBadge(unreadCount);
            }

            @Override
            public void onFailure(Exception e) {
            }
        });

        unreadCountListener = chatService.addUnreadCountListener(chatId, currentUserId, new ChatService.UnreadCountCallback() {
            @Override
            public void onSuccess(int unreadCount) {
                updateBadge(unreadCount);
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
    }

    private void updateBadge(int unreadCount) {
        runOnUiThread(() -> {
            if (badgeTextView == null && chatMenuItem != null) {
                View actionView = chatMenuItem.getActionView();
                if (actionView != null) {
                    badgeTextView = actionView.findViewById(R.id.badge_text);
                }
            }

            if (badgeTextView != null) {
                if (unreadCount > 0) {
                    String badgeText = String.valueOf(unreadCount > 99 ? "99+" : unreadCount);
                    badgeTextView.setText(badgeText);
                    badgeTextView.setVisibility(View.VISIBLE);
                    badgeTextView.invalidate();
                    badgeTextView.requestLayout();
                } else {
                    badgeTextView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void openTaskChat() {
        if (isChatOpening) {
            return;
        }

        if (task == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            return;
        }

        List<UserData> participants = getTaskParticipantsAsUserData();
        if (participants.isEmpty()) {
            Toast.makeText(this, "No participants found for this task", Toast.LENGTH_SHORT).show();
            return;
        }

        isChatOpening = true;
        if (chatActionView != null) {
            chatActionView.setEnabled(false);
        }

        String chatId = getTaskChatId();
        chatService.createOrGetGroupChatWithParticipants(String.valueOf(task.getId()), task.getTitle(), participants, new ChatService.GroupChatCallback() {
            @Override
            public void onSuccess(com.sendajapan.sendasnap.models.Chat chat) {
                isChatOpening = false;
                if (chatActionView != null) {
                    chatActionView.setEnabled(true);
                }
                Intent intent = new Intent(ScheduleDetailActivity.this, ChatActivity.class);
                intent.putExtra("chatId", chat.getChatId());
                intent.putExtra("isGroupChat", true);
                intent.putExtra("taskId", String.valueOf(task.getId()));
                intent.putExtra("taskTitle", task.getTitle());
                intent.putExtra("participants", (java.io.Serializable) participants);
                startActivity(intent);
            }

            @Override
            public void onFailure(Exception e) {
                isChatOpening = false;
                if (chatActionView != null) {
                    chatActionView.setEnabled(true);
                }
                Toast.makeText(ScheduleDetailActivity.this, "Failed to open chat: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (task != null) {
            loadUnreadCount();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unreadCountListener != null && task != null) {
            String chatId = getTaskChatId();
            String currentUserId = FirebaseUtils.getCurrentUserId(this);
            if (!chatId.isEmpty() && !currentUserId.isEmpty()) {
                chatService.removeUnreadCountListener(chatId, currentUserId, unreadCountListener);
            }
            unreadCountListener = null;
        }
        binding = null;
    }
}
