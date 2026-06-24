package com.jvmd.uniqueVehiclePlugin.processor;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;

import java.util.List;
import java.util.logging.Logger;

public abstract class Processor {
    protected final List<Vehicle> vehicles;
    protected static final Logger LOGGER = Logger.getLogger("UniqueVehiclePlugin");

    private int tickCount = 0;
    private static final int LOG_INTERVAL = 20;

    protected Processor(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public void process() {
        boolean doLog = (tickCount % LOG_INTERVAL) == 0;
        tickCount++;
        for (Vehicle vehicle : vehicles) {
            processVehicle(vehicle, doLog);
        }
    }

    protected void processVehicle(Vehicle vehicle, boolean log) {
        processVehicle(vehicle);
    }

    protected abstract void processVehicle(Vehicle vehicle);
}
