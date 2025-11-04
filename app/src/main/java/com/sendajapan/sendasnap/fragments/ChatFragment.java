package com.sendajapan.sendasnap.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.activities.ChatActivity;
import com.sendajapan.sendasnap.activities.ContactsActivity;
import com.sendajapan.sendasnap.adapters.RecentChatAdapter;
import com.sendajapan.sendasnap.databinding.FragmentChatBinding;
import com.sendajapan.sendasnap.models.Chat;
import com.sendajapan.sendasnap.services.ChatService;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import java.util.List;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private RecentChatAdapter chatAdapter;
    private ChatService chatService;
    private HapticFeedbackHelper hapticHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initHelpers();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadRecentChats();
    }

    private void initHelpers() {
        chatService = ChatService.getInstance();
        hapticHelper = HapticFeedbackHelper.getInstance(requireContext());
    }

    private void setupToolbar() {
        // Set up toolbar with drawer
        if (getActivity() instanceof com.sendajapan.sendasnap.activities.MainActivity) {
            com.sendajapan.sendasnap.activities.MainActivity mainActivity = 
                    (com.sendajapan.sendasnap.activities.MainActivity) getActivity();

            // Set title only
            binding.toolbar.setTitle("Chat");

            // Update drawer controller with this fragment's toolbar
            if (mainActivity.drawerController != null) {
                mainActivity.drawerController.updateToolbar(binding.toolbar);
            }
        }
    }

    private void setupRecyclerView() {
        chatAdapter = new RecentChatAdapter(chat -> {
            hapticHelper.vibrateClick();
            openChat(chat);
        });
        
        binding.recyclerViewRecentChats.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewRecentChats.setAdapter(chatAdapter);
    }

    private void setupClickListeners() {
        binding.fabNewChat.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            openContactsActivity();
        });
    }

    private void loadRecentChats() {
        chatService.getRecentChats(requireContext(), new ChatService.RecentChatsCallback() {
            @Override
            public void onSuccess(List<Chat> chats) {
                if (chats.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();
                    chatAdapter.updateChats(chats);
                }
            }

            @Override
            public void onFailure(Exception e) {
                showEmptyState();
            }
        });
    }

    private void showEmptyState() {
        binding.recyclerViewRecentChats.setVisibility(View.GONE);
        binding.layoutEmptyState.setVisibility(View.VISIBLE);
    }

    private void hideEmptyState() {
        binding.recyclerViewRecentChats.setVisibility(View.VISIBLE);
        binding.layoutEmptyState.setVisibility(View.GONE);
    }

    private void openChat(Chat chat) {
        Intent intent = new Intent(requireContext(), ChatActivity.class);
        intent.putExtra("chatId", chat.getChatId());
        intent.putExtra("otherUserId", chat.getOtherUserId());
        intent.putExtra("otherUserName", chat.getOtherUserName());
        intent.putExtra("otherUserEmail", chat.getOtherUserEmail());
        startActivity(intent);
    }

    private void openContactsActivity() {
        Intent intent = new Intent(requireContext(), ContactsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
