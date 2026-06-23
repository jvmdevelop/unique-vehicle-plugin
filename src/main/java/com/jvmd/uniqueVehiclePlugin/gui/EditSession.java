package com.jvmd.uniqueVehiclePlugin.gui;

import com.jvmd.uniqueVehiclePlugin.customization.PartCustomization;
import com.jvmd.uniqueVehiclePlugin.customization.VehicleCustomization;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;

import java.util.Map;

public class EditSession {

    private final Vehicle vehicle;
    private final VehicleCustomization originalCustomization;
    private int selectedPartIndex = 0;

    public EditSession(Vehicle vehicle) {
        this.vehicle = vehicle;
        this.originalCustomization = vehicle.getCustomization().copy();
    }

    public Vehicle getVehicle() { return vehicle; }

    public VehicleCustomization getOriginalCustomization() { return originalCustomization; }

    public int getSelectedPartIndex() { return selectedPartIndex; }

    public void setSelectedPartIndex(int selectedPartIndex) { this.selectedPartIndex = selectedPartIndex; }

    public void revert() {
        VehicleCustomization current = vehicle.getCustomization();
        current.setGlobalScale(originalCustomization.getGlobalScale());
        // Restore all parts from original
        for (Map.Entry<String, PartCustomization> entry : originalCustomization.getAllParts().entrySet()) {
            current.setPart(entry.getKey(), entry.getValue().copy());
        }
        // Remove parts that weren't in original
        current.getAllParts().keySet().retainAll(originalCustomization.getAllParts().keySet());
        vehicle.applyGlobalScale(originalCustomization.getGlobalScale());
    }
}
