package com.sendajapan.sendasnap.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sendajapan.sendasnap.databinding.FragmentChatBinding;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;

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

        setupToolbar();

        // Chat fragment is currently a placeholder
        // Future implementation will include:
        // - Message list RecyclerView
        // - Message input field
        // - Send button
        // - Real-time messaging functionality
    }

    private void setupToolbar() {
        // Set up toolbar with drawer
        if (getActivity() instanceof com.sendajapan.sendasnap.activities.MainActivity) {
            com.sendajapan.sendasnap.activities.MainActivity mainActivity = (com.sendajapan.sendasnap.activities.MainActivity) getActivity();

            // Set title only
            binding.toolbar.setTitle("Chat");

            // Update drawer controller with this fragment's toolbar
            if (mainActivity.drawerController != null) {
                mainActivity.drawerController.updateToolbar(binding.toolbar);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
