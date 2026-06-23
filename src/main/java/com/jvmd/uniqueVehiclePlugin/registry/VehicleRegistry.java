package com.jvmd.uniqueVehiclePlugin.registry;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.listener.VehicleListener;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VehicleRegistry {

    private final List<Vehicle> vehicles = new ArrayList<>();
    private Consumer<Vehicle> onRemoveCallback;

    public void setOnRemoveCallback(Consumer<Vehicle> callback) {
        this.onRemoveCallback = callback;
    }

    public void register(Vehicle vehicle) {
        vehicles.add(vehicle);
    }

    public void unregister(Vehicle vehicle) {
        if (onRemoveCallback != null) {
            onRemoveCallback.accept(vehicle);
        }
        vehicle.despawn();
        vehicles.remove(vehicle);
    }

    public void unregisterAll() {
        for (Vehicle vehicle : new ArrayList<>(vehicles)) {
            if (onRemoveCallback != null) {
                onRemoveCallback.accept(vehicle);
            }
            vehicle.despawn();
        }
        vehicles.clear();
    }

    public Vehicle getByEntity(Entity entity) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle.hasEntity(entity)) return vehicle;
        }
        return null;
    }

    public Vehicle getByPassenger(Entity passenger) {
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getSeat() != null && vehicle.getSeat().getPassengers().contains(passenger)) {
                return vehicle;
            }
        }
        return null;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }
}
