package com.jvmd.uniqueVehiclePlugin.listener;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.processor.impl.VehiclePhysicsProcessor;
import com.jvmd.uniqueVehiclePlugin.registry.VehicleRegistry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class VehicleListener implements Listener {

    private final VehicleRegistry registry;
    private final VehiclePhysicsProcessor physicsProcessor;

    public VehicleListener(VehicleRegistry registry, VehiclePhysicsProcessor physicsProcessor) {
        this.registry = registry;
        this.physicsProcessor = physicsProcessor;
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Vehicle vehicle = registry.getByEntity(event.getRightClicked());
        if (vehicle == null) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        if (player.isSneaking()) {
            // Shift + RMB = toggle doors
            vehicle.toggleDoors();
            return;
        }

        // Normal RMB = enter vehicle (only if a door is open)
        if (vehicle.getDriver() != null) return;

        if (!vehicle.hasOpenDoor()) return;

        vehicle.getSeat().addPassenger(player);
        // Close doors after entering
        vehicle.closeAllDoors();
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Vehicle vehicle = registry.getByEntity(event.getDismounted());
        if (vehicle == null) return;

        vehicle.setSpeed(0);
        vehicle.setDrifting(false);
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
        Vehicle vehicle = registry.getByPassenger(player);
        if (vehicle == null) return;

        vehicle.getSeat().eject();
        vehicle.setSpeed(0);
        vehicle.setDrifting(false);
    }

    public void onVehicleRemoved(Vehicle vehicle) {
        physicsProcessor.clearVehicle(vehicle.getFrame().getItemDisplay().getUniqueId());
    }
}
