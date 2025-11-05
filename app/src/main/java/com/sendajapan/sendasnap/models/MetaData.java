package com.sendajapan.sendasnap.models;

import com.google.gson.annotations.SerializedName;

public class MetaData {
    @SerializedName("timestamp")
    private String timestamp;

    public MetaData() {
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

