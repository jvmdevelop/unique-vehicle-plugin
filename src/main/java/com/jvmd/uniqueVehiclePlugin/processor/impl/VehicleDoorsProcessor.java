package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.entity.DoorType;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.entity.VehicleDoor;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import org.bukkit.Location;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import java.util.List;

public class VehicleDoorsProcessor extends Processor {

    private static final float DOOR_OPEN_ANGLE = (float) Math.toRadians(70);

    public VehicleDoorsProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        for (VehicleDoor door : vehicle.getDoors()) {
            Location loc = computePartLocation(vehicle, door.getOffset().x, door.getOffset().y, door.getOffset().z);
            loc.setYaw(vehicle.getLocation().getYaw());

            door.getDoorModel().teleport(loc);

            if (door.isOpen()) {
                float angle = door.getDoorType() == DoorType.LEFT ? DOOR_OPEN_ANGLE : -DOOR_OPEN_ANGLE;
                Transformation t = door.getDoorModel().getTransformation();
                door.getDoorModel().setTransformation(new Transformation(
                        t.getTranslation(),
                        new Quaternionf().rotateY(angle),
                        t.getScale(),
                        t.getRightRotation()
                ));
            } else {
                Transformation t = door.getDoorModel().getTransformation();
                door.getDoorModel().setTransformation(new Transformation(
                        t.getTranslation(),
                        new Quaternionf(),
                        t.getScale(),
                        t.getRightRotation()
                ));
            }
        }
    }

    private Location computePartLocation(Vehicle vehicle, double ox, double oy, double oz) {
        Location base = vehicle.getLocation();
        double rad = Math.toRadians(base.getYaw());
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        return base.clone().add(ox * cos - oz * sin, oy, ox * sin + oz * cos);
    }
}
