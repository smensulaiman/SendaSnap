package com.sendajapan.sendasnap.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VehicleImageUploadResponse {
    @SerializedName("success")
    private Boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private VehicleImageUploadData data;

    public VehicleImageUploadResponse() {
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

    public VehicleImageUploadData getData() {
        return data;
    }

    public void setData(VehicleImageUploadData data) {
        this.data = data;
    }

    public static class VehicleImageUploadData {
        @SerializedName("vehicle")
        private Vehicle vehicle;

        public VehicleImageUploadData() {
        }

        public Vehicle getVehicle() {
            return vehicle;
        }

        public void setVehicle(Vehicle vehicle) {
            this.vehicle = vehicle;
        }
    }
}

