package com.sendajapan.sendasnap.utils;

import android.content.Context;
import com.sendajapan.sendasnap.models.Vehicle;
import java.util.ArrayList;
import java.util.List;

public class VehicleCache {
    private static VehicleCache instance;
    private final SharedPrefsManager prefsManager;
    private static final int MAX_CACHE_SIZE = 50;

    private VehicleCache(Context context) {
        prefsManager = SharedPrefsManager.getInstance(context);
    }

    public static synchronized VehicleCache getInstance(Context context) {
        if (instance == null) {
            instance = new VehicleCache(context.getApplicationContext());
        }
        return instance;
    }

    public void addVehicle(Vehicle vehicle) {
        prefsManager.addVehicleToCache(vehicle);
    }

    public List<Vehicle> getAllVehicles() {
        return prefsManager.getRecentVehicles();
    }

    public List<Vehicle> getRecentVehicles(int limit) {
        List<Vehicle> allVehicles = getAllVehicles();
        if (allVehicles.size() <= limit) {
            return allVehicles;
        }
        return allVehicles.subList(0, limit);
    }

    public Vehicle getVehicleById(String id) {
        List<Vehicle> vehicles = getAllVehicles();
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getId() != null && vehicle.getId().equals(id)) {
                return vehicle;
            }
        }
        return null;
    }

    public List<Vehicle> searchVehicles(String query) {
        List<Vehicle> allVehicles = getAllVehicles();
        List<Vehicle> filteredVehicles = new ArrayList<>();

        String lowerQuery = query.toLowerCase();

        for (Vehicle vehicle : allVehicles) {
            if (vehicle.getMake() != null && vehicle.getMake().toLowerCase().contains(lowerQuery) ||
                    vehicle.getModel() != null && vehicle.getModel().toLowerCase().contains(lowerQuery) ||
                    vehicle.getChassisModel() != null && vehicle.getChassisModel().toLowerCase().contains(lowerQuery) ||
                    vehicle.getSerialNumber() != null && vehicle.getSerialNumber().toLowerCase().contains(lowerQuery) ||
                    vehicle.getYear() != null && vehicle.getYear().contains(query)) {
                filteredVehicles.add(vehicle);
            }
        }

        return filteredVehicles;
    }

    public void clearCache() {
        prefsManager.clearVehicleCache();
    }

    public int getCacheSize() {
        return prefsManager.getCacheSize();
    }

    public boolean isCacheEmpty() {
        return getCacheSize() == 0;
    }
}
