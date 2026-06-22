package com.jvmd.uniqueVehiclePlugin.entity;

import org.bukkit.entity.ItemDisplay;
import org.joml.Vector3d;

public class Wheel {
    private final WheelType wheelType;
    private final ItemDisplay model;
    private final Vector3d offset;
    private float spinAngle;

    public Wheel(WheelType wheelType, ItemDisplay model, Vector3d offset) {
        this.wheelType = wheelType;
        this.model = model;
        this.offset = offset;
    }

    public WheelType getWheelType() {
        return wheelType;
    }

    public ItemDisplay getModel() {
        return model;
    }

    public Vector3d getOffset() {
        return offset;
    }

    public float getSpinAngle() {
        return spinAngle;
    }

    public void setSpinAngle(float spinAngle) {
        this.spinAngle = spinAngle;
    }
}
