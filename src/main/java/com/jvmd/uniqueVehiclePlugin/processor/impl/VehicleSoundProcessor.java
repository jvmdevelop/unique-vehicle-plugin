package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;

public class VehicleSoundProcessor extends Processor {

    private int tickCounter = 0;

    public VehicleSoundProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    public void process() {
        tickCounter++;
        super.process();
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        Player driver = vehicle.getDriver();
        if (driver == null) return;

        double speed = Math.abs(vehicle.getSpeed());
        Location loc = vehicle.getLocation();

        if (speed > 0.01) {
            int interval = Math.max(2, (int) (10 - speed * 6));
            if (tickCounter % interval == 0) {
                float pitch = 0.5f + (float) (speed / vehicle.getPhysics().maxSpeed()) * 1.2f;
                loc.getWorld().playSound(loc, Sound.ENTITY_MINECART_RIDING, 0.3f, pitch);
            }
        }

        if (vehicle.isDrifting() && speed > 0.3 && tickCounter % 4 == 0) {
            loc.getWorld().playSound(loc, Sound.ENTITY_PHANTOM_FLAP, 0.5f, 1.8f);
        }
    }
}
