package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.config.PhysicsConfig;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public class VehicleDrivingProcessor extends Processor {

    public VehicleDrivingProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        Player driver = vehicle.getDriver();
        PhysicsConfig physics = vehicle.getPhysics();

        if (driver == null) {
            applyFriction(vehicle, physics);
            return;
        }

        Input input = driver.getCurrentInput();

        if (input.isForward()) {
            double newSpeed = vehicle.getSpeed() + physics.acceleration();
            vehicle.setSpeed(Math.min(newSpeed, physics.maxSpeed()));
        }

        double speed = vehicle.getSpeed();
        if (speed != 0 && (input.isLeft() || input.isRight())) {
            Location loc = vehicle.getLocation();
            float yaw = loc.getYaw();
            float speedFactor = (float) Math.min(Math.abs(speed) / (physics.maxSpeed() * 0.5), 1.0);
            float effectiveTurn = physics.turnRate() * speedFactor;

            if (speed < 0) effectiveTurn = -effectiveTurn;

            if (input.isLeft()) yaw -= effectiveTurn;
            if (input.isRight()) yaw += effectiveTurn;

            loc.setYaw(yaw);
            vehicle.setLocation(loc);
        }

        moveVehicle(vehicle, physics);
        applyFriction(vehicle, physics);
    }

    private void moveVehicle(Vehicle vehicle, PhysicsConfig physics) {
        double speed = vehicle.getSpeed();
        if (speed == 0) return;

        Location loc = vehicle.getLocation();
        double rad = Math.toRadians(loc.getYaw());
        double dx = -Math.sin(rad) * speed;
        double dz = Math.cos(rad) * speed;

        Location next = loc.clone().add(dx, 0, dz);

        boolean movingForward = speed > 0;
        if (isBlocked(next, rad, physics, movingForward)) {
            Location stepUp = next.clone().add(0, 1, 0);
            if (!isBlocked(stepUp, rad, physics, movingForward) && movingForward) {
                next.setY(next.getY() + 1);
            } else {
                vehicle.setSpeed(0);
                return;
            }
        }

        vehicle.setLocation(next);
    }

    private boolean isBlocked(Location loc, double rad, PhysicsConfig physics, boolean movingForward) {
        double halfWidth = physics.vehicleWidth() / 2.0;
        double halfLength = physics.vehicleLength() / 2.0;

        double forwardX = -Math.sin(rad);
        double forwardZ = Math.cos(rad);
        double sideX = Math.cos(rad);
        double sideZ = Math.sin(rad);

        for (double h = 0.5; h <= 1.5; h += 1.0) {
            double dirX = movingForward ? forwardX : -forwardX;
            double dirZ = movingForward ? forwardZ : -forwardZ;

            if (isSolidAt(loc, dirX * halfLength, h, dirZ * halfLength)) return true;
            if (isSolidAt(loc, dirX * halfLength + sideX * halfWidth, h, dirZ * halfLength + sideZ * halfWidth)) return true;
            if (isSolidAt(loc, dirX * halfLength - sideX * halfWidth, h, dirZ * halfLength - sideZ * halfWidth)) return true;

            if (isSolidAt(loc, sideX * halfWidth, h, sideZ * halfWidth)) return true;
            if (isSolidAt(loc, -sideX * halfWidth, h, -sideZ * halfWidth)) return true;
        }
        return false;
    }

    private boolean isSolidAt(Location base, double dx, double dy, double dz) {
        Block block = base.clone().add(dx, dy, dz).getBlock();
        return block.getType().isSolid();
    }

    private void applyFriction(Vehicle vehicle, PhysicsConfig physics) {
        double speed = vehicle.getSpeed();
        if (speed > 0) {
            vehicle.setSpeed(Math.max(speed - physics.friction(), 0));
        } else if (speed < 0) {
            vehicle.setSpeed(Math.min(speed + physics.friction(), 0));
        }
    }
}
