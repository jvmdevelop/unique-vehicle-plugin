package com.jvmd.uniqueVehiclePlugin.config;

import org.bukkit.Material;
import org.joml.Vector3d;

public record PartConfig(Material material, int customModelData, Vector3d offset, Vector3d pivot) {
    public PartConfig(Material material, int customModelData, Vector3d offset) {
        this(material, customModelData, offset, new Vector3d(0, 0, 0));
    }
}
