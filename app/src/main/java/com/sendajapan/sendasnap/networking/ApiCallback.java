package com.sendajapan.sendasnap.networking;

public interface ApiCallback<T> {
    void onSuccess(T data);
    void onError(String message, int errorCode);
}

