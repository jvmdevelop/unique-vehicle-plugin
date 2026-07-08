package com.jvmd.uniqueVehiclePlugin.listener;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.manager.impl.SeatPacketManager;
import com.jvmd.uniqueVehiclePlugin.processor.impl.VehiclePhysicsProcessor;
import com.jvmd.uniqueVehiclePlugin.registry.VehicleRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;

public class VehicleListener implements Listener {

    private final VehicleRegistry registry;
    private final VehiclePhysicsProcessor physicsProcessor;
    private final SeatPacketManager seatPacketController;

    public VehicleListener(VehicleRegistry registry, VehiclePhysicsProcessor physicsProcessor, SeatPacketManager seatPacketController) {
        this.registry = registry;
        this.physicsProcessor = physicsProcessor;
        this.seatPacketController = seatPacketController;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Vehicle vehicle = registry.getByEntity(event.getRightClicked());
        if (vehicle == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        if (vehicle.isDriver(player) || vehicle.isPassenger(player)) return;

        if (vehicle.getDriver() != null) {
            seatPacketController.enter(vehicle, player);
            return;
        }

        vehicle.setDriver(player);
        seatPacketController.enter(vehicle, player);
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (!event.isSneaking()) return;

        Player player = event.getPlayer();
        Vehicle vehicle = registry.getByDriver(player);
        if (vehicle == null) return;

        boolean wasDriver = vehicle.isDriver(player);
        seatPacketController.exit(vehicle, player);
        if (wasDriver) {
            vehicle.clearDriver(player);
            vehicle.setSpeed(0);
            vehicle.setDrifting(false);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        handlePlayerLeave(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        handlePlayerLeave(event.getPlayer());
    }

    private void handlePlayerLeave(Player player) {
        Vehicle vehicle = registry.getByDriver(player);
        if (vehicle == null) return;

        boolean wasDriver = vehicle.isDriver(player);
        seatPacketController.exit(vehicle, player);
        if (wasDriver) {
            vehicle.clearDriver(player);
            vehicle.setSpeed(0);
            vehicle.setDrifting(false);
        }
    }

    public void onVehicleRemoved(Vehicle vehicle) {
        Player driver = vehicle.getDriver();
        if (driver != null) {
            seatPacketController.exit(vehicle, driver);
            vehicle.clearDriver(driver);
        }
        for (var passenger : new ArrayList<>(vehicle.getPassenger().values())) {
            seatPacketController.exit(vehicle, passenger.left());
        }
        physicsProcessor.clearVehicle(vehicle.getFrame().getItemDisplay().getUniqueId());
    }
}
