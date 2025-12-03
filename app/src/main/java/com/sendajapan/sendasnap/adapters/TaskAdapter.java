package com.sendajapan.sendasnap.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.models.Task;
import com.sendajapan.sendasnap.models.UserData;
import com.sendajapan.sendasnap.services.ChatService;
import com.sendajapan.sendasnap.utils.FirebaseUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> tasks;
    private final OnTaskClickListener listener;
    private final ChatService chatService;
    private final String currentUserId;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public interface OnTaskLongClickListener {
        void onTaskLongClick(Task task);
    }

    private OnTaskLongClickListener longClickListener;

    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
        this.chatService = ChatService.getInstance();
        this.currentUserId = FirebaseUtils.getCurrentUserId(null);
    }

    public void setOnTaskLongClickListener(OnTaskLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);
    }
    
    @Override
    public void onViewRecycled(@NonNull TaskViewHolder holder) {
        super.onViewRecycled(holder);
        // Clean up listener when view is recycled
        holder.removeUnreadCountListener();
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView cardView;
        private final View statusIndicator;
        private final TextView textTaskTitle;
        private final TextView textTaskDescription;
        private final TextView textTaskTime;
        private final Chip chipTaskStatus;
        private final ImageView imgAttachment;
        private final LinearLayout layoutAssigneeAvatars;
        private final ImageView imgCreatorAvatar;
        private final TextView textCreatorName;
        private final TextView textCreatedByLabel;
        private final TextView badgeUnreadCount;
        private ValueEventListener unreadCountListener;
        private String currentTaskChatId;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = (MaterialCardView) itemView;
            statusIndicator = itemView.findViewById(R.id.statusIndicator);
            textTaskTitle = itemView.findViewById(R.id.textTaskTitle);
            textTaskDescription = itemView.findViewById(R.id.textTaskDescription);
            textTaskTime = itemView.findViewById(R.id.textTaskTime);
            chipTaskStatus = itemView.findViewById(R.id.chipTaskStatus);
            imgAttachment = itemView.findViewById(R.id.imgAttachment);
            layoutAssigneeAvatars = itemView.findViewById(R.id.layoutAssigneeAvatars);
            imgCreatorAvatar = itemView.findViewById(R.id.imgCreatorAvatar);
            textCreatorName = itemView.findViewById(R.id.textCreatorName);
            textCreatedByLabel = itemView.findViewById(R.id.textCreatedByLabel);
            badgeUnreadCount = itemView.findViewById(R.id.badgeUnreadCount);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTaskClick(tasks.get(getAdapterPosition()));
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onTaskLongClick(tasks.get(getAdapterPosition()));
                    return true;
                }
                return false;
            });
        }

        public void bind(Task task) {
            // Remove previous listener if exists
            removeUnreadCountListener();
            
            textTaskTitle.setText(task.getTitle());
            textTaskDescription.setText(task.getDescription());
            
            // Format time to 12-hour format
            textTaskTime.setText(formatTimeTo12Hour(task.getWorkTime()));

            setStatusColors(task.getStatus());

            if (task.getAttachments() != null && !task.getAttachments().isEmpty()) {
                imgAttachment.setVisibility(View.VISIBLE);
            } else {
                imgAttachment.setVisibility(View.GONE);
            }

            // Load assignee avatars with overlapping style
            loadAssigneeAvatars(task.getAssignees());

            // Load creator information
            loadCreatorInfo(task.getCreator());
            
            // Load unread count for this task's chat
            loadUnreadCount(task);
        }
        
        private void loadUnreadCount(Task task) {
            if (task == null || currentUserId == null || currentUserId.isEmpty()) {
                if (badgeUnreadCount != null) {
                    badgeUnreadCount.setVisibility(View.GONE);
                }
                return;
            }
            
            String chatId = "task_" + String.valueOf(task.getId());
            currentTaskChatId = chatId;
            
            // First get current count immediately
            chatService.getGroupChatUnreadCount(chatId, currentUserId, new ChatService.UnreadCountCallback() {
                @Override
                public void onSuccess(int unreadCount) {
                    updateBadge(unreadCount);
                }

                @Override
                public void onFailure(Exception e) {
                    // Silently fail, but still set up listener
                }
            });
            
            // Then add listener for real-time updates
            unreadCountListener = chatService.addUnreadCountListener(chatId, currentUserId, new ChatService.UnreadCountCallback() {
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
            if (badgeUnreadCount != null) {
                if (unreadCount > 0) {
                    badgeUnreadCount.setText(String.valueOf(unreadCount > 99 ? "99+" : unreadCount));
                    badgeUnreadCount.setVisibility(View.VISIBLE);
                } else {
                    badgeUnreadCount.setVisibility(View.GONE);
                }
            }
        }
        
        private void removeUnreadCountListener() {
            if (unreadCountListener != null && currentTaskChatId != null && currentUserId != null && !currentUserId.isEmpty()) {
                chatService.removeUnreadCountListener(currentTaskChatId, currentUserId, unreadCountListener);
                unreadCountListener = null;
                currentTaskChatId = null;
            }
        }

        private void loadAssigneeAvatars(List<UserData> assignees) {
            layoutAssigneeAvatars.removeAllViews();
            
            if (assignees == null || assignees.isEmpty()) {
                layoutAssigneeAvatars.setVisibility(View.GONE);
                return;
            }

            layoutAssigneeAvatars.setVisibility(View.VISIBLE);
            Context context = itemView.getContext();
            
            // Show max 4 avatars, with overlap
            int maxAvatars = Math.min(assignees.size(), 4);
            int overlapOffset = -8; // Negative margin for overlap (in dp, converted to pixels)
            int avatarSize = 26; // 26dp as per Design 3
            
            for (int i = 0; i < maxAvatars; i++) {
                UserData assignee = assignees.get(i);
                View avatarContainer = createAvatarImageView(context, avatarSize, i > 0 ? overlapOffset : 0);
                ImageView avatarView = (ImageView) avatarContainer.getTag();
                
                // Load avatar with Glide - prefer avatarUrl, fallback to avatar
                String avatarUrl = assignee.getAvatarUrl() != null ? assignee.getAvatarUrl() : assignee.getAvatar();

                if (isValidUrl(avatarUrl)) {
                    Glide.with(context)
                            .load(avatarUrl)
                            .placeholder(R.drawable.avater_placeholder)
                            .error(R.drawable.avater_placeholder)
                            .circleCrop()
                            .into(avatarView);
                } else {
                    // Set placeholder directly if URL is invalid
                    avatarView.setImageResource(R.drawable.avater_placeholder);
                }
                
                layoutAssigneeAvatars.addView(avatarContainer);
            }

            // Show "+N" badge if there are more assignees
            if (assignees.size() > maxAvatars) {
                TextView badgeView = createMoreBadge(context, assignees.size() - maxAvatars, overlapOffset);
                layoutAssigneeAvatars.addView(badgeView);
            }
        }

        private View createAvatarImageView(Context context, int sizeDp, int marginStartDp) {
            // Create FrameLayout wrapper for border
            FrameLayout container = new FrameLayout(context);
            int sizePx = (int) (sizeDp * context.getResources().getDisplayMetrics().density);
            int marginPx = (int) (marginStartDp * context.getResources().getDisplayMetrics().density);
            
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(sizePx, sizePx);
            containerParams.setMarginStart(marginPx);
            container.setLayoutParams(containerParams);
            
            // Add white border background
            container.setBackgroundResource(R.drawable.avatar_border_circle);
            
            // Create ImageView for the actual image
            ImageView imageView = new ImageView(context);
            int borderWidthPx = (int) (2 * context.getResources().getDisplayMetrics().density); // 2dp border width
            FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                    sizePx - (borderWidthPx * 2), 
                    sizePx - (borderWidthPx * 2)
            );
            imageParams.gravity = android.view.Gravity.CENTER;
            imageView.setLayoutParams(imageParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setClipToOutline(true);
            
            container.addView(imageView);
            
            // Return container but we need to access the ImageView for Glide
            // So we'll tag it
            container.setTag(imageView);
            
            return container;
        }

        private TextView createMoreBadge(Context context, int count, int marginStartDp) {
            TextView badgeView = new TextView(context);
            int sizeDp = 26;
            int sizePx = (int) (sizeDp * context.getResources().getDisplayMetrics().density);
            int marginPx = (int) (marginStartDp * context.getResources().getDisplayMetrics().density);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizePx, sizePx);
            params.setMarginStart(marginPx);
            badgeView.setLayoutParams(params);
            
            badgeView.setText("+" + count);
            badgeView.setTextSize(10);
            badgeView.setTextColor(context.getColor(R.color.white));
            badgeView.setGravity(android.view.Gravity.CENTER);
            badgeView.setBackgroundResource(R.drawable.badge_more_circle);
            
            return badgeView;
        }

        private void loadCreatorInfo(UserData creator) {
            if (creator == null) {
                textCreatedByLabel.setVisibility(View.GONE);
                imgCreatorAvatar.setVisibility(View.GONE);
                textCreatorName.setVisibility(View.GONE);
                return;
            }

            textCreatedByLabel.setVisibility(View.VISIBLE);
            imgCreatorAvatar.setVisibility(View.VISIBLE);
            textCreatorName.setVisibility(View.VISIBLE);

            // Set creator name
            textCreatorName.setText(creator.getName() != null ? creator.getName() : "");

            // Load creator avatar
            Context context = itemView.getContext();
            String avatarUrl = creator.getAvatarUrl() != null ? creator.getAvatarUrl() : creator.getAvatar();
            if (isValidUrl(avatarUrl)) {
                Glide.with(context)
                        .load(avatarUrl)
                        .placeholder(R.drawable.avater_placeholder)
                        .error(R.drawable.avater_placeholder)
                        .circleCrop()
                        .into(imgCreatorAvatar);
            } else {
                // Set placeholder directly if URL is invalid
                imgCreatorAvatar.setImageResource(R.drawable.avater_placeholder);
            }
        }

        private boolean isValidUrl(String url) {
            if (url == null || url.trim().isEmpty()) {
                return false;
            }
            
            // Check if it's a valid HTTP/HTTPS URL
            try {
                URI uri = new URI(url);
                String scheme = uri.getScheme();
                if (scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
                    return true;
                }
            } catch (URISyntaxException e) {
                // Not a valid URI
            }
            
            // If it's not a valid HTTP/HTTPS URL, don't try to load it
            return false;
        }

        private String formatTimeTo12Hour(String time24Hour) {
            if (time24Hour == null || time24Hour.trim().isEmpty()) {
                return "";
            }

            try {
                // Try parsing 24-hour format (HH:mm)
                SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Date date = inputFormat.parse(time24Hour.trim());
                
                // Format to 12-hour with AM/PM
                SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                return outputFormat.format(date);
            } catch (ParseException e) {
                // If parsing fails, try other common formats
                try {
                    SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    Date date = inputFormat.parse(time24Hour.trim());
                    SimpleDateFormat outputFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                    return outputFormat.format(date);
                } catch (ParseException e2) {
                    // If all parsing fails, return original string
                    return time24Hour;
                }
            }
        }

        private void setStatusColors(Task.TaskStatus status) {
            Context context = itemView.getContext();
            cardView.setCardBackgroundColor(context.getColor(R.color.white));
            cardView.setStrokeColor(context.getColor(R.color.primary));

            switch (status) {
                case RUNNING:
                    // Status indicator
                    statusIndicator.setBackgroundColor(context.getColor(R.color.primary));
                    // Chip
                    chipTaskStatus.setText("Running");
                    chipTaskStatus.setTextColor(context.getColorStateList(R.color.white));
                    chipTaskStatus.setChipBackgroundColor(context.getColorStateList(R.color.primary));
                    chipTaskStatus.setChipStrokeColor(context.getColorStateList(R.color.primary));
                    break;
                case PENDING:
                    // Status indicator
                    statusIndicator.setBackgroundColor(context.getColor(R.color.warning_dark));
                    // Chip
                    chipTaskStatus.setText("Pending");
                    chipTaskStatus.setTextColor(context.getColorStateList(R.color.black));
                    chipTaskStatus.setChipBackgroundColor(context.getColorStateList(R.color.warning_medium));
                    chipTaskStatus.setChipStrokeColor(context.getColorStateList(R.color.warning_dark));
                    break;
                case COMPLETED:
                    // Status indicator
                    statusIndicator.setBackgroundColor(context.getColor(R.color.success_dark));
                    // Chip
                    chipTaskStatus.setText("Completed");
                    chipTaskStatus.setChipBackgroundColor(context.getColorStateList(R.color.success_medium));
                    chipTaskStatus.setChipStrokeColor(context.getColorStateList(R.color.success_dark));
                    break;
                case CANCELLED:
                    // Status indicator
                    statusIndicator.setBackgroundColor(context.getColor(R.color.error_dark));
                    // Chip
                    chipTaskStatus.setText("Cancelled");
                    chipTaskStatus.setChipBackgroundColor(context.getColorStateList(R.color.error_medium));
                    chipTaskStatus.setChipStrokeColor(context.getColorStateList(R.color.error_dark));
                    break;
            }
        }
    }
}
