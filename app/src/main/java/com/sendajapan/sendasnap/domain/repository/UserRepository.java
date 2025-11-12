package com.sendajapan.sendasnap.domain.repository;

import com.sendajapan.sendasnap.models.UserData;
import java.util.List;

public interface UserRepository {
    void list(UserRepositoryCallback<List<UserData>> callback);

    interface UserRepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String message, int errorCode);
    }
}

