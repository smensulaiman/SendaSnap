package com.sendajapan.sendasnap.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.sendajapan.sendasnap.R;
import com.sendajapan.sendasnap.models.Message;
import com.sendajapan.sendasnap.utils.FirebaseUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int TYPE_TEXT_SENT = 1;
    private static final int TYPE_TEXT_RECEIVED = 2;
    private static final int TYPE_IMAGE_SENT = 3;
    private static final int TYPE_IMAGE_RECEIVED = 4;
    private static final int TYPE_FILE_SENT = 5;
    private static final int TYPE_FILE_RECEIVED = 6;
    
    private List<Message> messages;
    private String currentUserId;
    private boolean isGroupChat;
    private OnImageClickListener imageClickListener;
    private OnFileClickListener fileClickListener;
    
    public interface OnImageClickListener {
        void onImageClick(String imageUrl);
    }
    
    public interface OnFileClickListener {
        void onFileClick(String fileUrl, String fileName);
    }
    
    public MessageAdapter(String currentUserId) {
        this.messages = new ArrayList<>();
        this.currentUserId = currentUserId;
        this.isGroupChat = false;
    }
    
    public MessageAdapter(String currentUserId, boolean isGroupChat) {
        this.messages = new ArrayList<>();
        this.currentUserId = currentUserId;
        this.isGroupChat = isGroupChat;
    }
    
    public void setOnImageClickListener(OnImageClickListener listener) {
        this.imageClickListener = listener;
    }
    
    public void setOnFileClickListener(OnFileClickListener listener) {
        this.fileClickListener = listener;
    }
    
    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        boolean isSent = message.getSenderId().equals(currentUserId);
        String messageType = message.getMessageType();
        
        if ("image".equals(messageType)) {
            return isSent ? TYPE_IMAGE_SENT : TYPE_IMAGE_RECEIVED;
        } else if ("file".equals(messageType)) {
            return isSent ? TYPE_FILE_SENT : TYPE_FILE_RECEIVED;
        } else {
            return isSent ? TYPE_TEXT_SENT : TYPE_TEXT_RECEIVED;
        }
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        
        switch (viewType) {
            case TYPE_IMAGE_SENT:
            case TYPE_IMAGE_RECEIVED:
                return new ImageMessageViewHolder(
                    inflater.inflate(R.layout.item_message_image, parent, false), viewType);
            case TYPE_FILE_SENT:
            case TYPE_FILE_RECEIVED:
                return new FileMessageViewHolder(
                    inflater.inflate(R.layout.item_message_file, parent, false), viewType);
            case TYPE_TEXT_SENT:
            case TYPE_TEXT_RECEIVED:
            default:
                return new TextMessageViewHolder(
                    inflater.inflate(R.layout.item_message, parent, false), viewType);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        
        if (holder instanceof TextMessageViewHolder) {
            ((TextMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ImageMessageViewHolder) {
            ((ImageMessageViewHolder) holder).bind(message);
        } else if (holder instanceof FileMessageViewHolder) {
            ((FileMessageViewHolder) holder).bind(message);
        }
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    public void updateMessages(List<Message> newMessages) {
        this.messages.clear();
        this.messages.addAll(newMessages);
        notifyDataSetChanged();
    }
    
    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }
    
    // Text Message ViewHolder
    class TextMessageViewHolder extends RecyclerView.ViewHolder {
        private View layoutSentMessage;
        private View layoutReceivedMessage;
        private TextView txtSentMessage;
        private TextView txtReceivedMessage;
        private TextView txtSentTime;
        private TextView txtReceivedTime;
        private ImageView imgSentStatus;
        
        public TextMessageViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            
            layoutSentMessage = itemView.findViewById(R.id.layoutSentMessage);
            layoutReceivedMessage = itemView.findViewById(R.id.layoutReceivedMessage);
            txtSentMessage = itemView.findViewById(R.id.txtSentMessage);
            txtReceivedMessage = itemView.findViewById(R.id.txtReceivedMessage);
            txtSentTime = itemView.findViewById(R.id.txtSentTime);
            txtReceivedTime = itemView.findViewById(R.id.txtReceivedTime);
            imgSentStatus = itemView.findViewById(R.id.imgSentStatus);
        }
        
        public void bind(Message message) {
            boolean isSent = message.getSenderId().equals(currentUserId);
            
            if (isSent) {
                layoutSentMessage.setVisibility(View.VISIBLE);
                layoutReceivedMessage.setVisibility(View.GONE);
                txtSentMessage.setText(message.getMessage());
                txtSentTime.setText(formatTime(message.getTimestamp()));
                
                // Update seen status
                if (message.isSeen()) {
                    imgSentStatus.setImageResource(R.drawable.ic_check);
                    imgSentStatus.setAlpha(1.0f); // Double checkmark (you can use a different icon)
                } else {
                    imgSentStatus.setImageResource(R.drawable.ic_check);
                    imgSentStatus.setAlpha(0.5f); // Single checkmark
                }
            } else {
                layoutSentMessage.setVisibility(View.GONE);
                layoutReceivedMessage.setVisibility(View.VISIBLE);
                txtReceivedMessage.setText(message.getMessage());
                txtReceivedTime.setText(formatTime(message.getTimestamp()));
            }
        }
    }
    
    // Image Message ViewHolder
    class ImageMessageViewHolder extends RecyclerView.ViewHolder {
        private View layoutSentImage;
        private View layoutReceivedImage;
        private ImageView imgSentImage;
        private ImageView imgReceivedImage;
        private TextView txtSentImageTime;
        private TextView txtReceivedImageTime;
        private ImageView imgSentImageStatus;
        private ProgressBar progressSentImage;
        
        public ImageMessageViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            
            layoutSentImage = itemView.findViewById(R.id.layoutSentImage);
            layoutReceivedImage = itemView.findViewById(R.id.layoutReceivedImage);
            imgSentImage = itemView.findViewById(R.id.imgSentImage);
            imgReceivedImage = itemView.findViewById(R.id.imgReceivedImage);
            txtSentImageTime = itemView.findViewById(R.id.txtSentImageTime);
            txtReceivedImageTime = itemView.findViewById(R.id.txtReceivedImageTime);
            imgSentImageStatus = itemView.findViewById(R.id.imgSentImageStatus);
            progressSentImage = itemView.findViewById(R.id.progressSentImage);
        }
        
        public void bind(Message message) {
            boolean isSent = message.getSenderId().equals(currentUserId);
            String imageUrl = message.getImageUrl();
            
            if (isSent) {
                layoutSentImage.setVisibility(View.VISIBLE);
                layoutReceivedImage.setVisibility(View.GONE);
                
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.car_placeholder)
                            .into(imgSentImage);
                    progressSentImage.setVisibility(View.GONE);
                } else {
                    progressSentImage.setVisibility(View.VISIBLE);
                }
                
                txtSentImageTime.setText(formatTime(message.getTimestamp()));
                
                if (message.isSeen()) {
                    imgSentImageStatus.setAlpha(1.0f);
                } else {
                    imgSentImageStatus.setAlpha(0.5f);
                }
                
                imgSentImage.setOnClickListener(v -> {
                    if (imageClickListener != null && imageUrl != null) {
                        imageClickListener.onImageClick(imageUrl);
                    }
                });
            } else {
                layoutSentImage.setVisibility(View.GONE);
                layoutReceivedImage.setVisibility(View.VISIBLE);
                
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.car_placeholder)
                            .into(imgReceivedImage);
                }
                
                txtReceivedImageTime.setText(formatTime(message.getTimestamp()));
                
                imgReceivedImage.setOnClickListener(v -> {
                    if (imageClickListener != null && imageUrl != null) {
                        imageClickListener.onImageClick(imageUrl);
                    }
                });
            }
        }
    }
    
    // File Message ViewHolder
    class FileMessageViewHolder extends RecyclerView.ViewHolder {
        private View layoutSentFile;
        private View layoutReceivedFile;
        private ImageView imgSentFileIcon;
        private ImageView imgReceivedFileIcon;
        private TextView txtSentFileName;
        private TextView txtReceivedFileName;
        private TextView txtSentFileSize;
        private TextView txtReceivedFileSize;
        private TextView txtSentFileTime;
        private TextView txtReceivedFileTime;
        private ImageView imgSentFileStatus;
        
        public FileMessageViewHolder(@NonNull View itemView, int viewType) {
            super(itemView);
            
            layoutSentFile = itemView.findViewById(R.id.layoutSentFile);
            layoutReceivedFile = itemView.findViewById(R.id.layoutReceivedFile);
            imgSentFileIcon = itemView.findViewById(R.id.imgSentFileIcon);
            imgReceivedFileIcon = itemView.findViewById(R.id.imgReceivedFileIcon);
            txtSentFileName = itemView.findViewById(R.id.txtSentFileName);
            txtReceivedFileName = itemView.findViewById(R.id.txtReceivedFileName);
            txtSentFileSize = itemView.findViewById(R.id.txtSentFileSize);
            txtReceivedFileSize = itemView.findViewById(R.id.txtReceivedFileSize);
            txtSentFileTime = itemView.findViewById(R.id.txtSentFileTime);
            txtReceivedFileTime = itemView.findViewById(R.id.txtReceivedFileTime);
            imgSentFileStatus = itemView.findViewById(R.id.imgSentFileStatus);
        }
        
        public void bind(Message message) {
            boolean isSent = message.getSenderId().equals(currentUserId);
            String fileName = message.getFileName();
            String fileUrl = message.getFileUrl();
            
            if (isSent) {
                layoutSentFile.setVisibility(View.VISIBLE);
                layoutReceivedFile.setVisibility(View.GONE);
                
                txtSentFileName.setText(fileName != null ? fileName : "File");
                txtSentFileSize.setText(""); // File size can be added if available
                txtSentFileTime.setText(formatTime(message.getTimestamp()));
                
                if (message.isSeen()) {
                    imgSentFileStatus.setAlpha(1.0f);
                } else {
                    imgSentFileStatus.setAlpha(0.5f);
                }
            } else {
                layoutSentFile.setVisibility(View.GONE);
                layoutReceivedFile.setVisibility(View.VISIBLE);
                
                txtReceivedFileName.setText(fileName != null ? fileName : "File");
                txtReceivedFileSize.setText(""); // File size can be added if available
                txtReceivedFileTime.setText(formatTime(message.getTimestamp()));
                
                itemView.setOnClickListener(v -> {
                    if (fileClickListener != null && fileUrl != null) {
                        fileClickListener.onFileClick(fileUrl, fileName);
                    }
                });
            }
        }
    }
    
    private String formatTime(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        return sdf.format(date);
    }
}

