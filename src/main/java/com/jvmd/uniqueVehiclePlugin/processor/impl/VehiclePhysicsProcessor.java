package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VehiclePhysicsProcessor extends Processor {

    private static final double GRAVITY = 0.08;
    private static final double MAX_FALL_SPEED = 1.0;
    private final Map<UUID, Double> fallVelocity = new HashMap<>();

    public VehiclePhysicsProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        Location loc = vehicle.getLocation();
        UUID id = vehicle.getFrame().getItemDisplay().getUniqueId();

        boolean onGround = isOnGround(loc);

        if (!onGround) {
            double vel = fallVelocity.getOrDefault(id, 0.0);
            vel = Math.min(vel + GRAVITY, MAX_FALL_SPEED);
            fallVelocity.put(id, vel);

            double remaining = vel;
            while (remaining > 0) {
                double step = Math.min(remaining, 0.5);
                loc.subtract(0, step, 0);
                remaining -= step;

                if (isOnGround(loc)) {
                    Block ground = loc.clone().subtract(0, 0.05, 0).getBlock();
                    loc.setY(ground.getY() + 1);
                    fallVelocity.put(id, 0.0);
                    break;
                }
            }
        } else {
            fallVelocity.put(id, 0.0);
            Block below = loc.clone().subtract(0, 0.05, 0).getBlock();
            if (below.getType().isSolid()) {
                double groundY = below.getY() + 1;
                if (loc.getY() < groundY) {
                    loc.setY(groundY);
                }
            }
        }

        vehicle.setLocation(loc);
    }

    public void clearVehicle(UUID frameId) {
        fallVelocity.remove(frameId);
    }

    private boolean isOnGround(Location loc) {
        Block below = loc.clone().subtract(0, 0.05, 0).getBlock();
        return below.getType().isSolid();
    }
}
