package com.jvmd.uniqueVehiclePlugin.listener;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.registry.VehicleRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class VehicleListener implements Listener {

    private final VehicleRegistry registry;

    public VehicleListener(VehicleRegistry registry) {
        this.registry = registry;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Vehicle vehicle = registry.getByEntity(event.getRightClicked());
        if (vehicle == null) return;

        event.setCancelled(true);

        if (vehicle.getDriver() != null) return;

        vehicle.getSeat().addPassenger(event.getPlayer());
    }

    @EventHandler
    public void onVehicleExit(VehicleExitEvent event) {
        if (!(event.getExited() instanceof Player)) return;

        Vehicle vehicle = registry.getByEntity(event.getVehicle());
        if (vehicle == null) return;

        vehicle.setSpeed(0);
        vehicle.setDrifting(false);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Vehicle vehicle = registry.getByPassenger(event.getPlayer());
        if (vehicle == null) return;

        vehicle.getSeat().eject();
        vehicle.setSpeed(0);
        vehicle.setDrifting(false);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Vehicle vehicle = registry.getByPassenger(event.getPlayer());
        if (vehicle == null) return;

        vehicle.getSeat().eject();
        vehicle.setSpeed(0);
        vehicle.setDrifting(false);
    }
}
