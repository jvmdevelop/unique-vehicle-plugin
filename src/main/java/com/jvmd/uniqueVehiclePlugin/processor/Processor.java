package com.jvmd.uniqueVehiclePlugin.processor;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;

import java.util.List;

public abstract class Processor {
    protected final List<Vehicle> vehicles;

    protected Processor(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    // process every tick on the main thread
    public void process() {
        for (Vehicle vehicle : vehicles) {
            processVehicle(vehicle);
        }
    }

    protected abstract void processVehicle(Vehicle vehicle);
}
