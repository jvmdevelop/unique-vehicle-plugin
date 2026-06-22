package com.jvmd.uniqueVehiclePlugin.config;

import com.jvmd.uniqueVehiclePlugin.entity.DoorType;
import com.jvmd.uniqueVehiclePlugin.entity.WheelType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleConfigParser {

    public Map<String, VehicleConfig> parseAll(FileConfiguration config) {
        Map<String, VehicleConfig> vehicles = new HashMap<>();

        ConfigurationSection vehiclesSection = config.getConfigurationSection("vehicles");
        if (vehiclesSection == null) return vehicles;

        for (String id : vehiclesSection.getKeys(false)) {
            ConfigurationSection section = vehiclesSection.getConfigurationSection(id);
            if (section == null) continue;

            vehicles.put(id, parseVehicle(id, section));
        }

        return vehicles;
    }

    private VehicleConfig parseVehicle(String id, ConfigurationSection section) {
        PartConfig frame = parsePart(section.getConfigurationSection("frame"));
        PartConfig steeringWheel = parsePart(section.getConfigurationSection("steering-wheel"));

        List<VehicleConfig.DoorPartConfig> doors = new ArrayList<>();
        List<?> doorsList = section.getMapList("doors");
        for (Object entry : doorsList) {
            if (entry instanceof Map<?, ?> map) {
                DoorType doorType = DoorType.valueOf(((String) map.get("type")).toUpperCase());
                PartConfig part = parsePartFromMap(map);
                doors.add(new VehicleConfig.DoorPartConfig(doorType, part));
            }
        }

        List<VehicleConfig.WheelPartConfig> wheels = new ArrayList<>();
        List<?> wheelsList = section.getMapList("wheels");
        for (Object entry : wheelsList) {
            if (entry instanceof Map<?, ?> map) {
                WheelType wheelType = WheelType.valueOf(((String) map.get("type")).toUpperCase());
                PartConfig part = parsePartFromMap(map);
                wheels.add(new VehicleConfig.WheelPartConfig(wheelType, part));
            }
        }

        Vector3d seatOffset = parseOffset(section.getConfigurationSection("seat.offset"));

        return new VehicleConfig(id, frame, steeringWheel, doors, wheels, seatOffset);
    }

    private PartConfig parsePart(ConfigurationSection section) {
        if (section == null) {
            return new PartConfig(Material.BARRIER, 0, new Vector3d(0, 0, 0));
        }

        Material material = Material.valueOf(section.getString("material", "BARRIER").toUpperCase());
        int customModelData = section.getInt("custom-model-data", 0);
        Vector3d offset = parseOffset(section.getConfigurationSection("offset"));

        return new PartConfig(material, customModelData, offset);
    }

    @SuppressWarnings("unchecked")
    private PartConfig parsePartFromMap(Map<?, ?> map) {
        Object materialObj = map.get("material");
        Material material = Material.valueOf(materialObj != null ? materialObj.toString().toUpperCase() : "BARRIER");
        int customModelData = map.containsKey("custom-model-data") ? ((Number) map.get("custom-model-data")).intValue() : 0;

        Vector3d offset = new Vector3d(0, 0, 0);
        Object offsetObj = map.get("offset");
        if (offsetObj instanceof Map<?, ?> offsetMap) {
            offset = new Vector3d(
                    toDouble(offsetMap.get("x")),
                    toDouble(offsetMap.get("y")),
                    toDouble(offsetMap.get("z"))
            );
        }

        return new PartConfig(material, customModelData, offset);
    }

    private Vector3d parseOffset(ConfigurationSection section) {
        if (section == null) return new Vector3d(0, 0, 0);

        return new Vector3d(
                section.getDouble("x", 0),
                section.getDouble("y", 0),
                section.getDouble("z", 0)
        );
    }

    private double toDouble(Object obj) {
        if (obj instanceof Number num) return num.doubleValue();
        return 0;
    }
}
