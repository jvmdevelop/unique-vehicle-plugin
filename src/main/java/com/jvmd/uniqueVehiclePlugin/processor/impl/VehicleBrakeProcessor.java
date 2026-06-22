package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import org.bukkit.Input;
import org.bukkit.entity.Player;

import java.util.List;

public class VehicleBrakeProcessor extends Processor {

    private static final double BRAKE_FORCE = 0.08;

    public VehicleBrakeProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        Player driver = vehicle.getDriver();
        if (driver == null) return;

        Input input = driver.getCurrentInput();

        if (input.isBackward() && vehicle.getSpeed() > 0) {
            vehicle.setSpeed(Math.max(vehicle.getSpeed() - BRAKE_FORCE, 0));
        }
    }
}
