package com.sendajapan.sendasnap.domain.repository;

import com.sendajapan.sendasnap.data.dto.PagedResult;
import com.sendajapan.sendasnap.models.Task;
import java.io.File;
import java.util.List;

public interface TaskRepository {

    void list(String fromDate, String toDate, TaskRepositoryCallback<PagedResult<Task>> callback);

    void get(Integer id, TaskRepositoryCallback<Task> callback);

    void create(CreateTaskParams params, List<File> files, TaskRepositoryCallback<Task> callback);

    void update(Integer id, UpdateTaskParams params, List<File> files, Boolean attachmentsUpdate,
            TaskRepositoryCallback<Task> callback);

    void delete(Integer id, TaskRepositoryCallback<Void> callback);

    void updateStatus(Integer id, Task.TaskStatus status, TaskRepositoryCallback<Task> callback);

    interface TaskRepositoryCallback<T> {
        void onSuccess(T result);

        void onError(String message, int errorCode);
    }

    class CreateTaskParams {
        public String title;
        public String description;
        public String workDate;
        public String workTime;
        public String priority;
        public String dueDate;
        public List<Integer> assignedTo;
    }

    class UpdateTaskParams {
        public String title;
        public String description;
        public String workDate;
        public String workTime;
        public String priority;
        public String dueDate;
        public List<Integer> assignedTo;
    }
}
