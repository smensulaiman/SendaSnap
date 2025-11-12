package com.sendajapan.sendasnap.domain.usecase;

import android.content.Context;
import com.sendajapan.sendasnap.data.repository.TaskRepositoryImpl;
import com.sendajapan.sendasnap.domain.repository.TaskRepository;
import com.sendajapan.sendasnap.models.Task;

public class GetTaskUseCase {
    private final TaskRepository taskRepository;

    public GetTaskUseCase(Context context) {
        this.taskRepository = new TaskRepositoryImpl(context);
    }

    public void execute(Integer id, UseCaseCallback<Task> callback) {
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

    public interface UseCaseCallback<T> {
        void onSuccess(T result);
        void onError(String message, int errorCode);
    }
}

