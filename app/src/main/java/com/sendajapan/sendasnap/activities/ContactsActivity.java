package com.sendajapan.sendasnap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.adapters.ContactAdapter;
import com.sendajapan.sendasnap.databinding.ActivityContactsBinding;
import com.sendajapan.sendasnap.models.ChatUser;
import com.sendajapan.sendasnap.services.ChatService;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import java.util.ArrayList;
import java.util.List;

public class ContactsActivity extends AppCompatActivity {

    private ActivityContactsBinding binding;
    private ContactAdapter contactAdapter;
    private ChatService chatService;
    private HapticFeedbackHelper hapticHelper;
    private List<ChatUser> allUsers;
    private List<ChatUser> filteredUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityContactsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set status bar and navigation bar colors
        getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_color, getTheme()));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.navigation_bar_color, getTheme()));

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initHelpers();
        setupToolbar();
        setupRecyclerView();
        setupSearchListener();
        loadUsers();
    }

    private void initHelpers() {
        chatService = ChatService.getInstance();
        hapticHelper = HapticFeedbackHelper.getInstance(this);
        allUsers = new ArrayList<>();
        filteredUsers = new ArrayList<>();
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
        contactAdapter = new ContactAdapter(user -> {
            hapticHelper.vibrateClick();
            openChat(user);
        });
        
        binding.recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewContacts.setAdapter(contactAdapter);
    }

    private void setupSearchListener() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadUsers() {
        chatService.getAllUsers(this, new ChatService.UsersCallback() {
            @Override
            public void onSuccess(List<ChatUser> users) {
                allUsers.clear();
                allUsers.addAll(users);
                filteredUsers.clear();
                filteredUsers.addAll(users);
                
                if (filteredUsers.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();
                    contactAdapter.updateUsers(filteredUsers);
                }
            }

            @Override
            public void onFailure(Exception e) {
                showEmptyState();
            }
        });
    }

    private void filterUsers(String query) {
        filteredUsers.clear();
        
        if (query.isEmpty()) {
            filteredUsers.addAll(allUsers);
        } else {
            String lowerQuery = query.toLowerCase();
            for (ChatUser user : allUsers) {
                if (user.getUsername().toLowerCase().contains(lowerQuery) ||
                    user.getEmail().toLowerCase().contains(lowerQuery)) {
                    filteredUsers.add(user);
                }
            }
        }
        
        if (filteredUsers.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
        }
        
        contactAdapter.updateUsers(filteredUsers);
    }

    private void showEmptyState() {
        binding.recyclerViewContacts.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
    }

    private void hideEmptyState() {
        binding.recyclerViewContacts.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);
    }

    private void openChat(ChatUser user) {
        chatService.createChat(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                new ChatService.ChatCallback() {
                    @Override
                    public void onSuccess(com.sendajapan.sendasnap.models.Chat chat) {
                        Intent intent = new Intent(ContactsActivity.this, ChatActivity.class);
                        intent.putExtra("chatId", chat.getChatId());
                        intent.putExtra("otherUserId", chat.getOtherUserId());
                        intent.putExtra("otherUserName", chat.getOtherUserName());
                        intent.putExtra("otherUserEmail", chat.getOtherUserEmail());
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(Exception e) {
                        // Handle error
                    }
                });
    }
}

