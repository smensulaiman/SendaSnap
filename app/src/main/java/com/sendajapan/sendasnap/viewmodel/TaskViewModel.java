package com.sendajapan.sendasnap.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.sendajapan.sendasnap.data.dto.PagedResult;
import com.sendajapan.sendasnap.data.repository.TaskRepositoryImpl;
import com.sendajapan.sendasnap.domain.repository.TaskRepository;
import com.sendajapan.sendasnap.models.Task;

import java.io.File;
import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository taskRepository;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        this.taskRepository = new TaskRepositoryImpl(application);
    }

    public void listTasks(String fromDate, String toDate, TaskCallback<PagedResult<Task>> callback) {
        taskRepository.list(fromDate, toDate, new TaskRepository.TaskRepositoryCallback<PagedResult<Task>>() {
            @Override
            public void onSuccess(PagedResult<Task> result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String message, int errorCode) {
                callback.onError(message, errorCode);
            }
        });
    }

    public void getTask(Integer id, TaskCallback<Task> callback) {
        taskRepository.get(id, new TaskRepository.TaskRepositoryCallback<Task>() {
            @Override
            public void onSuccess(Task result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String message, int errorCode) {
                callback.onError(message, errorCode);
            }
        });
    }

    public void createTask(CreateTaskParams params, List<File> files, TaskCallback<Task> callback) {
        TaskRepository.CreateTaskParams repoParams = new TaskRepository.CreateTaskParams();
        repoParams.title = params.title;
        repoParams.description = params.description;
        repoParams.workDate = params.workDate;
        repoParams.workTime = params.workTime;
        repoParams.priority = params.priority;
        repoParams.dueDate = params.dueDate;
        repoParams.assignedTo = params.assignedTo;

        taskRepository.create(repoParams, files, new TaskRepository.TaskRepositoryCallback<Task>() {
            @Override
            public void onSuccess(Task result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String message, int errorCode) {
                callback.onError(message, errorCode);
            }
        });
    }

    public void updateTask(Integer id, UpdateTaskParams params, List<File> files, Boolean attachmentsUpdate, TaskCallback<Task> callback) {
        TaskRepository.UpdateTaskParams repoParams = new TaskRepository.UpdateTaskParams();
        repoParams.title = params.title;
        repoParams.description = params.description;
        repoParams.workDate = params.workDate;
        repoParams.workTime = params.workTime;
        repoParams.priority = params.priority;
        repoParams.dueDate = params.dueDate;
        repoParams.assignedTo = params.assignedTo;

        taskRepository.update(id, repoParams, files, attachmentsUpdate, new TaskRepository.TaskRepositoryCallback<Task>() {
            @Override
            public void onSuccess(Task result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String message, int errorCode) {
                callback.onError(message, errorCode);
            }
        });
    }

    public void deleteTask(Integer id, TaskCallback<Void> callback) {
        taskRepository.delete(id, new TaskRepository.TaskRepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String message, int errorCode) {
                callback.onError(message, errorCode);
            }
        });
    }

    public void updateTaskStatus(Integer id, Task.TaskStatus status, TaskCallback<Task> callback) {
        taskRepository.updateStatus(id, status, new TaskRepository.TaskRepositoryCallback<Task>() {
            @Override
            public void onSuccess(Task result) {
                callback.onSuccess(result);
            }

            @Override
            public void onError(String message, int errorCode) {
                callback.onError(message, errorCode);
            }
        });
    }

    public interface TaskCallback<T> {
        void onSuccess(T result);
        void onError(String message, int errorCode);
    }

    public static class CreateTaskParams {
        public String title;
        public String description;
        public String workDate;
        public String workTime;
        public String priority;
        public String dueDate;
        public List<Integer> assignedTo;
    }

    public static class UpdateTaskParams {
        public String title;
        public String description;
        public String workDate;
        public String workTime;
        public String priority;
        public String dueDate;
        public List<Integer> assignedTo;
    }
}

