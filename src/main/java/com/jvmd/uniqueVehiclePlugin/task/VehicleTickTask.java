package com.jvmd.uniqueVehiclePlugin.task;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.manager.impl.VehicleBodyManager;
import com.jvmd.uniqueVehiclePlugin.manager.impl.VehicleMovingManager;
import com.jvmd.uniqueVehiclePlugin.registry.VehicleRegistry;
import org.bukkit.scheduler.BukkitRunnable;

public class VehicleTickTask extends BukkitRunnable {

    private final VehicleMovingManager movingManager;
    private final VehicleBodyManager bodyManager;
    private final VehicleRegistry registry;

    public VehicleTickTask(VehicleMovingManager movingManager, VehicleBodyManager bodyManager, VehicleRegistry registry) {
        this.movingManager = movingManager;
        this.bodyManager = bodyManager;
        this.registry = registry;
    }

    @Override
    public void run() {
        if (registry.getVehicles().isEmpty()) return;

        movingManager.manage();
        bodyManager.manage();

        for (Vehicle vehicle : registry.getVehicles()) {
            vehicle.teleportSeat();
        }
    }
}
