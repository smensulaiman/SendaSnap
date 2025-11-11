package com.sendajapan.sendasnap.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.models.UserData;

import java.util.List;

public class UserSpinnerAdapter extends ArrayAdapter<UserData> {

    private final LayoutInflater inflater;
    private final List<UserData> users;

    public UserSpinnerAdapter(@NonNull Context context, @NonNull List<UserData> users) {
        super(context, 0, users);
        this.inflater = LayoutInflater.from(context);
        this.users = users;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // For the selected item in the text field, show simple text view
        TextView textView;
        if (convertView instanceof TextView) {
            textView = (TextView) convertView;
        } else {
            textView = new TextView(getContext());
            textView.setPadding(0, 0, 0, 0);
            textView.setTextSize(14);
            textView.setTextColor(getContext().getResources().getColor(R.color.text_black, null));
        }
        
        UserData user = users.get(position);
        if (user != null) {
            textView.setText(user.getName() != null ? user.getName() : "");
        }
        
        return textView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // For dropdown items, show custom layout with avatar, name, and role
        return getCustomView(position, convertView, parent);
    }

    private View getCustomView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_user_spinner, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        UserData user = users.get(position);
        if (user != null) {
            holder.txtUserName.setText(user.getName() != null ? user.getName() : "");
            holder.txtUserRole.setText(user.getRole() != null ? capitalizeFirst(user.getRole()) : "");

            // Load avatar image
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                Glide.with(getContext())
                        .load(user.getAvatar())
                        .placeholder(R.drawable.ic_user_24)
                        .error(R.drawable.ic_user_24)
                        .circleCrop()
                        .into(holder.imgUserAvatar);
            } else {
                holder.imgUserAvatar.setImageResource(R.drawable.ic_user_24);
            }
        }

        return convertView;
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private static class ViewHolder {
        ImageView imgUserAvatar;
        TextView txtUserName;
        TextView txtUserRole;

        ViewHolder(View view) {
            imgUserAvatar = view.findViewById(R.id.imgUserAvatar);
            txtUserName = view.findViewById(R.id.txtUserName);
            txtUserRole = view.findViewById(R.id.txtUserRole);
        }
    }
}

