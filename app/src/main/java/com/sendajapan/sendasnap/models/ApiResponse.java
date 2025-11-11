package com.sendajapan.sendasnap.models;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("success")
    private Boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("meta")
    private MetaData meta;

    public ApiResponse() {
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public MetaData getMeta() {
        return meta;
    }

    public void setMeta(MetaData meta) {
        this.meta = meta;
    }
}

