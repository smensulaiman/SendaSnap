package com.sendajapan.sendasnap.networking;

import com.sendajapan.sendasnap.models.ChangePasswordRequest;
import com.sendajapan.sendasnap.models.LoginRequest;
import com.sendajapan.sendasnap.models.LoginResponse;
import com.sendajapan.sendasnap.models.Vehicle;
import com.sendajapan.sendasnap.models.VehicleSearchResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import java.util.List;
import okhttp3.MultipartBody;

public interface ApiService {

    // Login
    @POST("api/v1/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    // Refresh token
    @POST("api/v1/auth/refresh")
    Call<LoginResponse> refreshToken();

    // Change password
    @POST("api/v1/auth/change-password")
    Call<ResponseBody> changePassword(@Body ChangePasswordRequest changePasswordRequest);

    // Search vehicle by chassis number
    @GET("vehicle/{chassisNumber}")
    Call<Vehicle> getVehicleByChassisNumber(@Path("chassisNumber") String chassisNumber);

    // Upload vehicle images
    @Multipart
    @POST("vehicle/{id}/images")
    Call<ResponseBody> uploadVehicleImages(
            @Path("id") String vehicleId,
            @Part List<MultipartBody.Part> images);

    // Get recent vehicles
    @GET("vehicles/recent")
    Call<List<Vehicle>> getRecentVehicles();

    // Search vehicles
    @GET("api/v1/vehicles/search")
    Call<VehicleSearchResponse> searchVehicles(
            @Query("search_type") String searchType,
            @Query("search_query") String searchQuery);

}
