package com.sendajapan.sendasnap.domain.usecase;

import android.content.Context;
import com.sendajapan.sendasnap.data.dto.PagedResult;
import com.sendajapan.sendasnap.data.repository.TaskRepositoryImpl;
import com.sendajapan.sendasnap.domain.repository.TaskRepository;
import com.sendajapan.sendasnap.models.Task;

public class ListTasksUseCase {
    private final TaskRepository taskRepository;

    public ListTasksUseCase(Context context) {
        this.taskRepository = new TaskRepositoryImpl(context);
    }

    public void execute(String fromDate, String toDate, Integer page, UseCaseCallback<PagedResult<Task>> callback) {
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

    public interface UseCaseCallback<T> {
        void onSuccess(T result);
        void onError(String message, int errorCode);
    }
}

