package com.sendajapan.sendasnap;

import android.app.Application;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;

public class MyApplication extends Application {

    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase database = FirebaseDatabase.getInstance();

        try {
            database.setPersistenceEnabled(false);
        } catch (Exception e) {
            Log.w(TAG, "Firebase persistence may already be enabled: " + e.getMessage());
        }

        Log.d(TAG, "MyApplication initialized");
    }
}

