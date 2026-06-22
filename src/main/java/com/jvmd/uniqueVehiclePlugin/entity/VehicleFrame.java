package com.jvmd.uniqueVehiclePlugin.entity;

import org.bukkit.entity.ItemDisplay;
import org.joml.Vector3d;

public class VehicleFrame {

    private final ItemDisplay itemDisplay;
    private final Vector3d offset;

    public VehicleFrame(ItemDisplay itemDisplay, Vector3d offset) {
        this.itemDisplay = itemDisplay;
        this.offset = offset;
    }

    public ItemDisplay getItemDisplay() {
        return itemDisplay;
    }

    public Vector3d getOffset() {
        return offset;
    }
}
