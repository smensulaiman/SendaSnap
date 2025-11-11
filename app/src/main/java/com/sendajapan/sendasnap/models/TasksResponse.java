package com.sendajapan.sendasnap.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TasksResponse {

    public static class TasksData {
        @SerializedName("tasks")
        private List<Task> tasks;

        @SerializedName("pagination")
        private Pagination pagination;

        public TasksData() {
        }

        public List<Task> getTasks() {
            return tasks;
        }

        public void setTasks(List<Task> tasks) {
            this.tasks = tasks;
        }

        public Pagination getPagination() {
            return pagination;
        }

        public void setPagination(Pagination pagination) {
            this.pagination = pagination;
        }
    }

    public static class Pagination {
        @SerializedName("current_page")
        private int currentPage;

        @SerializedName("last_page")
        private int lastPage;

        @SerializedName("per_page")
        private int perPage;

        @SerializedName("total")
        private int total;

        public Pagination() {
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public void setCurrentPage(int currentPage) {
            this.currentPage = currentPage;
        }

        public int getLastPage() {
            return lastPage;
        }

        public void setLastPage(int lastPage) {
            this.lastPage = lastPage;
        }

        public int getPerPage() {
            return perPage;
        }

        public void setPerPage(int perPage) {
            this.perPage = perPage;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }
    }
}

