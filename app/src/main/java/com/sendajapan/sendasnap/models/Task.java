package com.sendajapan.sendasnap.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Task implements Serializable {

    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("work_date")
    private String workDate;

    @SerializedName("work_time")
    private String workTime;

    @SerializedName("status")
    private String statusString;

    private transient TaskStatus status;

    private UserData assignee;

    @SerializedName("assigned_users")
    private List<UserData> assignees;

    @SerializedName("created_by")
    private int createdByUserId;

    @SerializedName("priority")
    private String priorityString;

    private transient TaskPriority priority;

    @SerializedName("attachments")
    private List<TaskAttachment> attachments;

    @SerializedName("due_date")
    private String dueDate;

    @SerializedName("completed_at")
    private String completedAt;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("creator")
    private UserData creator;

    private boolean isNew;

    public Task() {
    }

    public Task(int id, String title, String description, String workDate, String workTime, TaskStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.workDate = workDate;
        this.workTime = workTime;
        this.status = status;
        this.statusString = status != null ? status.name().toLowerCase() : null;
        this.assignee = null;
        this.assignees = new ArrayList<>();
        this.priority = TaskPriority.NORMAL;
        this.priorityString = "medium";
        this.attachments = new ArrayList<>();
        this.createdAt = String.valueOf(System.currentTimeMillis());
        this.updatedAt = String.valueOf(System.currentTimeMillis());
        this.isNew = true;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
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
        // Convert statusString to enum if status is null but statusString exists
        if (status == null && statusString != null) {
            status = parseStatus(statusString);
        }
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
        this.statusString = status != null ? status.name().toLowerCase() : null;
        this.updatedAt = String.valueOf(System.currentTimeMillis());
    }

    // Helper method to parse status string to enum
    private TaskStatus parseStatus(String statusStr) {
        if (statusStr == null) {
            return TaskStatus.PENDING;
        }
        switch (statusStr.toLowerCase()) {
            case "running":
                return TaskStatus.RUNNING;
            case "pending":
                return TaskStatus.PENDING;
            case "completed":
                return TaskStatus.COMPLETED;
            case "cancelled":
                return TaskStatus.CANCELLED;
            default:
                return TaskStatus.PENDING;
        }
    }

    public UserData getAssignee() {
        return assignee;
    }

    public void setAssignee(UserData assignee) {
        this.assignee = assignee;
        this.updatedAt = String.valueOf(System.currentTimeMillis());
    }

    public List<UserData> getAssignees() {
        if (assignees == null) {
            assignees = new ArrayList<>();
            // Migrate from old assignee field if exists
            if (assignee != null) {
                assignees.add(assignee);
            }
        }
        return assignees;
    }

    public void setAssignees(List<UserData> assignees) {
        this.assignees = assignees;
        // Also update legacy assignee field for backward compatibility
        if (assignees != null && !assignees.isEmpty()) {
            this.assignee = assignees.get(0); // Set first assignee as legacy field
        } else {
            this.assignee = null;
        }
        this.updatedAt = String.valueOf(System.currentTimeMillis());
    }

    public void addAssignee(UserData assignee) {
        if (assignees == null) {
            assignees = new ArrayList<>();
        }
        // Check if user already exists by ID
        boolean exists = false;
        for (UserData existing : assignees) {
            if (existing != null && assignee != null && 
                existing.getId() == assignee.getId()) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            assignees.add(assignee);
            if (this.assignee == null) {
                this.assignee = assignee; // Set as legacy field if empty
            }
            this.updatedAt = String.valueOf(System.currentTimeMillis());
        }
    }

    public void removeAssignee(UserData assignee) {
        if (assignees != null && assignee != null) {
            assignees.removeIf(user -> user != null && user.getId() == assignee.getId());
            if (assignees.isEmpty()) {
                this.assignee = null;
            } else {
                this.assignee = assignees.get(0); // Set first as legacy field
            }
            this.updatedAt = String.valueOf(System.currentTimeMillis());
        }
    }

    public int getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(int createdByUserId) {
        this.createdByUserId = createdByUserId;
        this.updatedAt = String.valueOf(System.currentTimeMillis());
    }

    public TaskPriority getPriority() {
        // Convert priorityString to enum if priority is null but priorityString exists
        if (priority == null && priorityString != null) {
            priority = parsePriority(priorityString);
        }
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
        // Convert enum to API string format
        if (priority != null) {
            switch (priority) {
                case LOW:
                    this.priorityString = "low";
                    break;
                case NORMAL:
                    this.priorityString = "medium";
                    break;
                case HIGH:
                    this.priorityString = "high";
                    break;
            }
        } else {
            this.priorityString = null;
        }
        this.updatedAt = String.valueOf(System.currentTimeMillis());
    }

    // Helper method to parse priority string to enum
    private TaskPriority parsePriority(String priorityStr) {
        if (priorityStr == null) {
            return TaskPriority.NORMAL;
        }
        switch (priorityStr.toLowerCase()) {
            case "low":
                return TaskPriority.LOW;
            case "medium":
                return TaskPriority.NORMAL;
            case "high":
            case "urgent":
                return TaskPriority.HIGH;
            default:
                return TaskPriority.NORMAL;
        }
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public UserData getCreator() {
        return creator;
    }

    public void setCreator(UserData creator) {
        this.creator = creator;
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
        this.updatedAt = String.valueOf(System.currentTimeMillis());
    }

    public void addAttachment(TaskAttachment attachment) {
        if (this.attachments == null) {
            this.attachments = new ArrayList<>();
        }
        this.attachments.add(attachment);
        this.updatedAt = String.valueOf(System.currentTimeMillis());
    }

    public void removeAttachment(TaskAttachment attachment) {
        if (this.attachments != null) {
            this.attachments.remove(attachment);
            this.updatedAt = String.valueOf(System.currentTimeMillis());
        }
    }

    public enum TaskStatus {
        RUNNING, PENDING, COMPLETED, CANCELLED
    }

    public enum TaskPriority {
        LOW, NORMAL, HIGH
    }

    @Override
    public String toString() {
        return "Task{\n" +
                "  id=" + id + ",\n" +
                "  title='" + title + "',\n" +
                "  description='" + description + "',\n" +
                "  workDate='" + workDate + "',\n" +
                "  workTime='" + workTime + "',\n" +
                "  statusString='" + statusString + "',\n" +
                "  status=" + status + ",\n" +
                "  assignee=" + assignee + ",\n" +
                "  assignees=" + assignees + ",\n" +
                "  createdByUserId=" + createdByUserId + ",\n" +
                "  priorityString='" + priorityString + "',\n" +
                "  priority=" + priority + ",\n" +
                "  attachments=" + attachments + ",\n" +
                "  dueDate='" + dueDate + "',\n" +
                "  completedAt='" + completedAt + "',\n" +
                "  createdAt='" + createdAt + "',\n" +
                "  updatedAt='" + updatedAt + "',\n" +
                "  creator=" + creator + ",\n" +
                "  isNew=" + isNew + "\n" +
                "}";
    }
}
