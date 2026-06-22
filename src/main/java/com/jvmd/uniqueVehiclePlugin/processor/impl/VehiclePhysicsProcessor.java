package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.List;

public class VehiclePhysicsProcessor extends Processor {

    private static final double GRAVITY = 0.08;
    private static final double MAX_FALL_SPEED = 1.5;

    public VehiclePhysicsProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        Location loc = vehicle.getLocation();

        Block below = loc.clone().subtract(0, 0.1, 0).getBlock();

        if (below.getType().isAir()) {
            double fallSpeed = Math.min(GRAVITY, MAX_FALL_SPEED);
            loc.subtract(0, fallSpeed, 0);
        } else {
            double groundY = below.getY() + 1;
            if (loc.getY() < groundY) {
                loc.setY(groundY);
            }
        }

        vehicle.setLocation(loc);
    }
}
