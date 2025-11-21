package com.sendajapan.sendasnap.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.sendajapan.sendasnap.data.dto.PagedResult;
import com.sendajapan.sendasnap.data.dto.TaskResponseDto;
import com.sendajapan.sendasnap.data.dto.TasksListResponseDto;
import com.sendajapan.sendasnap.data.mapper.TaskMapper;
import com.sendajapan.sendasnap.domain.repository.TaskRepository;
import com.sendajapan.sendasnap.models.Task;
import com.sendajapan.sendasnap.models.ApiResponse;
import com.sendajapan.sendasnap.models.ErrorResponse;
import com.sendajapan.sendasnap.networking.ApiService;
import com.sendajapan.sendasnap.networking.RetrofitClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskRepositoryImpl implements TaskRepository {

    private static final String TAG = "TaskRepositoryImpl";
    
    private final ApiService apiService;
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    public TaskRepositoryImpl(Context context) {
        this.apiService = RetrofitClient.getInstance(context).getApiService();
    }

    @Override
    public void list(String fromDate, String toDate, TaskRepositoryCallback<PagedResult<Task>> callback) {

        Call<ApiResponse<TasksListResponseDto>> call = apiService.getTasksList(fromDate, toDate);
        call.enqueue(new Callback<ApiResponse<TasksListResponseDto>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<TasksListResponseDto>> call,
                                   @NonNull Response<ApiResponse<TasksListResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<TasksListResponseDto> apiResponse = response.body();

                    if (apiResponse.getSuccess() != null && apiResponse.getSuccess()) {
                        if (apiResponse.getData() != null) {
                            List<Task> tasks = TaskMapper.toDomainList(apiResponse.getData().getTasks());
                            PagedResult<Task> result = new PagedResult<>(
                                    tasks,
                                    apiResponse.getData().getPagination()
                            );
                            callback.onSuccess(result);
                        } else {
                            callback.onError("No tasks data received", response.code());
                        }
                    } else {
                        String errorMessage = apiResponse.getMessage() != null
                                ? apiResponse.getMessage()
                                : "Failed to retrieve tasks";
                        callback.onError(errorMessage, response.code());
                    }
                } else {
                    String errorMessage = parseErrorMessage(response);
                    callback.onError(errorMessage, response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<TasksListResponseDto>> call, Throwable t) {
                String errorMessage = "Network error. Please check your connection and try again.";
                if (t.getMessage() != null) {
                    errorMessage = t.getMessage();
                }
                callback.onError(errorMessage, 0);
            }
        });
    }

    @Override
    public void get(Integer id, TaskRepositoryCallback<Task> callback) {
        Call<ApiResponse<TaskResponseDto>> call = apiService.getTask(id);
        call.enqueue(new Callback<ApiResponse<TaskResponseDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<TaskResponseDto>> call,
                                   Response<ApiResponse<TaskResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<TaskResponseDto> apiResponse = response.body();

                    if (apiResponse.getSuccess() != null && apiResponse.getSuccess()) {
                        if (apiResponse.getData() != null && apiResponse.getData().getTask() != null) {
                            Task task = TaskMapper.toDomain(apiResponse.getData().getTask());
                            callback.onSuccess(task);
                        } else {
                            callback.onError("Task not found", 404);
                        }
                    } else {
                        String errorMessage = apiResponse.getMessage() != null
                                ? apiResponse.getMessage()
                                : "Failed to retrieve task";
                        callback.onError(errorMessage, response.code());
                    }
                } else {
                    String errorMessage = parseErrorMessage(response);
                    if (response.code() == 404) {
                        errorMessage = "Task not found";
                    }
                    callback.onError(errorMessage, response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<TaskResponseDto>> call, Throwable t) {
                String errorMessage = "Network error. Please check your connection and try again.";
                if (t.getMessage() != null) {
                    errorMessage = t.getMessage();
                }
                callback.onError(errorMessage, 0);
            }
        });
    }

    @Override
    public void create(CreateTaskParams params, List<File> files, TaskRepositoryCallback<Task> callback) {
        // Validate file sizes
        if (files != null) {
            for (File file : files) {
                if (file.length() > MAX_FILE_SIZE) {
                    callback.onError("File size exceeds 10MB limit: " + file.getName(), 422);
                    return;
                }
            }
        }

        List<MultipartBody.Part> parts = buildMultipartParts(params, files, null);

        Call<ApiResponse<TaskResponseDto>> call = apiService.createTask(parts);
        call.enqueue(new Callback<ApiResponse<TaskResponseDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<TaskResponseDto>> call,
                                   Response<ApiResponse<TaskResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<TaskResponseDto> apiResponse = response.body();

                    if (apiResponse.getSuccess() != null && apiResponse.getSuccess()) {
                        if (apiResponse.getData() != null && apiResponse.getData().getTask() != null) {
                            Task task = TaskMapper.toDomain(apiResponse.getData().getTask());
                            callback.onSuccess(task);
                        } else {
                            callback.onError("Failed to create task", response.code());
                        }
                    } else {
                        String errorMessage = parseErrorMessage(apiResponse, response);
                        callback.onError(errorMessage, response.code());
                    }
                } else {
                    String errorMessage = parseErrorMessage(response);
                    callback.onError(errorMessage, response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<TaskResponseDto>> call, Throwable t) {
                String errorMessage = "Network error. Please check your connection and try again.";
                if (t.getMessage() != null) {
                    errorMessage = t.getMessage();
                }
                callback.onError(errorMessage, 0);
            }
        });
    }

    @Override
    public void update(Integer id, UpdateTaskParams params, List<File> files, Boolean attachmentsUpdate,
                       TaskRepositoryCallback<Task> callback) {
        if (files != null) {
            for (File file : files) {
                if (file.length() > MAX_FILE_SIZE) {
                    callback.onError("File size exceeds 10MB limit: " + file.getName(), 422);
                    return;
                }
            }
        }

        Log.d(TAG, "update: id=" + id + ", params=" + params);
        Log.d(TAG, "update: files count=" + (files != null ? files.size() : 0) + ", attachmentsUpdate=" + attachmentsUpdate);
        
        if (params != null && params instanceof UpdateTaskParams) {
            UpdateTaskParams updateParams = (UpdateTaskParams) params;
            Log.d(TAG, "update: UpdateTaskParams details - title=" + updateParams.title + 
                    ", description=" + (updateParams.description != null ? updateParams.description.substring(0, Math.min(50, updateParams.description.length())) : "null") + 
                    ", workDate=" + updateParams.workDate + 
                    ", workTime=" + updateParams.workTime + 
                    ", priority=" + updateParams.priority + 
                    ", assignedTo=" + (updateParams.assignedTo != null ? updateParams.assignedTo.size() + " users" : "null"));
        }
        
        List<MultipartBody.Part> parts = buildMultipartParts(params, files, attachmentsUpdate);
        Log.d(TAG, "update: built " + parts.size() + " multipart parts total");
        
        // Count non-file parts (fields)
        int fieldPartsCount = 0;
        int filePartsCount = 0;
        // Log what parts are being sent
        for (int i = 0; i < parts.size(); i++) {
            MultipartBody.Part part = parts.get(i);
            if (part != null && part.headers() != null) {
                String contentDisposition = part.headers().get("Content-Disposition");
                if (contentDisposition != null) {
                    // Extract field name from Content-Disposition header
                    if (contentDisposition.contains("name=\"")) {
                        int start = contentDisposition.indexOf("name=\"") + 6;
                        int end = contentDisposition.indexOf("\"", start);
                        if (end > start) {
                            String fieldName = contentDisposition.substring(start, end);
                            Log.d(TAG, "update: part " + i + " = " + fieldName);
                            if (fieldName.startsWith("attachments[")) {
                                filePartsCount++;
                            } else {
                                fieldPartsCount++;
                            }
                        }
                    }
                }
            }
        }
        Log.d(TAG, "update: sending " + fieldPartsCount + " field parts and " + filePartsCount + " file parts");
        
        // Warn if no fields are being sent (only files or attachments_update)
        if (fieldPartsCount == 0 && files == null) {
            Log.w(TAG, "update: WARNING - No field parts being sent! Only attachments_update or no data at all.");
        }

        Call<ApiResponse<TaskResponseDto>> call = apiService.updateTask(id, parts);
        call.enqueue(new Callback<ApiResponse<TaskResponseDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<TaskResponseDto>> call,
                                   Response<ApiResponse<TaskResponseDto>> response) {
                Log.d(TAG, "update response: code=" + response.code() + ", isSuccessful=" + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<TaskResponseDto> apiResponse = response.body();

                    if (apiResponse.getSuccess() != null && apiResponse.getSuccess()) {
                        if (apiResponse.getData() != null && apiResponse.getData().getTask() != null) {
                            Task task = TaskMapper.toDomain(apiResponse.getData().getTask());
                            Log.d(TAG, "update: success, task id=" + task.getId() + 
                                    ", title=" + task.getTitle() + 
                                    ", workDate=" + task.getWorkDate() + 
                                    ", priority=" + task.getPriority());
                            callback.onSuccess(task);
                        } else {
                            Log.e(TAG, "update: failed - no task data in response");
                            callback.onError("Failed to update task", response.code());
                        }
                    } else {
                        String errorMessage = parseErrorMessage(apiResponse, response);
                        Log.e(TAG, "update: API returned error - " + errorMessage);
                        callback.onError(errorMessage, response.code());
                    }
                } else {
                    String errorMessage = parseErrorMessage(response);
                    Log.e(TAG, "update: HTTP error - " + errorMessage + " (code: " + response.code() + ")");
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "update: error body - " + errorBody);
                        } catch (Exception e) {
                            Log.e(TAG, "update: failed to read error body", e);
                        }
                    }
                    callback.onError(errorMessage, response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<TaskResponseDto>> call, Throwable t) {
                String errorMessage = "Network error. Please check your connection and try again.";
                if (t.getMessage() != null) {
                    errorMessage = t.getMessage();
                }
                callback.onError(errorMessage, 0);
            }
        });
    }

    @Override
    public void delete(Integer id, TaskRepositoryCallback<Void> callback) {
        Call<ApiResponse<Object>> call = apiService.deleteTask(id);
        call.enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call,
                                   Response<ApiResponse<Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> apiResponse = response.body();

                    if (apiResponse.getSuccess() != null && apiResponse.getSuccess()) {
                        callback.onSuccess(null);
                    } else {
                        String errorMessage = apiResponse.getMessage() != null
                                ? apiResponse.getMessage()
                                : "Failed to delete task";
                        callback.onError(errorMessage, response.code());
                    }
                } else {
                    String errorMessage = parseErrorMessage(response);
                    if (response.code() == 404) {
                        errorMessage = "Task not found";
                    }
                    callback.onError(errorMessage, response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                String errorMessage = "Network error. Please check your connection and try again.";
                if (t.getMessage() != null) {
                    errorMessage = t.getMessage();
                }
                callback.onError(errorMessage, 0);
            }
        });
    }

    @Override
    public void updateStatus(Integer id, Task.TaskStatus status, TaskRepositoryCallback<Task> callback) {
        // Convert enum to string for API
        String statusStr = status != null ? status.name().toLowerCase() : "pending";
        com.sendajapan.sendasnap.data.dto.StatusUpdateRequest request =
                new com.sendajapan.sendasnap.data.dto.StatusUpdateRequest(statusStr);

        Call<ApiResponse<TaskResponseDto>> call = apiService.updateTaskStatus(id, request);
        call.enqueue(new Callback<ApiResponse<TaskResponseDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<TaskResponseDto>> call,
                                   Response<ApiResponse<TaskResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<TaskResponseDto> apiResponse = response.body();

                    if (apiResponse.getSuccess() != null && apiResponse.getSuccess()) {
                        if (apiResponse.getData() != null && apiResponse.getData().getTask() != null) {
                            Task task = TaskMapper.toDomain(apiResponse.getData().getTask());
                            callback.onSuccess(task);
                        } else {
                            callback.onError("Failed to update task status", response.code());
                        }
                    } else {
                        String errorMessage = apiResponse.getMessage() != null
                                ? apiResponse.getMessage()
                                : "Failed to update task status";
                        callback.onError(errorMessage, response.code());
                    }
                } else {
                    String errorMessage = parseErrorMessage(response);
                    callback.onError(errorMessage, response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<TaskResponseDto>> call, Throwable t) {
                String errorMessage = "Network error. Please check your connection and try again.";
                if (t.getMessage() != null) {
                    errorMessage = t.getMessage();
                }
                callback.onError(errorMessage, 0);
            }
        });
    }

    private List<MultipartBody.Part> buildMultipartParts(Object params, List<File> files, Boolean attachmentsUpdate) {
        List<MultipartBody.Part> parts = new ArrayList<>();

        if (params instanceof CreateTaskParams) {
            CreateTaskParams createParams = (CreateTaskParams) params;
            addPart(parts, "title", createParams.title);
            addPart(parts, "description", createParams.description);
            addPart(parts, "work_date", createParams.workDate);
            addPart(parts, "work_time", createParams.workTime);
            addPart(parts, "priority", createParams.priority);
            addPart(parts, "due_date", createParams.dueDate);

            if (createParams.assignedTo != null) {
                for (Integer userId : createParams.assignedTo) {
                    addPart(parts, "assigned_to[]", userId != null ? userId.toString() : null);
                }
            }
        } else if (params instanceof UpdateTaskParams) {
            UpdateTaskParams updateParams = (UpdateTaskParams) params;
            Log.d(TAG, "buildMultipartParts - UpdateTaskParams: title=" + updateParams.title + 
                    ", description=" + updateParams.description + 
                    ", workDate=" + updateParams.workDate + 
                    ", workTime=" + updateParams.workTime + 
                    ", priority=" + updateParams.priority + 
                    ", dueDate=" + updateParams.dueDate);
            
            // For updates, only send fields that have values (not null/empty)
            // Backend uses array_filter to remove null values, so only sent fields are updated
            // This allows partial updates - fields not sent will remain unchanged
            int partsBefore = parts.size();
            addPart(parts, "title", updateParams.title);
            addPart(parts, "description", updateParams.description);
            addPart(parts, "work_date", updateParams.workDate);
            addPart(parts, "work_time", updateParams.workTime);
            addPart(parts, "priority", updateParams.priority);
            addPart(parts, "due_date", updateParams.dueDate);
            int partsAfter = parts.size();
            Log.d(TAG, "buildMultipartParts - added " + (partsAfter - partsBefore) + " field parts");

            // Always send assigned_to if it's not null (even if empty array)
            // Backend uses $request->has('assigned_to') to check if it should update
            if (updateParams.assignedTo != null) {
                Log.d(TAG, "buildMultipartParts - assignedTo count: " + updateParams.assignedTo.size());
                if (updateParams.assignedTo.isEmpty()) {
                    // Send empty array - backend will clear all assignments
                    // Laravel expects at least one part with the array key to recognize it as an array
                    addPart(parts, "assigned_to[]", "");
                } else {
                    for (Integer userId : updateParams.assignedTo) {
                        if (userId != null) {
                            addPart(parts, "assigned_to[]", userId.toString());
                        }
                    }
                }
            } else {
                Log.d(TAG, "buildMultipartParts - assignedTo is null (will not update assignments)");
            }

            if (attachmentsUpdate != null) {
                // Send as "1" or "0" for better Laravel boolean() compatibility
                addPart(parts, "attachments_update", attachmentsUpdate ? "1" : "0");
                Log.d(TAG, "buildMultipartParts - attachments_update: " + attachmentsUpdate);
            }
            
            // Add _method=PUT for Laravel method spoofing (some backends need this for PUT with multipart)
            // This ensures Laravel recognizes it as a PUT request even with multipart/form-data
            addPart(parts, "_method", "PUT");
            Log.d(TAG, "buildMultipartParts - added _method=PUT for Laravel method spoofing");
        }

        // Add file attachments
        if (files != null) {
            for (File file : files) {
                if (file.exists() && file.isFile()) {
                    RequestBody requestFile = RequestBody.create(
                            MediaType.parse("application/octet-stream"), file);
                    MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                            "attachments[]", file.getName(), requestFile);
                    parts.add(filePart);
                }
            }
        }

        return parts;
    }

    private void addPart(List<MultipartBody.Part> parts, String key, String value) {
        // For updates, we only send non-null, non-empty values
        // Backend uses array_filter to remove null values, so only sent fields are updated
        if (value != null && !value.trim().isEmpty()) {
            RequestBody body = RequestBody.create(MediaType.parse("text/plain"), value);
            parts.add(MultipartBody.Part.createFormData(key, null, body));
            Log.d(TAG, "addPart: added " + key + " = " + value);
        } else {
            Log.d(TAG, "addPart: skipping " + key + " (null or empty - will not update this field)");
        }
    }

    private String parseErrorMessage(Response<?> response) {
        String defaultMessage = "An error occurred. Please try again.";

        if (response.errorBody() != null) {
            try {
                String errorBodyStr = response.errorBody().string();
                Gson gson = new Gson();
                ErrorResponse errorResponse = gson.fromJson(errorBodyStr, ErrorResponse.class);

                if (errorResponse != null && errorResponse.getMessage() != null) {
                    return errorResponse.getMessage();
                }
            } catch (Exception e) {
                // If parsing fails, return default message
            }
        }

        return defaultMessage;
    }

    private String parseErrorMessage(ApiResponse<?> apiResponse, Response<?> response) {
        if (apiResponse.getMessage() != null) {
            return apiResponse.getMessage();
        }
        return parseErrorMessage(response);
    }
}

