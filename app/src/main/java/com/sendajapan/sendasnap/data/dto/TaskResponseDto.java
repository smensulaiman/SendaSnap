package com.sendajapan.sendasnap.data.dto;

import com.google.gson.annotations.SerializedName;

public class TaskResponseDto {
    @SerializedName("task")
    private TaskDto task;

    public TaskResponseDto() {
    }

    public TaskDto getTask() {
        return task;
    }

    public void setTask(TaskDto task) {
        this.task = task;
    }
}

