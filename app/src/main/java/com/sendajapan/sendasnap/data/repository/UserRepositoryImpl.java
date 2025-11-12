package com.sendajapan.sendasnap.data.repository;

import android.content.Context;
import com.google.gson.Gson;
import com.sendajapan.sendasnap.data.dto.UserDto;
import com.sendajapan.sendasnap.data.dto.UsersListResponseDto;
import com.sendajapan.sendasnap.data.mapper.UserMapper;
import com.sendajapan.sendasnap.domain.repository.UserRepository;
import com.sendajapan.sendasnap.models.UserData;
import com.sendajapan.sendasnap.models.ApiResponse;
import com.sendajapan.sendasnap.models.ErrorResponse;
import com.sendajapan.sendasnap.networking.ApiService;
import com.sendajapan.sendasnap.networking.RetrofitClient;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepositoryImpl implements UserRepository {

    private final ApiService apiService;
    private final Context context;

    public UserRepositoryImpl(Context context) {
        this.context = context.getApplicationContext();
        this.apiService = RetrofitClient.getInstance(context).getApiService();
    }

    @Override
    public void list(UserRepositoryCallback<List<UserData>> callback) {
        Call<ApiResponse<UsersListResponseDto>> call = apiService.getUsersList();
        call.enqueue(new Callback<ApiResponse<UsersListResponseDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<UsersListResponseDto>> call,
                                 Response<ApiResponse<UsersListResponseDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UsersListResponseDto> apiResponse = response.body();
                    
                    if (apiResponse.getSuccess() != null && apiResponse.getSuccess()) {
                        if (apiResponse.getData() != null && apiResponse.getData().getUsers() != null) {
                            List<UserData> users = UserMapper.toDomainList(apiResponse.getData().getUsers());
                            callback.onSuccess(users);
                        } else {
                            callback.onError("No users data received", response.code());
                        }
                    } else {
                        String errorMessage = apiResponse.getMessage() != null 
                                ? apiResponse.getMessage() 
                                : "Failed to retrieve users";
                        callback.onError(errorMessage, response.code());
                    }
                } else {
                    String errorMessage = parseErrorMessage(response);
                    callback.onError(errorMessage, response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UsersListResponseDto>> call, Throwable t) {
                String errorMessage = "Network error. Please check your connection and try again.";
                if (t.getMessage() != null) {
                    errorMessage = t.getMessage();
                }
                callback.onError(errorMessage, 0);
            }
        });
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
}

