package com.jvmd.uniqueVehiclePlugin.processor.impl;


import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.entity.VehicleFrame;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import com.jvmd.uniqueVehiclePlugin.util.VehicleTransformUtil;
import org.bukkit.Location;
import org.bukkit.util.Transformation;


import java.util.List;

public class VehicleFrameProcessor extends Processor {

    public VehicleFrameProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    protected void processVehicle(Vehicle vehicle, boolean log) {
        VehicleFrame frame = vehicle.getFrame();

        Location loc = VehicleTransformUtil.computePartLocation(vehicle.getLocation(),
                frame.getOffset().x ,
                frame.getOffset().y,
                frame.getOffset().z);
        loc.setYaw(vehicle.getLocation().getYaw());

        frame.getItemDisplay().teleport(loc);

        Transformation t = frame.getItemDisplay().getTransformation();
        frame.getItemDisplay().setTransformation(new Transformation(
                t.getTranslation(), t.getLeftRotation(), t.getScale(), t.getRightRotation()));

    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        processVehicle(vehicle, false);
    }
}
