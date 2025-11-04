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
import com.sendajapan.sendasnap.models.ChatUser;
import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    
    private List<ChatUser> users;
    private OnContactClickListener listener;
    
    public interface OnContactClickListener {
        void onContactClick(ChatUser user);
    }
    
    public ContactAdapter(OnContactClickListener listener) {
        this.users = new ArrayList<>();
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        ChatUser user = users.get(position);
        holder.bind(user);
    }
    
    @Override
    public int getItemCount() {
        return users.size();
    }
    
    public void updateUsers(List<ChatUser> newUsers) {
        this.users.clear();
        this.users.addAll(newUsers);
        notifyDataSetChanged();
    }
    
    public void filterUsers(String query) {
        // This will be handled by the activity, but we provide the method for consistency
        notifyDataSetChanged();
    }
    
    class ContactViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProfile;
        private TextView txtUserName;
        private TextView txtUserEmail;
        
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            
            imgProfile = itemView.findViewById(R.id.imgProfile);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserEmail = itemView.findViewById(R.id.txtUserEmail);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onContactClick(users.get(position));
                }
            });
        }
        
        public void bind(ChatUser user) {
            txtUserName.setText(user.getUsername());
            txtUserEmail.setText(user.getEmail());
            
            // Load profile image
            if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(user.getProfileImageUrl())
                        .placeholder(R.drawable.car_placeholder)
                        .circleCrop()
                        .into(imgProfile);
            } else {
                imgProfile.setImageResource(R.drawable.car_placeholder);
            }
        }
    }
}

