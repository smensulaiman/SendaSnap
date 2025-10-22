package com.sendajapan.sendasnap.models;

import java.util.List;

public class TaskResponse {
    private List<Task> tasks;
    private int totalCount;
    private boolean hasMore;
    private String message;

    public TaskResponse() {
    }

    public TaskResponse(List<Task> tasks, int totalCount, boolean hasMore, String message) {
        this.tasks = tasks;
        this.totalCount = totalCount;
        this.hasMore = hasMore;
        this.message = message;
    }

    // Getters and Setters
    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
