package com.sendajapan.sendasnap.data.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TasksListResponseDto {
    @SerializedName("tasks")
    private List<TaskDto> tasks;

    @SerializedName("pagination")
    private PaginationDto pagination;

    public TasksListResponseDto() {
    }

    public List<TaskDto> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskDto> tasks) {
        this.tasks = tasks;
    }

    public PaginationDto getPagination() {
        return pagination;
    }

    public void setPagination(PaginationDto pagination) {
        this.pagination = pagination;
    }
}

