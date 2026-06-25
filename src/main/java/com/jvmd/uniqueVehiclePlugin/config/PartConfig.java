package com.jvmd.uniqueVehiclePlugin.config;

import org.bukkit.Material;
import org.joml.Vector3d;

public record PartConfig(Material material, int customModelData, Vector3d offset) {
}
