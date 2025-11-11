package com.sendajapan.sendasnap.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.models.TaskAttachment;

import java.util.List;

public class TaskAttachmentAdapter extends RecyclerView.Adapter<TaskAttachmentAdapter.AttachmentViewHolder> {

    private final List<TaskAttachment> attachments;
    private OnAttachmentRemoveListener removeListener;
    private boolean showRemoveButton;

    public interface OnAttachmentRemoveListener {
        void onRemove(int position, TaskAttachment attachment);
    }

    public TaskAttachmentAdapter(List<TaskAttachment> attachments, boolean showRemoveButton) {
        this.attachments = attachments;
        this.showRemoveButton = showRemoveButton;
    }

    public void setOnAttachmentRemoveListener(OnAttachmentRemoveListener listener) {
        this.removeListener = listener;
    }

    @NonNull
    @Override
    public AttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_attachment, parent, false);
        return new AttachmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttachmentViewHolder holder, int position) {
        TaskAttachment attachment = attachments.get(position);
        holder.bind(attachment, position);
    }

    @Override
    public int getItemCount() {
        return attachments.size();
    }

    class AttachmentViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgFileIcon;
        private TextView txtFileName;
        private TextView txtFileSize;
        private MaterialButton btnRemoveFile;

        AttachmentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFileIcon = itemView.findViewById(R.id.imgFileIcon);
            txtFileName = itemView.findViewById(R.id.txtFileName);
            txtFileSize = itemView.findViewById(R.id.txtFileSize);
            btnRemoveFile = itemView.findViewById(R.id.btnRemoveFile);
        }

        void bind(TaskAttachment attachment, int position) {
            if (attachment != null) {
                txtFileName.setText(attachment.getFileName() != null ? attachment.getFileName() : "");
                txtFileSize.setText(attachment.getFormattedFileSize());

                // Show/hide remove button
                btnRemoveFile.setVisibility(showRemoveButton ? View.VISIBLE : View.GONE);

                // Set remove button click listener
                if (showRemoveButton) {
                    btnRemoveFile.setOnClickListener(v -> {
                        if (removeListener != null) {
                            removeListener.onRemove(position, attachment);
                        }
                    });
                }
            }
        }
    }
}

