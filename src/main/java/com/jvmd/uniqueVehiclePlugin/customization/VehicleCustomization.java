package com.jvmd.uniqueVehiclePlugin.customization;

import java.util.HashMap;
import java.util.Map;

public class VehicleCustomization {

    private final String vehicleId;
    private final Map<String, PartCustomization> parts = new HashMap<>();
    private float globalScale = 1.0f;

    public VehicleCustomization(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleId() { return vehicleId; }

    public float getGlobalScale() { return globalScale; }

    public void setGlobalScale(float globalScale) { this.globalScale = globalScale; }

    public PartCustomization getPart(String key) {
        return parts.computeIfAbsent(key, k -> new PartCustomization());
    }

    public Map<String, PartCustomization> getAllParts() {
        return parts;
    }

}
