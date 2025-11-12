package com.sendajapan.sendasnap.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TasksResponse {

    public static class TasksData {
        @SerializedName("tasks")
        private List<Task> tasks;

        public TasksData() {
        }

        public List<Task> getTasks() {
            return tasks;
        }

        public void setTasks(List<Task> tasks) {
            this.tasks = tasks;
        }

    }
}

