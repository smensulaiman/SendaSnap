package com.sendajapan.sendasnap.domain.usecase;

import android.content.Context;
import com.sendajapan.sendasnap.data.repository.TaskRepositoryImpl;
import com.sendajapan.sendasnap.domain.repository.TaskRepository;

public class DeleteTaskUseCase {
    private final TaskRepository taskRepository;

    public DeleteTaskUseCase(Context context) {
        this.taskRepository = new TaskRepositoryImpl(context);
    }

    public void execute(Integer id, UseCaseCallback<Void> callback) {
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

    public interface UseCaseCallback<T> {
        void onSuccess(T result);
        void onError(String message, int errorCode);
    }
}

