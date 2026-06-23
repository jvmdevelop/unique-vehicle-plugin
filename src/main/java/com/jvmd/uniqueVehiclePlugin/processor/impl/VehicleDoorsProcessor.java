package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.customization.PartCustomization;
import com.jvmd.uniqueVehiclePlugin.entity.DoorType;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.entity.VehicleDoor;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import org.bukkit.Location;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class VehicleDoorsProcessor extends Processor {

    private static final float DOOR_OPEN_ANGLE = (float) Math.toRadians(70);

    public VehicleDoorsProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        VehicleDoor[] doors = vehicle.getDoors();
        for (int i = 0; i < doors.length; i++) {
            VehicleDoor door = doors[i];
            PartCustomization c = vehicle.getCustomization().getPart("door_" + i);
            var pivotCfg = vehicle.getConfig().doors().get(i).part().pivot();

            Location loc = computePartLocation(vehicle,
                    door.getOffset().x + c.getDeltaX(),
                    door.getOffset().y + c.getDeltaY(),
                    door.getOffset().z + c.getDeltaZ());
            loc.setYaw(vehicle.getLocation().getYaw());

            door.getDoorModel().teleport(loc);

            Quaternionf rotation = new Quaternionf();
            if (door.isOpen()) {
                float angle = door.getDoorType() == DoorType.LEFT ? DOOR_OPEN_ANGLE : -DOOR_OPEN_ANGLE;
                rotation.rotateY(angle);
            }

            Vector3f pivot = new Vector3f((float) pivotCfg.x, (float) pivotCfg.y, (float) pivotCfg.z);
            Vector3f translation = new Vector3f(pivot).sub(rotation.transform(new Vector3f(pivot)));

            Transformation t = door.getDoorModel().getTransformation();
            door.getDoorModel().setTransformation(new Transformation(
                    translation, rotation, t.getScale(), t.getRightRotation()
            ));
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
