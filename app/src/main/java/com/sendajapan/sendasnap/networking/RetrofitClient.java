package com.sendajapan.sendasnap.networking;

import android.content.Context;

import com.sendajapan.sendasnap.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    private static final String BASE_URL = "https://snap.senda.fit/";

    private static RetrofitClient instance;
    private final ApiService apiService;

    private RetrofitClient(Context context) {

        AuthInterceptor authInterceptor = new AuthInterceptor(context.getApplicationContext());

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS);

        // Only enable logging in debug builds for security
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            clientBuilder.addInterceptor(loggingInterceptor);
        }

        OkHttpClient okHttpClient = clientBuilder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized RetrofitClient getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitClient(context);
        }
        return instance;
    }

    public ApiService getApiService() {
        return apiService;
    }
}
