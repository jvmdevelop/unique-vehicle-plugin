package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.entity.Wheel;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.List;

public class VehicleDriftProcessor extends Processor {

    private static final double DRIFT_BRAKE_FORCE = 0.04;
    private static final float DRIFT_TURN_MULTIPLIER = 1.8f;

    public VehicleDriftProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        Player driver = vehicle.getDriver();
        if (driver == null) {
            vehicle.setDrifting(false);
            return;
        }

        Input input = driver.getCurrentInput();

        if (input.isJump() && vehicle.getSpeed() > 0.1) {
            vehicle.setDrifting(true);
            vehicle.setSpeed(Math.max(vehicle.getSpeed() - DRIFT_BRAKE_FORCE, 0));

            if (input.isLeft() || input.isRight()) {
                Location loc = vehicle.getLocation();
                float yaw = loc.getYaw();
                float turn = DRIFT_TURN_MULTIPLIER;

                if (input.isLeft()) yaw -= turn;
                if (input.isRight()) yaw += turn;

                loc.setYaw(yaw);
                vehicle.setLocation(loc);
            }

            spawnDriftSmoke(vehicle);
        } else {
            vehicle.setDrifting(false);
        }
    }

    private void spawnDriftSmoke(Vehicle vehicle) {
        Location vehicleLoc = vehicle.getLocation();
        double rad = Math.toRadians(vehicleLoc.getYaw());
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        for (Wheel wheel : vehicle.getWheels()) {
            double ox = wheel.getOffset().x;
            double oy = wheel.getOffset().y;
            double oz = wheel.getOffset().z;

            double rx = ox * cos - oz * sin;
            double rz = ox * sin + oz * cos;

            Location wheelLoc = vehicleLoc.clone().add(rx, oy, rz);
            vehicleLoc.getWorld().spawnParticle(Particle.CAMPFIRE_COSY_SMOKE, wheelLoc, 1, 0.1, 0, 0.1, 0.01);
        }
    }
}
