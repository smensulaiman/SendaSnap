package com.sendajapan.sendasnap.domain.usecase;

import android.content.Context;
import com.sendajapan.sendasnap.data.repository.TaskRepositoryImpl;
import com.sendajapan.sendasnap.domain.repository.TaskRepository;
import com.sendajapan.sendasnap.models.Task;
import java.io.File;
import java.util.List;

public class UpdateTaskUseCase {
    private final TaskRepository taskRepository;

    public UpdateTaskUseCase(Context context) {
        this.taskRepository = new TaskRepositoryImpl(context);
    }

    public void execute(Integer id, UpdateTaskParams params, List<File> files, Boolean attachmentsUpdate, 
                       UseCaseCallback<Task> callback) {
        TaskRepository.UpdateTaskParams repoParams = new TaskRepository.UpdateTaskParams();
        repoParams.title = params.title;
        repoParams.description = params.description;
        repoParams.workDate = params.workDate;
        repoParams.workTime = params.workTime;
        repoParams.priority = params.priority;
        repoParams.dueDate = params.dueDate;
        repoParams.assignedTo = params.assignedTo;

        taskRepository.update(id, repoParams, files, attachmentsUpdate, 
                new TaskRepository.TaskRepositoryCallback<Task>() {
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

    public static class UpdateTaskParams {
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

