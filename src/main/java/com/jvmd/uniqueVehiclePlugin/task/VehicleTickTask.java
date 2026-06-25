package com.jvmd.uniqueVehiclePlugin.task;

import com.jvmd.uniqueVehiclePlugin.gui.VehiclePartEditor;
import com.jvmd.uniqueVehiclePlugin.manager.impl.VehicleBodyManager;
import com.jvmd.uniqueVehiclePlugin.manager.impl.VehicleMovingManager;
import com.jvmd.uniqueVehiclePlugin.protocol.VehicleSeatPacketController;
import com.jvmd.uniqueVehiclePlugin.registry.VehicleRegistry;
import org.bukkit.scheduler.BukkitRunnable;

public class VehicleTickTask extends BukkitRunnable {

    private final VehicleMovingManager movingManager;
    private final VehicleBodyManager bodyManager;
    private final VehicleRegistry registry;
    private final VehiclePartEditor partEditor;
    private final VehicleSeatPacketController seatPacketController;

    public VehicleTickTask(VehicleMovingManager movingManager, VehicleBodyManager bodyManager,
                           VehicleRegistry registry, VehiclePartEditor partEditor,
                           VehicleSeatPacketController seatPacketController) {
        this.movingManager = movingManager;
        this.bodyManager = bodyManager;
        this.registry = registry;
        this.partEditor = partEditor;
        this.seatPacketController = seatPacketController;
    }

    @Override
    public void run() {
        // Always tick the part editor (active even when no vehicles are moving)
        partEditor.tick();

        if (registry.getVehicles().isEmpty()) return;

        movingManager.manage();
        bodyManager.manage();

        for (var vehicle : registry.getVehicles()) {
            vehicle.teleportSeat();
            seatPacketController.sync(vehicle);
        }
    }
}
