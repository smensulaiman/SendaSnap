package com.sendajapan.sendasnap.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.sendajapan.sendasnap.data.repository.UserRepositoryImpl;
import com.sendajapan.sendasnap.domain.repository.UserRepository;
import com.sendajapan.sendasnap.models.UserData;

import java.util.List;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository userRepository;

    public UserViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepositoryImpl(application);
    }

    public void listUsers(UserCallback<List<UserData>> callback) {
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

    public interface UserCallback<T> {
        void onSuccess(T result);
        void onError(String message, int errorCode);
    }
}

