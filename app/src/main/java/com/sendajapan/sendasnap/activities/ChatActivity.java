package com.sendajapan.sendasnap.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sendajapan.sendasnap.MyApplication;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.adapters.MessageAdapter;
import com.sendajapan.sendasnap.databinding.ActivityChatBinding;
import com.sendajapan.sendasnap.databinding.BottomSheetImagePickerBinding;
import com.sendajapan.sendasnap.models.Message;
import com.sendajapan.sendasnap.models.UserData;
import com.sendajapan.sendasnap.services.ChatService;
import com.sendajapan.sendasnap.services.FirebaseStorageService;
import com.sendajapan.sendasnap.utils.CookieBarToastHelper;
import com.sendajapan.sendasnap.utils.FirebaseUtils;
import com.sendajapan.sendasnap.utils.HapticFeedbackHelper;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity";

    private boolean isGroupChat = false;

    private ActivityChatBinding binding;
    private MessageAdapter messageAdapter;

    private ChatService chatService;
    private FirebaseStorageService storageService;
    private HapticFeedbackHelper hapticHelper;

    private String chatId;
    private String currentUserId;
    private String otherUserId;
    private String otherUserName;
    private String taskTitle;

    private List<UserData> participants = new ArrayList<>();

    private ActivityResultLauncher<String> filePickerLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;

    private Uri cameraImageUri;

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
        setupKeyboardHandling();
        setupMemberAvatars();
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

        if (currentUserId.isEmpty()) {
            android.util.Log.w(TAG, "Current user ID is null or empty");
        }
    }

    private void initializeCurrentUser() {
        try {
            chatService.initializeUser(this);
        } catch (Exception e) {
            android.util.Log.e(TAG, "Failed to initialize user in Firebase", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void getIntentData() {
        chatId = getIntent().getStringExtra("chatId");
        isGroupChat = getIntent().getBooleanExtra("isGroupChat", false);
        taskTitle = getIntent().getStringExtra("taskTitle");
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUserName = getIntent().getStringExtra("otherUserName");

        Serializable participantsSerializable = getIntent().getSerializableExtra("participants");
        if (participantsSerializable instanceof List) {
            try {
                participants = (List<UserData>) participantsSerializable;
            } catch (ClassCastException e) {
                android.util.Log.e(TAG, "Failed to cast participants list", e);
                participants = new ArrayList<>();
            }
        }
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
            TextView titleView = findTitleTextView(binding.toolbar);
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

    private TextView findTitleTextView(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof TextView) {
                TextView textView = (TextView) child;
                String toolbarTitle = binding.toolbar.getTitle() != null ? binding.toolbar.getTitle().toString() : "";
                String textViewText = textView.getText() != null ? textView.getText().toString() : "";
                if (textViewText.equals(toolbarTitle) && textView.getVisibility() == View.VISIBLE) {
                    return textView;
                }
            } else if (child instanceof ViewGroup) {
                TextView found = findTitleTextView((ViewGroup) child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void setupKeyboardHandling() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.cardInput, (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.cardInput
                    .getLayoutParams();
            if (insets.bottom > 200) {
                params.bottomMargin = insets.bottom - 80;
            } else {
                params.bottomMargin = (int) (8 * getResources().getDisplayMetrics().density);
            }

            binding.cardInput.setLayoutParams(params);

            return windowInsets;
        });
    }

    @SuppressLint("SetTextI18n")
    private void setupMemberAvatars() {
        if (!isGroupChat || participants == null || participants.isEmpty()) {
            binding.layoutMemberAvatars.setVisibility(View.GONE);
            return;
        }

        binding.layoutMemberAvatars.setVisibility(View.VISIBLE);
        binding.layoutMemberAvatars.removeAllViews();

        int avatarSize = (int) (18 * getResources().getDisplayMetrics().density);
        int overlapOffset = (int) (-4 * getResources().getDisplayMetrics().density);
        int maxAvatars = 5;

        int displayCount = Math.min(participants.size(), maxAvatars);

        for (int i = 0; i < displayCount; i++) {
            UserData user = participants.get(i);
            ImageView avatarView = new ImageView(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(avatarSize, avatarSize);
            if (i > 0) {
                params.setMargins(overlapOffset, 0, 0, 0);
            }
            avatarView.setLayoutParams(params);

            avatarView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            avatarView.setBackgroundResource(R.drawable.avater_placeholder);

            String avatarUrl = user.getAvatarUrl() != null ? user.getAvatarUrl() : user.getAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty() && isValidUrl(avatarUrl)) {
                Glide.with(this)
                        .load(avatarUrl)
                        .placeholder(R.drawable.avater_placeholder)
                        .error(R.drawable.avater_placeholder)
                        .circleCrop()
                        .into(avatarView);
            } else {
                Glide.with(this)
                        .load(R.drawable.avater_placeholder)
                        .circleCrop()
                        .into(avatarView);
            }

            binding.layoutMemberAvatars.addView(avatarView);
        }

        if (participants.size() > maxAvatars) {
            TextView moreTextView = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(avatarSize, avatarSize);
            params.setMargins(overlapOffset, 0, 0, 0);
            moreTextView.setLayoutParams(params);
            moreTextView.setText("+" + (participants.size() - maxAvatars));
            moreTextView.setTextSize(10);
            moreTextView.setTextColor(getColor(R.color.white));
            moreTextView.setGravity(android.view.Gravity.CENTER);
            moreTextView.setBackgroundResource(R.drawable.avater_placeholder);
            moreTextView.setBackgroundTintList(getColorStateList(R.color.primary));
            binding.layoutMemberAvatars.addView(moreTextView);
        }
    }

    private boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
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
                binding.recyclerViewMessages.postDelayed(this::scrollToBottom, 100);
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
                });
    }

    private void sendTextMessage() {
        String messageText = binding.etMessage.getText() != null ? binding.etMessage.getText().toString().trim() : "";

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
        BottomSheetImagePickerBinding bottomSheetBinding = BottomSheetImagePickerBinding.inflate(getLayoutInflater());

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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(new String[] { Manifest.permission.CAMERA });
            return;
        }

        try {
            File photoFile = new File(getExternalFilesDir(null), "chat_image_" + System.currentTimeMillis() + ".jpg");
            cameraImageUri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", photoFile);
            cameraLauncher.launch(cameraImageUri);
        } catch (Exception e) {
            Toast.makeText(this, "Failed to open camera", Toast.LENGTH_SHORT).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void openGallery() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(new String[] { Manifest.permission.READ_MEDIA_IMAGES });
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
        if (Objects.equals(uri.getScheme(), "content")) {
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
                    updateEmptyState(messages.isEmpty());
                    scrollToBottom();
                } catch (Exception e) {
                    android.util.Log.e(TAG, "Error updating messages", e);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (!isFinishing() && binding != null) {
                    android.util.Log.e(TAG, "Failed to load messages", e);
                    updateEmptyState(true);
                }
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        if (binding == null) {
            return;
        }

        if (isEmpty) {
            binding.layoutEmptyState.setVisibility(View.VISIBLE);
            binding.recyclerViewMessages.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyState.setVisibility(View.GONE);
            binding.recyclerViewMessages.setVisibility(View.VISIBLE);
        }
    }

    private void markMessagesAsSeen() {
        if (isGroupChat) {
            chatService.markGroupChatAsSeen(chatId, currentUserId);
        } else {
            chatService.markAsSeen(chatId, currentUserId);
        }
    }

    private void scrollToBottom() {
        if (isFinishing() || binding == null || messageAdapter == null) {
            return;
        }

        binding.recyclerViewMessages.post(() -> {
            if (isFinishing() || binding == null || messageAdapter == null) {
                return;
            }

            int itemCount = messageAdapter.getItemCount();
            if (itemCount <= 0) {
                return;
            }

            try {
                binding.recyclerViewMessages.smoothScrollToPosition(itemCount - 1);
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error scrolling to bottom", e);
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
