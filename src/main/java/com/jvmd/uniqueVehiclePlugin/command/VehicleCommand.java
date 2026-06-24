package com.jvmd.uniqueVehiclePlugin.command;

import com.jvmd.uniqueVehiclePlugin.assembler.VehicleAssembler;
import com.jvmd.uniqueVehiclePlugin.config.PartConfig;
import com.jvmd.uniqueVehiclePlugin.config.VehicleConfig;
import com.jvmd.uniqueVehiclePlugin.customization.PartCustomization;
import com.jvmd.uniqueVehiclePlugin.customization.VehicleCustomization;
import com.jvmd.uniqueVehiclePlugin.customization.VehicleDatabase;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.gui.VehicleEditorGui;
import com.jvmd.uniqueVehiclePlugin.registry.VehicleRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VehicleCommand implements CommandExecutor, TabCompleter {

    private static final List<String> PIVOT_NAMES = List.of(
            "steering_wheel", "wheel_0", "wheel_1", "wheel_2", "wheel_3");

    private final Map<String, VehicleConfig> vehicleConfigs;
    private final VehicleAssembler assembler;
    private final VehicleRegistry registry;
    private final VehicleDatabase database;
    private final VehicleEditorGui editorGui;
    private final PivotVisualizer pivotVisualizer;

    public VehicleCommand(Map<String, VehicleConfig> vehicleConfigs, VehicleAssembler assembler,
                          VehicleRegistry registry, VehicleDatabase database,
                          VehicleEditorGui editorGui, Plugin plugin) {
        this.vehicleConfigs = vehicleConfigs;
        this.assembler = assembler;
        this.registry = registry;
        this.database = database;
        this.editorGui = editorGui;
        this.pivotVisualizer = new PivotVisualizer(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("Usage: /vehicle <spawn|remove|list|edit>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "spawn"  -> handleSpawn(player, args);
            case "remove" -> handleRemove(player);
            case "list"   -> handleList(player);
            case "edit"   -> handleEdit(player, args);
            default       -> player.sendMessage("Usage: /vehicle <spawn|remove|list|edit>");
        }

        return true;
    }

    private void handleSpawn(Player player, String[] args) {
        if (args.length < 2) { player.sendMessage("Usage: /vehicle spawn <id>"); return; }
        String id = args[1].toLowerCase();
        VehicleConfig config = vehicleConfigs.get(id);
        if (config == null) { player.sendMessage("Unknown vehicle: " + id); return; }
        VehicleCustomization customization = database.load(id);
        Vehicle vehicle = assembler.assemble(config, player.getLocation(), customization);
        registry.register(vehicle);
        player.sendMessage("Vehicle '" + id + "' spawned.");
    }

    private void handleRemove(Player player) {
        Vehicle vehicle = registry.getByPassenger(player);
        if (vehicle == null) vehicle = findNearest(player);
        if (vehicle == null) { player.sendMessage("No vehicle found nearby."); return; }
        registry.unregister(vehicle);
        player.sendMessage("Vehicle removed.");
    }

    private void handleList(Player player) {
        if (vehicleConfigs.isEmpty()) { player.sendMessage("No vehicles configured."); return; }
        player.sendMessage("Available vehicles: " + String.join(", ", vehicleConfigs.keySet()));
    }

    private void handleEdit(Player player, String[] args) {
        Vehicle vehicle = registry.getByPassenger(player);
        if (vehicle == null) vehicle = findNearest(player);
        if (vehicle == null) { player.sendMessage("No vehicle found nearby (within 10 blocks)."); return; }

        if (args.length >= 4 && args[2].equalsIgnoreCase("pivot")) {
            String pivotName = args[3];
            switch (args[1].toLowerCase()) {
                case "set"  -> handleSetPivot(player, vehicle, pivotName);
                case "show" -> handleShowPivot(player, vehicle, pivotName);
                case "hide" -> handleHidePivot(player, pivotName);
                default -> player.sendMessage("Usage: /vehicle edit <set|show|hide> pivot <name>");
            }
            return;
        }

        editorGui.open(player, vehicle);
    }

    private void handleSetPivot(Player player, Vehicle vehicle, String pivotName) {
        ItemDisplay model = PivotVisualizer.resolveModel(vehicle, pivotName);
        PartConfig cfgPart = PivotVisualizer.resolveCfgPart(vehicle, pivotName);
        if (model == null || cfgPart == null) {
            player.sendMessage("Unknown pivot name. Valid: " + String.join(", ", PIVOT_NAMES));
            return;
        }

        boolean isWheel = pivotName.toLowerCase().startsWith("wheel_");

        // World offset from display entity to player's feet
        Location entityLoc = model.getLocation();
        Location playerFeet = player.getLocation();
        double worldDx = playerFeet.getX() - entityLoc.getX();
        double worldDy = playerFeet.getY() - entityLoc.getY();
        double worldDz = playerFeet.getZ() - entityLoc.getZ();

        // Unrotate by vehicle yaw → model-space coordinates
        double rad = Math.toRadians(vehicle.getLocation().getYaw());
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        double modelX =  worldDx * cos + worldDz * sin;
        double modelY =  worldDy;
        double modelZ = -worldDx * sin + worldDz * cos;

        PartCustomization part = vehicle.getCustomization().getPart(pivotName.toLowerCase());

        if (isWheel) {
            part.setPivotDeltaY(modelY - cfgPart.pivot().y);
            part.setPivotDeltaZ(modelZ - cfgPart.pivot().z);
            player.sendMessage(Component.text(
                    String.format("[%s] pivot Y=%.4f Z=%.4f (model space)", pivotName, modelY, modelZ),
                    NamedTextColor.GREEN));
        } else {
            part.setPivotDeltaX(modelX - cfgPart.pivot().x);
            part.setPivotDeltaY(modelY - cfgPart.pivot().y);
            part.setPivotDeltaZ(modelZ - cfgPart.pivot().z);
            player.sendMessage(Component.text(
                    String.format("[%s] pivot X=%.4f Y=%.4f Z=%.4f (model space)", pivotName, modelX, modelY, modelZ),
                    NamedTextColor.GREEN));
        }

        database.save(vehicle.getCustomization());
    }

    private void handleShowPivot(Player player, Vehicle vehicle, String pivotName) {
        PartConfig cfgPart = PivotVisualizer.resolveCfgPart(vehicle, pivotName);
        if (cfgPart == null) {
            player.sendMessage("Unknown pivot name. Valid: " + String.join(", ", PIVOT_NAMES));
            return;
        }

        boolean isWheel = pivotName.toLowerCase().startsWith("wheel_");
        PartCustomization part = vehicle.getCustomization().getPart(pivotName.toLowerCase());

        double finalX = cfgPart.pivot().x + part.getPivotDeltaX();
        double finalY = cfgPart.pivot().y + part.getPivotDeltaY();
        double finalZ = cfgPart.pivot().z + part.getPivotDeltaZ();

        player.sendMessage(Component.text("--- " + pivotName + " pivot ---", NamedTextColor.GOLD));
        if (isWheel) {
            player.sendMessage(Component.text(
                    String.format("  X: %.4f (config, fixed)", cfgPart.pivot().x), NamedTextColor.GRAY));
            player.sendMessage(Component.text(
                    String.format("  Y: %.4f (config %.4f + delta %.4f)", finalY, cfgPart.pivot().y, part.getPivotDeltaY()), NamedTextColor.AQUA));
            player.sendMessage(Component.text(
                    String.format("  Z: %.4f (config %.4f + delta %.4f)", finalZ, cfgPart.pivot().z, part.getPivotDeltaZ()), NamedTextColor.AQUA));
        } else {
            player.sendMessage(Component.text(
                    String.format("  X: %.4f (config %.4f + delta %.4f)", finalX, cfgPart.pivot().x, part.getPivotDeltaX()), NamedTextColor.AQUA));
            player.sendMessage(Component.text(
                    String.format("  Y: %.4f (config %.4f + delta %.4f)", finalY, cfgPart.pivot().y, part.getPivotDeltaY()), NamedTextColor.AQUA));
            player.sendMessage(Component.text(
                    String.format("  Z: %.4f (config %.4f + delta %.4f)", finalZ, cfgPart.pivot().z, part.getPivotDeltaZ()), NamedTextColor.AQUA));
        }

        pivotVisualizer.show(player, vehicle, pivotName.toLowerCase());
        player.sendMessage(Component.text(
                "Particles shown. Use /vehicle edit hide pivot " + pivotName + " to stop.", NamedTextColor.GRAY));
    }

    private void handleHidePivot(Player player, String pivotName) {
        if (!PIVOT_NAMES.contains(pivotName.toLowerCase())) {
            player.sendMessage("Unknown pivot name. Valid: " + String.join(", ", PIVOT_NAMES));
            return;
        }
        boolean stopped = pivotVisualizer.hide(player, pivotName.toLowerCase());
        player.sendMessage(stopped
                ? Component.text("Pivot particles hidden.", NamedTextColor.YELLOW)
                : Component.text("No active visualization for: " + pivotName, NamedTextColor.RED));
    }

    private Vehicle findNearest(Player player) {
        Vehicle nearest = null;
        double nearestDist = 100.0;
        for (Vehicle vehicle : registry.getVehicles()) {
            double dist = vehicle.getLocation().distanceSquared(player.getLocation());
            if (dist < nearestDist) { nearestDist = dist; nearest = vehicle; }
        }
        return nearest;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) return List.of("spawn", "remove", "list", "edit");
        if (args.length == 2 && args[0].equalsIgnoreCase("spawn")) return new ArrayList<>(vehicleConfigs.keySet());
        if (args.length == 2 && args[0].equalsIgnoreCase("edit")) return List.of("set", "show", "hide");
        if (args.length == 3 && args[0].equalsIgnoreCase("edit")
                && List.of("set", "show", "hide").contains(args[1].toLowerCase())) return List.of("pivot");
        if (args.length == 4 && args[0].equalsIgnoreCase("edit") && args[2].equalsIgnoreCase("pivot")) return PIVOT_NAMES;
        return List.of();
    }
}
