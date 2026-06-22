package com.jvmd.uniqueVehiclePlugin.entity;

import org.bukkit.entity.ItemDisplay;
import org.joml.Vector3d;

public class VehicleDoor {
    private final DoorType doorType;
    private final ItemDisplay doorModel;
    private final Vector3d offset;
    private boolean open;

    public VehicleDoor(DoorType doorType, ItemDisplay doorModel, Vector3d offset) {
        this.doorType = doorType;
        this.doorModel = doorModel;
        this.offset = offset;
    }

    public void toggleDoor() {
        this.open = !this.open;
    }

    public DoorType getDoorType() {
        return doorType;
    }

    public ItemDisplay getDoorModel() {
        return doorModel;
    }

    public Vector3d getOffset() {
        return offset;
    }

    public boolean isOpen() {
        return open;
    }
}
