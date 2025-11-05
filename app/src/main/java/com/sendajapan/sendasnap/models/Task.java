package com.sendajapan.sendasnap.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Task implements Serializable {

    private String id;
    private String title;
    private String description;
    private String workDate;
    private String workTime;
    private TaskStatus status;
    private String assignee;
    private TaskPriority priority;
    private List<TaskAttachment> attachments;
    private long createdAt;
    private long updatedAt;
    private boolean isNew;

    public Task() {
    }

    public Task(String id, String title, String description, String workDate, String workTime, TaskStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.workDate = workDate;
        this.workTime = workTime;
        this.status = status;
        this.assignee = "";
        this.priority = TaskPriority.NORMAL;
        this.attachments = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isNew = true;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWorkDate() {
        return workDate;
    }

    public void setWorkDate(String workDate) {
        this.workDate = workDate;
    }

    public String getWorkTime() {
        return workTime;
    }

    public void setWorkTime(String workTime) {
        this.workTime = workTime;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
        this.updatedAt = System.currentTimeMillis();
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public List<TaskAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<TaskAttachment> attachments) {
        this.attachments = attachments;
        this.updatedAt = System.currentTimeMillis();
    }

    public void addAttachment(TaskAttachment attachment) {
        if (this.attachments == null) {
            this.attachments = new ArrayList<>();
        }
        this.attachments.add(attachment);
        this.updatedAt = System.currentTimeMillis();
    }

    public void removeAttachment(TaskAttachment attachment) {
        if (this.attachments != null) {
            this.attachments.remove(attachment);
            this.updatedAt = System.currentTimeMillis();
        }
    }

    public enum TaskStatus {
        RUNNING, PENDING, COMPLETED, CANCELLED
    }

    public enum TaskPriority {
        LOW, NORMAL, HIGH
    }
}
