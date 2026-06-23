package com.jvmd.uniqueVehiclePlugin.customization;

public class PartCustomization {

    private double deltaX;
    private double deltaY;
    private double deltaZ;
    private float scale = 1.0f;

    public PartCustomization() {}

    public PartCustomization(double deltaX, double deltaY, double deltaZ, float scale) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.deltaZ = deltaZ;
        this.scale = scale;
    }

    public PartCustomization copy() {
        return new PartCustomization(deltaX, deltaY, deltaZ, scale);
    }

    public double getDeltaX() { return deltaX; }
    public double getDeltaY() { return deltaY; }
    public double getDeltaZ() { return deltaZ; }
    public float getScale() { return scale; }

    public void setDeltaX(double deltaX) { this.deltaX = deltaX; }
    public void setDeltaY(double deltaY) { this.deltaY = deltaY; }
    public void setDeltaZ(double deltaZ) { this.deltaZ = deltaZ; }
    public void setScale(float scale) { this.scale = scale; }
}
