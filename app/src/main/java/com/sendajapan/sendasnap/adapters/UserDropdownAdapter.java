package com.sendajapan.sendasnap.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.models.UserData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserDropdownAdapter extends RecyclerView.Adapter<UserDropdownAdapter.UserViewHolder> {

    private final List<UserData> users;
    private final Set<String> selectedUserNames;
    private OnUserClickListener listener;

    public interface OnUserClickListener {
        void onUserClick(UserData user, boolean isSelected);
    }

    public UserDropdownAdapter(List<UserData> users) {
        this.users = users;
        this.selectedUserNames = new HashSet<>();
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedUsers(List<String> selectedNames) {
        selectedUserNames.clear();
        if (selectedNames != null) {
            selectedUserNames.addAll(selectedNames);
        }

        notifyDataSetChanged();
    }

    public Set<String> getSelectedUserNames() {
        return new HashSet<>(selectedUserNames);
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_dropdown, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserData user = users.get(position);
        boolean isSelected = user.getName() != null && selectedUserNames.contains(user.getName());
        holder.bind(user, isSelected);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgUserAvatar;
        private TextView txtUserName;
        private TextView txtUserRole;
        private CheckBox checkBoxSelected;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);

            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
            txtUserName = itemView.findViewById(R.id.txtUserName);
            txtUserRole = itemView.findViewById(R.id.txtUserRole);
            checkBoxSelected = itemView.findViewById(R.id.checkBoxSelected);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    UserData user = users.get(position);
                    boolean isSelected = user.getName() != null && selectedUserNames.contains(user.getName());
                    if (isSelected) {
                        selectedUserNames.remove(user.getName());
                    } else {
                        if (user.getName() != null) {
                            selectedUserNames.add(user.getName());
                        }
                    }
                    notifyItemChanged(position);
                    listener.onUserClick(user, !isSelected);
                }
            });
        }

        void bind(UserData user, boolean isSelected) {
            if (user != null) {
                txtUserName.setText(user.getName() != null ? user.getName() : "");
                txtUserRole.setText(user.getRole() != null ? capitalizeFirst(user.getRole()) : "");
                checkBoxSelected.setChecked(isSelected);

                // Load avatar image - prefer avatarUrl, fallback to avatar
                String avatarUrl = user.getAvatarUrl() != null ? user.getAvatarUrl() : user.getAvatar();
                
                if (avatarUrl != null && !avatarUrl.isEmpty() && isValidUrl(avatarUrl)) {
                    Glide.with(itemView.getContext())
                            .load(avatarUrl)
                            .placeholder(R.drawable.avater_placeholder)
                            .error(R.drawable.avater_placeholder)
                            .circleCrop()
                            .into(imgUserAvatar);
                } else {
                    imgUserAvatar.setImageResource(R.drawable.avater_placeholder);
                }
            }
        }

        private boolean isValidUrl(String url) {
            return url != null && (url.startsWith("http://") || url.startsWith("https://"));
        }

        private String capitalizeFirst(String str) {
            if (str == null || str.isEmpty()) {
                return str;
            }
            return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
        }
    }
}

