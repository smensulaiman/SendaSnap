package com.sendajapan.sendasnap.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.models.Chat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RecentChatAdapter extends RecyclerView.Adapter<RecentChatAdapter.ChatViewHolder> {
    
    private List<Chat> chats;
    private OnChatClickListener listener;
    
    public interface OnChatClickListener {
        void onChatClick(Chat chat);
    }
    
    public RecentChatAdapter(OnChatClickListener listener) {
        this.chats = new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent_chat, parent, false);
        return new ChatViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chats.get(position);
        holder.bind(chat);
    }
    
    @Override
    public int getItemCount() {
        return chats.size();
    }
    
    public void updateChats(List<Chat> newChats) {
        this.chats.clear();
        this.chats.addAll(newChats);
        notifyDataSetChanged();
    }
    
    class ChatViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProfile;
        private TextView txtUserName;
        private TextView txtLastMessage;
        private TextView txtTime;
        private TextView txtUnreadCount;
        
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtLastMessage = itemView.findViewById(R.id.txtLastMessage);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtUnreadCount = itemView.findViewById(R.id.txtUnreadCount);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onChatClick(chats.get(position));
                }
            });
        }
        
        public void bind(Chat chat) {
            txtUserName.setText(chat.getOtherUserName());
            txtLastMessage.setText(chat.getLastMessage());
            
            // Format time
            if (chat.getLastMessageTime() > 0) {
                txtTime.setText(formatTime(chat.getLastMessageTime()));
            } else {
                txtTime.setText("");
            }
            
            // Show unread count
            if (chat.getUnreadCount() > 0) {
                txtUnreadCount.setText(String.valueOf(chat.getUnreadCount()));
                txtUnreadCount.setVisibility(View.VISIBLE);
            } else {
                txtUnreadCount.setVisibility(View.GONE);
            }
            
            // Load profile image
            if (chat.getOtherUserProfileImageUrl() != null && !chat.getOtherUserProfileImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(chat.getOtherUserProfileImageUrl())
                        .placeholder(R.drawable.car_placeholder)
                        .circleCrop()
                        .into(imgProfile);
            } else {
                imgProfile.setImageResource(R.drawable.car_placeholder);
            }
        }
        
        private String formatTime(long timestamp) {
            Date date = new Date(timestamp);
            SimpleDateFormat sdf;
            
            long now = System.currentTimeMillis();
            long diff = now - timestamp;
            
            if (diff < 86400000) { // Less than 24 hours
                sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            } else if (diff < 604800000) { // Less than 7 days
                sdf = new SimpleDateFormat("EEE h:mm a", Locale.getDefault());
            } else {
                sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            }
            
            return sdf.format(date);
        }
    }
}

