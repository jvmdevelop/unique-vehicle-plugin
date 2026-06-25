package com.jvmd.uniqueVehiclePlugin.customization;

public class PartCustomization {

    private double deltaX;
    private double deltaY;
    private double deltaZ;
    private float scale = 1.0f;
    private double rotationYaw;

    public PartCustomization() {}

    public PartCustomization(double deltaX, double deltaY, double deltaZ, float scale,
                             double rotationYaw) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.deltaZ = deltaZ;
        this.scale = scale;
        this.rotationYaw = rotationYaw;
    }

    public PartCustomization copy() {
        return new PartCustomization(deltaX, deltaY, deltaZ, scale, rotationYaw);
    }

    public double getDeltaX() { return deltaX; }
    public double getDeltaY() { return deltaY; }
    public double getDeltaZ() { return deltaZ; }
    public float getScale() { return scale; }
    public double getRotationYaw() { return rotationYaw; }

    public void setDeltaX(double deltaX) { this.deltaX = deltaX; }
    public void setDeltaY(double deltaY) { this.deltaY = deltaY; }
    public void setDeltaZ(double deltaZ) { this.deltaZ = deltaZ; }
    public void setScale(float scale) { this.scale = scale; }
    public void setRotationYaw(double rotationYaw) { this.rotationYaw = rotationYaw; }
}
