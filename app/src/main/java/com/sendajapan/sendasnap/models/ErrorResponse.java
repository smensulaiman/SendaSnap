package com.sendajapan.sendasnap.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;
import java.util.List;

public class ErrorResponse {
    @SerializedName("success")
    private Boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("errors")
    private Map<String, Object> errors;

    public ErrorResponse() {
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

    public Map<String, Object> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, Object> errors) {
        this.errors = errors;
    }

    /**
     * Get formatted error message from errors map
     * Handles both Map<String, String> and Map<String, List<String>> formats
     */
    public String getFormattedErrorMessage() {
        if (errors == null || errors.isEmpty()) {
            return message != null ? message : "An error occurred";
        }

        StringBuilder errorMsg = new StringBuilder();
        if (message != null) {
            errorMsg.append(message);
        }
        
        for (Map.Entry<String, Object> entry : errors.entrySet()) {
            String field = entry.getKey();
            Object value = entry.getValue();
            
            if (value != null) {
                if (errorMsg.length() > 0 && !errorMsg.toString().endsWith("\n")) {
                    errorMsg.append("\n");
                }
                
                if (value instanceof List) {
                    // Handle List<String> case (validation errors)
                    @SuppressWarnings("unchecked")
                    List<String> fieldErrors = (List<String>) value;
                    if (!fieldErrors.isEmpty()) {
                        errorMsg.append(field).append(": ").append(String.join(", ", fieldErrors));
                    }
                } else if (value instanceof String) {
                    // Handle String case (simple error messages)
                    errorMsg.append(field).append(": ").append(value);
                } else {
                    // Fallback for other types
                    errorMsg.append(field).append(": ").append(value.toString());
                }
            }
        }
        
        return errorMsg.toString().trim();
    }
}

