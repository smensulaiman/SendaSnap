package com.sendajapan.sendasnap.domain.usecase;

import android.content.Context;
import com.sendajapan.sendasnap.data.repository.UserRepositoryImpl;
import com.sendajapan.sendasnap.domain.repository.UserRepository;
import com.sendajapan.sendasnap.models.UserData;
import java.util.List;

public class ListUsersUseCase {
    private final UserRepository userRepository;

    public ListUsersUseCase(Context context) {
        this.userRepository = new UserRepositoryImpl(context);
    }

    public void execute(UseCaseCallback<List<UserData>> callback) {
        userRepository.list(new UserRepository.UserRepositoryCallback<List<UserData>>() {
            @Override
            public void onSuccess(List<UserData> result) {
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

