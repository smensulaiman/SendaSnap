package com.sendajapan.sendasnap.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sendajapan.sendasnap.MyApplication;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.adapters.MessageAdapter;
import com.sendajapan.sendasnap.databinding.ActivityChatBinding;
import com.sendajapan.sendasnap.databinding.BottomSheetImagePickerBinding;
import com.sendajapan.sendasnap.models.Message;
import com.sendajapan.sendasnap.services.ChatService;
import com.sendajapan.sendasnap.services.FirebaseStorageService;
import com.sendajapan.sendasnap.utils.CookieBarToastHelper;
import com.sendajapan.sendasnap.utils.FirebaseUtils;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;

import java.io.File;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private ChatService chatService;
    private FirebaseStorageService storageService;
    private HapticFeedbackHelper hapticHelper;
    private MessageAdapter messageAdapter;

    private String chatId;
    private String currentUserId;
    private String otherUserEmail;
    private String otherUserId;
    private String otherUserName;
    private String taskId;
    private String taskTitle;

    private boolean isGroupChat = false;

    private ActivityResultLauncher<String> filePickerLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;

    private Uri cameraImageUri;
    private String cameraImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        MyApplication.applyWindowInsets(binding.getRoot());

        initHelpers();
        getIntentData();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        setupActivityResultLaunchers();
        initializeCurrentUser();
        loadMessages();
        markMessagesAsSeen();
    }

    private void initHelpers() {
        chatService = ChatService.getInstance();
        storageService = FirebaseStorageService.getInstance();
        hapticHelper = HapticFeedbackHelper.getInstance(this);
        currentUserId = FirebaseUtils.getCurrentUserId(this);
    }

    private void initializeCurrentUser() {
        try {
            chatService.initializeUser(this);
        } catch (Exception e) {
            android.util.Log.e("ChatActivity", "Failed to initialize user in Firebase", e);
        }
    }

    private void getIntentData() {
        chatId = getIntent().getStringExtra("chatId");
        isGroupChat = getIntent().getBooleanExtra("isGroupChat", false);
        taskId = getIntent().getStringExtra("taskId");
        taskTitle = getIntent().getStringExtra("taskTitle");
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUserName = getIntent().getStringExtra("otherUserName");
        otherUserEmail = getIntent().getStringExtra("otherUserEmail");
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (isGroupChat && taskTitle != null) {
            binding.toolbar.setTitle(taskTitle);
        } else {
            binding.toolbar.setTitle(otherUserName != null ? otherUserName : "Chat");
        }

        binding.toolbar.post(() -> {
            android.widget.TextView titleView = findTitleTextView(binding.toolbar);
            if (titleView != null) {
                titleView.setMaxLines(2);
                titleView.setEllipsize(android.text.TextUtils.TruncateAt.END);
                titleView.setSingleLine(false);
            }
        });

        binding.toolbar.setNavigationOnClickListener(v -> {
            hapticHelper.vibrateClick();
            finish();
        });
    }

    private android.widget.TextView findTitleTextView(android.view.ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            android.view.View child = parent.getChildAt(i);
            if (child instanceof android.widget.TextView) {
                android.widget.TextView textView = (android.widget.TextView) child;
                String toolbarTitle = binding.toolbar.getTitle() != null ? binding.toolbar.getTitle().toString() : "";
                String textViewText = textView.getText() != null ? textView.getText().toString() : "";
                if (textViewText.equals(toolbarTitle) && textView.getVisibility() == android.view.View.VISIBLE) {
                    return textView;
                }
            } else if (child instanceof android.view.ViewGroup) {
                android.widget.TextView found = findTitleTextView((android.view.ViewGroup) child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(currentUserId, isGroupChat);
        messageAdapter.setOnImageClickListener(imageUrl -> {
            Toast.makeText(this, "Image viewer coming soon", Toast.LENGTH_SHORT).show();
        });
        messageAdapter.setOnFileClickListener((fileUrl, fileName) -> {
            Toast.makeText(this, "File download coming soon", Toast.LENGTH_SHORT).show();
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.recyclerViewMessages.setLayoutManager(layoutManager);
        binding.recyclerViewMessages.setAdapter(messageAdapter);
    }

    private void setupClickListeners() {
        binding.btnSend.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            sendTextMessage();
        });

        binding.btnAttachment.setOnClickListener(v -> {
            hapticHelper.vibrateClick();
            showAttachmentBottomSheet();
        });

        binding.etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendTextMessage();
                return true;
            }
            return false;
        });

        binding.etMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.recyclerViewMessages.postDelayed(() -> scrollToBottom(), 100);
            }
        });
    }

    private void setupActivityResultLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && cameraImageUri != null) {
                        uploadImage(cameraImageUri);
                    }
                });

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadImage(uri);
                    }
                });

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadFile(uri);
                    }
                });

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allGranted = true;
                    for (Boolean granted : permissions.values()) {
                        if (!granted) {
                            allGranted = false;
                            break;
                        }
                    }
                });
    }

    private void sendTextMessage() {
        String messageText = binding.etMessage.getText() != null ?
                binding.etMessage.getText().toString().trim() : "";

        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        binding.etMessage.setText("");

        if (isGroupChat) {
            chatService.sendGroupMessage(chatId, messageText, new ChatService.MessageCallback() {
                @Override
                public void onSuccess(Message message) {
                    scrollToBottom();
                }

                @Override
                public void onFailure(Exception e) {
                    CookieBarToastHelper.showError(ChatActivity.this, "Error",
                            "Failed to send message", CookieBarToastHelper.LONG_DURATION);
                }
            });
        } else {
            chatService.sendMessage(chatId, otherUserId, messageText, new ChatService.MessageCallback() {
                @Override
                public void onSuccess(Message message) {
                    scrollToBottom();
                }

                @Override
                public void onFailure(Exception e) {
                    CookieBarToastHelper.showError(ChatActivity.this, "Error",
                            "Failed to send message", CookieBarToastHelper.LONG_DURATION);
                }
            });
        }
    }

    private void showAttachmentBottomSheet() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        BottomSheetImagePickerBinding bottomSheetBinding =
                BottomSheetImagePickerBinding.inflate(getLayoutInflater());

        bottomSheetBinding.layoutCamera.setOnClickListener(v -> {
            bottomSheet.dismiss();
            openCamera();
        });

        bottomSheetBinding.layoutGallery.setOnClickListener(v -> {
            bottomSheet.dismiss();
            openGallery();
        });

        bottomSheetBinding.layoutFile.setOnClickListener(v -> {
            bottomSheet.dismiss();
            openFilePicker();
        });

        bottomSheet.setContentView(bottomSheetBinding.getRoot());
        bottomSheet.show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
            return;
        }

        try {
            File photoFile = new File(getExternalFilesDir(null), "chat_image_" + System.currentTimeMillis() + ".jpg");
            cameraImagePath = photoFile.getAbsolutePath();
            cameraImageUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(cameraImageUri);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(new String[]{Manifest.permission.READ_MEDIA_IMAGES});
            return;
        }

        galleryLauncher.launch("image/*");
    }

    private void openFilePicker() {
        filePickerLauncher.launch("*/*");
    }

    private void uploadImage(Uri imageUri) {
        CookieBarToastHelper.showInfo(this, "Uploading", "Uploading image...",
                CookieBarToastHelper.SHORT_DURATION);

        storageService.uploadImage(imageUri, chatId, new FirebaseStorageService.StorageCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                chatService.sendImageMessage(chatId, otherUserId, downloadUrl,
                        new ChatService.MessageCallback() {
                            @Override
                            public void onSuccess(Message message) {
                                scrollToBottom();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                CookieBarToastHelper.showError(ChatActivity.this, "Error",
                                        "Failed to send image", CookieBarToastHelper.LONG_DURATION);
                            }
                        });
            }

            @Override
            public void onFailure(Exception exception) {
                CookieBarToastHelper.showError(ChatActivity.this, "Error",
                        "Failed to upload image", CookieBarToastHelper.LONG_DURATION);
            }
        });
    }

    private void uploadFile(Uri fileUri) {
        String fileName = getFileName(fileUri);
        CookieBarToastHelper.showInfo(this, "Uploading", "Uploading file...",
                CookieBarToastHelper.SHORT_DURATION);

        storageService.uploadFile(fileUri, chatId, fileName, new FirebaseStorageService.StorageCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                chatService.sendFileMessage(chatId, otherUserId, downloadUrl, fileName,
                        new ChatService.MessageCallback() {
                            @Override
                            public void onSuccess(Message message) {
                                scrollToBottom();
                            }

                            @Override
                            public void onFailure(Exception e) {
                                CookieBarToastHelper.showError(ChatActivity.this, "Error",
                                        "Failed to send file", CookieBarToastHelper.LONG_DURATION);
                            }
                        });
            }

            @Override
            public void onFailure(Exception exception) {
                CookieBarToastHelper.showError(ChatActivity.this, "Error",
                        "Failed to upload file", CookieBarToastHelper.LONG_DURATION);
            }
        });
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result != null ? result : "file";
    }

    private void loadMessages() {
        chatService.getChatMessages(chatId, new ChatService.MessagesCallback() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (isFinishing() || binding == null || messageAdapter == null) {
                    return;
                }

                try {
                    messageAdapter.updateMessages(messages);
                    scrollToBottom();
                } catch (Exception e) {
                    android.util.Log.e("ChatActivity", "Error updating messages", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (!isFinishing() && binding != null) {
                    android.util.Log.e("ChatActivity", "Failed to load messages", e);
                }
            }
        });
    }

    private void markMessagesAsSeen() {
        if (isGroupChat) {
            chatService.markGroupChatAsSeen(chatId, currentUserId);
        } else {
            chatService.markAsSeen(chatId, currentUserId);
        }
    }

    private void scrollToBottom() {
        if (isFinishing() || binding == null) {
            return;
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (isDestroyed()) {
                    return;
                }
            }
        } catch (Exception e) {
        }

        if (binding.recyclerViewMessages == null) {
            return;
        }

        if (messageAdapter == null) {
            return;
        }

        try {
            binding.recyclerViewMessages.post(() -> {
                if (isFinishing() || binding == null || binding.recyclerViewMessages == null) {
                    return;
                }

                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        if (isDestroyed()) {
                            return;
                        }
                    }
                } catch (Exception e) {
                }

                if (messageAdapter != null && messageAdapter.getItemCount() > 0) {
                    try {
                        binding.recyclerViewMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                    } catch (Exception e) {
                        android.util.Log.e("ChatActivity", "Error scrolling to bottom", e);
                    }
                }
            });
        } catch (Exception e) {
            android.util.Log.e("ChatActivity", "Error posting scroll to bottom", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatService.updateLastSeen(this);
        markMessagesAsSeen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
