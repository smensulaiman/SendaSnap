package com.sendajapan.sendasnap.domain.usecase;

import android.content.Context;
import com.sendajapan.sendasnap.data.repository.TaskRepositoryImpl;
import com.sendajapan.sendasnap.domain.repository.TaskRepository;
import com.sendajapan.sendasnap.models.Task;
import java.io.File;
import java.util.List;

public class CreateTaskUseCase {
    private final TaskRepository taskRepository;

    public CreateTaskUseCase(Context context) {
        this.taskRepository = new TaskRepositoryImpl(context);
    }

    public void execute(CreateTaskParams params, List<File> files, UseCaseCallback<Task> callback) {
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

    public static class CreateTaskParams {
        public String title;
        public String description;
        public String workDate;
        public String workTime;
        public String priority;
        public String dueDate;
        public List<Integer> assignedTo;
    }

    public interface UseCaseCallback<T> {
        void onSuccess(T result);
        void onError(String message, int errorCode);
    }
}

