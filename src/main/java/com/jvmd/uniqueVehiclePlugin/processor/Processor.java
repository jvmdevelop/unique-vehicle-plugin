package com.jvmd.uniqueVehiclePlugin.processor;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;

import java.util.List;
import java.util.concurrent.Executors;

public abstract class Processor {
    protected final List<Vehicle> vehicles;

    protected Processor(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    // process every tick
    public void process() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Vehicle vehicle : vehicles) {
                executor.submit(() -> processVehicle(vehicle));
            }
        }
    }

    protected abstract void processVehicle(Vehicle vehicle);
}
