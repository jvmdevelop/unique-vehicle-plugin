package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.config.PhysicsConfig;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import org.bukkit.Input;
import org.bukkit.entity.Player;

import java.util.List;

public class VehicleBrakeProcessor extends Processor {

    public VehicleBrakeProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        Player driver = vehicle.getDriver();
        if (driver == null) return;

        Input input = driver.getCurrentInput();
        PhysicsConfig physics = vehicle.getPhysics();

        if (input.isBackward()) {
            if (vehicle.getSpeed() > 0) {
                vehicle.setSpeed(Math.max(vehicle.getSpeed() - physics.brakeForce(), 0));
            } else {
                double newSpeed = vehicle.getSpeed() - physics.reverseAcceleration();
                vehicle.setSpeed(Math.max(newSpeed, -physics.reverseMaxSpeed()));
            }
        }
    }
}
