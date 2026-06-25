package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.customization.PartCustomization;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.entity.VehicleFrame;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import com.jvmd.uniqueVehiclePlugin.util.VehicleTransformUtil;
import org.bukkit.Location;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;

import java.util.List;

public class VehicleFrameProcessor extends Processor {

    public VehicleFrameProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    protected void processVehicle(Vehicle vehicle, boolean log) {
        VehicleFrame frame = vehicle.getFrame();
        PartCustomization c = vehicle.getCustomization().getPart("frame");

        Location loc = VehicleTransformUtil.computePartLocation(vehicle.getLocation(),
                frame.getOffset().x + c.getDeltaX(),
                frame.getOffset().y + c.getDeltaY(),
                frame.getOffset().z + c.getDeltaZ());
        loc.setYaw(vehicle.getLocation().getYaw());

        frame.getItemDisplay().teleport(loc);

        // Apply base rotation (rotationYaw) to ItemDisplay transformation
        Quaternionf baseRot = new Quaternionf().rotateY((float) Math.toRadians(c.getRotationYaw()));
        Transformation t = frame.getItemDisplay().getTransformation();
        frame.getItemDisplay().setTransformation(new Transformation(
                t.getTranslation(), baseRot, t.getScale(), t.getRightRotation()));

        if (log) {
            LOGGER.info(String.format("[Parts] frame -> x=%.3f y=%.3f z=%.3f yaw=%.1f rot=%.1f",
                    loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), c.getRotationYaw()));
        }
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        processVehicle(vehicle, false);
    }
}
