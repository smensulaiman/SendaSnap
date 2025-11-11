package com.sendajapan.sendasnap.networking;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.sendajapan.sendasnap.models.ApiResponse;
import com.sendajapan.sendasnap.models.ErrorResponse;
import com.sendajapan.sendasnap.models.UserData;
import com.sendajapan.sendasnap.models.UsersResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApiManager {

    private static ApiManager instance;
    private final ApiService apiService;
    private final Context context;

    private ApiManager(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = RetrofitClient.getInstance(context).getApiService();
    }

    public static synchronized ApiManager getInstance(Context context) {
        if (instance == null) {
            instance = new ApiManager(context);
        }
        return instance;
    }

    /**
     * Get all users from the API
     * @param callback Callback to handle success or error
     */
    public void getUsers(ApiCallback<List<UserData>> callback) {
        Call<ApiResponse<UsersResponse.UsersData>> call = apiService.getUsers();
        call.enqueue(new Callback<ApiResponse<UsersResponse.UsersData>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<UsersResponse.UsersData>> call,
                                   @NonNull Response<ApiResponse<UsersResponse.UsersData>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UsersResponse.UsersData> apiResponse = response.body();
                    
                    // Check if the API call was successful
                    if (apiResponse.getSuccess() != null && apiResponse.getSuccess()) {
                        // Extract users from the response
                        if (apiResponse.getData() != null && apiResponse.getData().getUsers() != null) {
                            callback.onSuccess(apiResponse.getData().getUsers());
                        } else {
                            callback.onError("No users data received", response.code());
                        }
                    } else {
                        // API returned success: false
                        String errorMessage = apiResponse.getMessage() != null 
                                ? apiResponse.getMessage() 
                                : "Failed to retrieve users";
                        callback.onError(errorMessage, response.code());
                    }
                } else {
                    // HTTP error response (4xx, 5xx)
                    String errorMessage = parseErrorMessage(response);
                    callback.onError(errorMessage, response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<UsersResponse.UsersData>> call,
                                  @NonNull Throwable t) {
                String errorMessage = "Network error. Please check your connection and try again.";
                if (t.getMessage() != null) {
                    errorMessage = t.getMessage();
                }
                callback.onError(errorMessage, 0);
            }
        });
    }

    /**
     * Parse error message from error response body
     * @param response The Retrofit response
     * @return Error message string
     */
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
}

