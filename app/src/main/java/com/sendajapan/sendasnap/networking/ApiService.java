package com.sendajapan.sendasnap.networking;

import com.sendajapan.sendasnap.models.Vehicle;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import java.util.List;
import okhttp3.MultipartBody;

public interface ApiService {

    // Search vehicle by chassis number
    @GET("vehicle/{chassisNumber}")
    Call<Vehicle> getVehicleByChassisNumber(@Path("chassisNumber") String chassisNumber);

    // Upload vehicle images
    @Multipart
    @POST("vehicle/{id}/images")
    Call<ResponseBody> uploadVehicleImages(
            @Path("id") String vehicleId,
            @Part List<MultipartBody.Part> images);

    // Get recent vehicles (placeholder for future implementation)
    @GET("vehicles/recent")
    Call<List<Vehicle>> getRecentVehicles();

    // Login endpoint (placeholder for future implementation)
    @POST("auth/login")
    Call<ResponseBody> login(@Part("username") String username, @Part("password") String password);
}
