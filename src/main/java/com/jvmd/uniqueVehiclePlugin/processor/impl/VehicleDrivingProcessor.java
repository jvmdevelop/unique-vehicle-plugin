package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class VehicleDrivingProcessor extends Processor {

    private static final double MAX_SPEED = 1.2;
    private static final double ACCELERATION = 0.05;
    private static final double FRICTION = 0.02;
    private static final float TURN_RATE = 3.5f;

    public VehicleDrivingProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        Player driver = vehicle.getDriver();
        if (driver == null) {
            applyFriction(vehicle);
            return;
        }

        Input input = driver.getCurrentInput();

        if (input.isForward()) {
            vehicle.setSpeed(Math.min(vehicle.getSpeed() + ACCELERATION, MAX_SPEED));
        }

        if (vehicle.getSpeed() > 0 && (input.isLeft() || input.isRight())) {
            Location loc = vehicle.getLocation();
            float yaw = loc.getYaw();

            if (input.isLeft()) {
                yaw -= TURN_RATE;
            }
            if (input.isRight()) {
                yaw += TURN_RATE;
            }

            loc.setYaw(yaw);
            vehicle.setLocation(loc);
        }

        moveForward(vehicle);
        applyFriction(vehicle);
    }

    private void moveForward(Vehicle vehicle) {
        if (vehicle.getSpeed() <= 0) return;

        Location loc = vehicle.getLocation();
        double rad = Math.toRadians(loc.getYaw());
        double dx = -Math.sin(rad) * vehicle.getSpeed();
        double dz = Math.cos(rad) * vehicle.getSpeed();

        loc.add(dx, 0, dz);
        vehicle.setLocation(loc);
    }

    private void applyFriction(Vehicle vehicle) {
        double speed = vehicle.getSpeed();
        if (speed > 0) {
            vehicle.setSpeed(Math.max(speed - FRICTION, 0));
        }
    }
}
