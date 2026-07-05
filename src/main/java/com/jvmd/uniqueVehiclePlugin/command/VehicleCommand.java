package com.jvmd.uniqueVehiclePlugin.command;

import com.jvmd.uniqueVehiclePlugin.assembler.VehicleAssembler;
import com.jvmd.uniqueVehiclePlugin.config.VehicleConfig;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.registry.VehicleRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VehicleCommand implements CommandExecutor, TabCompleter {

    private final Map<String, VehicleConfig> vehicleConfigs;
    private final VehicleAssembler assembler;
    private final VehicleRegistry registry;

    public VehicleCommand(Map<String, VehicleConfig> vehicleConfigs, VehicleAssembler assembler,
                          VehicleRegistry registry) {
        this.vehicleConfigs = vehicleConfigs;
        this.assembler = assembler;
        this.registry = registry;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("Usage: /vehicle <spawn|remove|list>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "spawn" -> handleSpawn(player, args);
            case "remove" -> handleRemove(player);
            case "list" -> handleList(player);
            default -> player.sendMessage("Usage: /vehicle <spawn|remove|list>");
        }

        return true;
    }

    private void handleSpawn(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /vehicle spawn <id>");
            return;
        }

        String id = args[1].toLowerCase();
        VehicleConfig config = vehicleConfigs.get(id);
        if (config == null) {
            player.sendMessage("Unknown vehicle: " + id);
            return;
        }

        Vehicle vehicle = assembler.assemble(config, player.getLocation());
        registry.register(vehicle);
        player.sendMessage("Vehicle '" + id + "' spawned.");
    }

    private void handleRemove(Player player) {
        Vehicle vehicle = registry.getByPassenger(player);
        if (vehicle == null) vehicle = findNearest(player);
        if (vehicle == null) {
            player.sendMessage("No vehicle found nearby.");
            return;
        }
        registry.unregister(vehicle);
        player.sendMessage("Vehicle removed.");
    }

    private void handleList(Player player) {
        if (vehicleConfigs.isEmpty()) {
            player.sendMessage("No vehicles configured.");
            return;
        }
        player.sendMessage("Available vehicles: " + String.join(", ", vehicleConfigs.keySet()));
    }

    private Vehicle findNearest(Player player) {
        Vehicle nearest = null;
        double nearestDist = 100.0;
        for (Vehicle vehicle : registry.getVehicles()) {
            double dist = vehicle.getLocation().distanceSquared(player.getLocation());
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = vehicle;
            }
        }
        return nearest;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return List.of("spawn", "remove", "list");
        if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) return new ArrayList<>(vehicleConfigs.keySet());
        return List.of();
    }
}
