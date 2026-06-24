package com.jvmd.uniqueVehiclePlugin.command;

import com.jvmd.uniqueVehiclePlugin.config.PartConfig;
import com.jvmd.uniqueVehiclePlugin.customization.PartCustomization;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PivotVisualizer {

    private final Plugin plugin;
    // player -> (pivotName -> task)
    private final Map<UUID, Map<String, BukkitTask>> tasks = new HashMap<>();

    public PivotVisualizer(Plugin plugin) {
        this.plugin = plugin;
    }

    public void show(Player player, Vehicle vehicle, String pivotName) {
        // Cancel existing task for this pivot if any
        hide(player, pivotName);

        ItemDisplay model = resolveModel(vehicle, pivotName);
        if (model == null) return;

        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || model.isDead()) {
                hide(player, pivotName);
                return;
            }
            Location pivotWorld = computePivotWorld(vehicle, pivotName, model);
            if (pivotWorld == null) return;

            player.spawnParticle(Particle.END_ROD, pivotWorld, 1, 0, 0, 0, 0);
        }, 0L, 2L);

        tasks.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(pivotName, task);
    }

    public boolean hide(Player player, String pivotName) {
        Map<String, BukkitTask> playerTasks = tasks.get(player.getUniqueId());
        if (playerTasks == null) return false;
        BukkitTask task = playerTasks.remove(pivotName);
        if (task == null) return false;
        task.cancel();
        if (playerTasks.isEmpty()) tasks.remove(player.getUniqueId());
        return true;
    }

    public void hideAll(Player player) {
        Map<String, BukkitTask> playerTasks = tasks.remove(player.getUniqueId());
        if (playerTasks == null) return;
        playerTasks.values().forEach(BukkitTask::cancel);
    }

    private Location computePivotWorld(Vehicle vehicle, String pivotName, ItemDisplay model) {
        PartConfig cfgPart = resolveCfgPart(vehicle, pivotName);
        if (cfgPart == null) return null;

        String partKey = pivotName.toLowerCase();
        PartCustomization part = vehicle.getCustomization().getPart(partKey);

        double finalX = cfgPart.pivot().x + part.getPivotDeltaX();
        double finalY = cfgPart.pivot().y + part.getPivotDeltaY();
        double finalZ = cfgPart.pivot().z + part.getPivotDeltaZ();

        // For wheels X is fixed from config (no deltaX applied)
        if (partKey.startsWith("wheel_")) {
            finalX = cfgPart.pivot().x;
        }

        // Rotate model-space pivot to world space using vehicle yaw
        double rad = Math.toRadians(vehicle.getLocation().getYaw());
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double worldDx = finalX * cos - finalZ * sin;
        double worldDz = finalX * sin + finalZ * cos;

        Location entityLoc = model.getLocation();
        return entityLoc.clone().add(worldDx, finalY, worldDz);
    }

    static ItemDisplay resolveModel(Vehicle vehicle, String pivotName) {
        return switch (pivotName.toLowerCase()) {
            case "steering_wheel" -> vehicle.getSteeringWheel();
            case "wheel_0" -> vehicle.getWheels().length > 0 ? vehicle.getWheels()[0].getModel() : null;
            case "wheel_1" -> vehicle.getWheels().length > 1 ? vehicle.getWheels()[1].getModel() : null;
            case "wheel_2" -> vehicle.getWheels().length > 2 ? vehicle.getWheels()[2].getModel() : null;
            case "wheel_3" -> vehicle.getWheels().length > 3 ? vehicle.getWheels()[3].getModel() : null;
            default -> null;
        };
    }

    static PartConfig resolveCfgPart(Vehicle vehicle, String pivotName) {
        return switch (pivotName.toLowerCase()) {
            case "steering_wheel" -> vehicle.getConfig().steeringWheel();
            case "wheel_0" -> vehicle.getConfig().wheels().size() > 0 ? vehicle.getConfig().wheels().get(0).part() : null;
            case "wheel_1" -> vehicle.getConfig().wheels().size() > 1 ? vehicle.getConfig().wheels().get(1).part() : null;
            case "wheel_2" -> vehicle.getConfig().wheels().size() > 2 ? vehicle.getConfig().wheels().get(2).part() : null;
            case "wheel_3" -> vehicle.getConfig().wheels().size() > 3 ? vehicle.getConfig().wheels().get(3).part() : null;
            default -> null;
        };
    }
}
