package com.sendajapan.sendasnap.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.adapters.MessageAdapter;
import com.sendajapan.sendasnap.databinding.ActivityChatBinding;
import com.sendajapan.sendasnap.databinding.BottomSheetImagePickerBinding;
import com.sendajapan.sendasnap.models.Message;
import com.sendajapan.sendasnap.services.ChatService;
import com.sendajapan.sendasnap.services.FirebaseStorageService;
import com.sendajapan.sendasnap.utils.FirebaseUtils;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;
import com.sendajapan.sendasnap.utils.CookieBarToastHelper;
import java.io.File;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private MessageAdapter messageAdapter;
    private ChatService chatService;
    private FirebaseStorageService storageService;
    private HapticFeedbackHelper hapticHelper;
    
    private String chatId;
    private String otherUserId;
    private String otherUserName;
    private String otherUserEmail;
    private String currentUserId;
    
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String> filePickerLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private Uri cameraImageUri;
    private String cameraImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
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
        getIntentData();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        setupActivityResultLaunchers();
        loadMessages();
        markMessagesAsSeen();
    }

    private void initHelpers() {
        chatService = ChatService.getInstance();
        storageService = FirebaseStorageService.getInstance();
        hapticHelper = HapticFeedbackHelper.getInstance(this);
        currentUserId = FirebaseUtils.getCurrentUserId(this);
    }

    private void getIntentData() {
        chatId = getIntent().getStringExtra("chatId");
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
        binding.toolbar.setTitle(otherUserName != null ? otherUserName : "Chat");
        binding.toolbar.setNavigationOnClickListener(v -> {
            hapticHelper.vibrateClick();
            finish();
        });
    }

    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(currentUserId);
        messageAdapter.setOnImageClickListener(imageUrl -> {
            // Open full-screen image viewer (can be implemented later)
            Toast.makeText(this, "Image viewer coming soon", Toast.LENGTH_SHORT).show();
        });
        messageAdapter.setOnFileClickListener((fileUrl, fileName) -> {
            // Download/open file (can be implemented later)
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
        
        // Send on Enter key
        binding.etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEND) {
                sendTextMessage();
                return true;
            }
            return false;
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
                    if (allGranted) {
                        // Retry action after permission granted
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
        
        chatService.sendMessage(chatId, otherUserId, messageText, new ChatService.MessageCallback() {
            @Override
            public void onSuccess(Message message) {
                // Message will be added via real-time listener
                scrollToBottom();
            }

            @Override
            public void onFailure(Exception e) {
                CookieBarToastHelper.showError(ChatActivity.this, "Error", 
                        "Failed to send message", CookieBarToastHelper.LONG_DURATION);
            }
        });
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
                messageAdapter.updateMessages(messages);
                scrollToBottom();
            }

            @Override
            public void onFailure(Exception e) {
                // Handle error
            }
        });
    }

    private void markMessagesAsSeen() {
        chatService.markAsSeen(chatId, currentUserId);
    }

    private void scrollToBottom() {
        binding.recyclerViewMessages.post(() -> {
            if (messageAdapter.getItemCount() > 0) {
                binding.recyclerViewMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
            }
        });
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

