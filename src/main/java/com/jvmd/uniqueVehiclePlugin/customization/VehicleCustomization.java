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

    public void setPart(String key, PartCustomization part) {
        parts.put(key, part);
    }

    public void resetPart(String key) {
        parts.put(key, new PartCustomization());
    }

    public Map<String, PartCustomization> getAllParts() {
        return parts;
    }

    public VehicleCustomization copy() {
        VehicleCustomization copy = new VehicleCustomization(vehicleId);
        copy.globalScale = globalScale;
        for (Map.Entry<String, PartCustomization> entry : parts.entrySet()) {
            copy.parts.put(entry.getKey(), entry.getValue().copy());
        }
        return copy;
    }
}
