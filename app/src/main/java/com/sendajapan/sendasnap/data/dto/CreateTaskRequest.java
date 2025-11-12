package com.sendajapan.sendasnap.data.dto;

import java.util.List;

public class CreateTaskRequest {
    private String title;
    private String description;
    private String workDate;
    private String workTime;
    private String priority;
    private String dueDate;
    private List<Integer> assignedTo;

    public CreateTaskRequest() {
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

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public List<Integer> getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(List<Integer> assignedTo) {
        this.assignedTo = assignedTo;
    }
}

