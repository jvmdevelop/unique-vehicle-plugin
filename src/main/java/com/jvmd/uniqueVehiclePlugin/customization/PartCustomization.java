package com.jvmd.uniqueVehiclePlugin.customization;

public class PartCustomization {

    private double deltaX;
    private double deltaY;
    private double deltaZ;
    private float scale = 1.0f;
    private double pivotDeltaX;
    private double pivotDeltaY;
    private double pivotDeltaZ;
    private double rotationYaw;

    public PartCustomization() {}

    public PartCustomization(double deltaX, double deltaY, double deltaZ, float scale,
                             double pivotDeltaX, double pivotDeltaY, double pivotDeltaZ,
                             double rotationYaw) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.deltaZ = deltaZ;
        this.scale = scale;
        this.pivotDeltaX = pivotDeltaX;
        this.pivotDeltaY = pivotDeltaY;
        this.pivotDeltaZ = pivotDeltaZ;
        this.rotationYaw = rotationYaw;
    }

    public PartCustomization copy() {
        return new PartCustomization(deltaX, deltaY, deltaZ, scale,
                pivotDeltaX, pivotDeltaY, pivotDeltaZ, rotationYaw);
    }

    public double getDeltaX() { return deltaX; }
    public double getDeltaY() { return deltaY; }
    public double getDeltaZ() { return deltaZ; }
    public float getScale() { return scale; }
    public double getPivotDeltaX() { return pivotDeltaX; }
    public double getPivotDeltaY() { return pivotDeltaY; }
    public double getPivotDeltaZ() { return pivotDeltaZ; }
    public double getRotationYaw() { return rotationYaw; }

    public void setDeltaX(double deltaX) { this.deltaX = deltaX; }
    public void setDeltaY(double deltaY) { this.deltaY = deltaY; }
    public void setDeltaZ(double deltaZ) { this.deltaZ = deltaZ; }
    public void setScale(float scale) { this.scale = scale; }
    public void setPivotDeltaX(double pivotDeltaX) { this.pivotDeltaX = pivotDeltaX; }
    public void setPivotDeltaY(double pivotDeltaY) { this.pivotDeltaY = pivotDeltaY; }
    public void setPivotDeltaZ(double pivotDeltaZ) { this.pivotDeltaZ = pivotDeltaZ; }
    public void setRotationYaw(double rotationYaw) { this.rotationYaw = rotationYaw; }
}
