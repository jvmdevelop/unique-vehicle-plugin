package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.entity.VehicleFrame;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import org.bukkit.Location;

import java.util.List;

public class VehicleFrameProcessor extends Processor {

    public VehicleFrameProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        VehicleFrame frame = vehicle.getFrame();
        Location loc = computePartLocation(vehicle, frame.getOffset().x, frame.getOffset().y, frame.getOffset().z);
        loc.setYaw(vehicle.getLocation().getYaw());

        frame.getItemDisplay().teleport(loc);
    }

    private Location computePartLocation(Vehicle vehicle, double ox, double oy, double oz) {
        Location base = vehicle.getLocation();
        double rad = Math.toRadians(base.getYaw());
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        return base.clone().add(ox * cos - oz * sin, oy, ox * sin + oz * cos);
    }
}
