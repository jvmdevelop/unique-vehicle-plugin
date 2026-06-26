package com.jvmd.uniqueVehiclePlugin.task;

import com.jvmd.uniqueVehiclePlugin.manager.impl.SeatPacketManager;
import com.jvmd.uniqueVehiclePlugin.manager.impl.VehicleBodyManager;
import com.jvmd.uniqueVehiclePlugin.manager.impl.VehicleMovingManager;
import com.jvmd.uniqueVehiclePlugin.registry.VehicleRegistry;
import org.bukkit.scheduler.BukkitRunnable;

public class VehicleTickTask extends BukkitRunnable {

    private final VehicleMovingManager movingManager;
    private final VehicleBodyManager bodyManager;
    private final VehicleRegistry registry;
    private final SeatPacketManager seatPacketController;

    public VehicleTickTask(VehicleMovingManager movingManager, VehicleBodyManager bodyManager,
                           VehicleRegistry registry, SeatPacketManager seatPacketController) {
        this.movingManager = movingManager;
        this.bodyManager = bodyManager;
        this.registry = registry;
        this.seatPacketController = seatPacketController;
    }

    @Override
    public void run() {
        if (registry.getVehicles().isEmpty()) return;

        movingManager.manage();
        bodyManager.manage();

        for (var vehicle : registry.getVehicles()) {
            vehicle.teleportSeat();
            seatPacketController.sync(vehicle);
        }
    }
}
