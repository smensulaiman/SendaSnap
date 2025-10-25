package com.sendajapan.sendasnap.models;

import java.io.Serializable;

public class Task implements Serializable {
    private String id;
    private String title;
    private String description;
    private String workDate; // ISO 8601 format
    private String workTime;
    private TaskStatus status;
    private String assignee; // Task assignee
    private long createdAt;
    private long updatedAt;
    private boolean isNew; // For NEW badge

    public Task() {
    }

    public Task(String id, String title, String description, String workDate, String workTime, TaskStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.workDate = workDate;
        this.workTime = workTime;
        this.status = status;
        this.assignee = ""; // Default empty assignee
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

    public enum TaskStatus {
        RUNNING, PENDING, COMPLETED, CANCELLED
    }
}
