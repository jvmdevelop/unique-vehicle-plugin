package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.customization.PartCustomization;
import com.jvmd.uniqueVehiclePlugin.entity.DoorType;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.entity.VehicleDoor;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import com.jvmd.uniqueVehiclePlugin.util.VehicleTransformUtil;
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

            Location loc = VehicleTransformUtil.computePartLocation(vehicle.getLocation(),
                    door.getOffset().x + c.getDeltaX(),
                    door.getOffset().y + c.getDeltaY(),
                    door.getOffset().z + c.getDeltaZ());
            loc.setYaw(vehicle.getLocation().getYaw());

            door.getDoorModel().teleport(loc);

            // Base rotation from rotationYaw, then animation on top
            Quaternionf baseRot = new Quaternionf().rotateY((float) Math.toRadians(c.getRotationYaw()));
            Quaternionf animRot = new Quaternionf();
            if (door.isOpen()) {
                float angle = door.getDoorType() == DoorType.LEFT ? DOOR_OPEN_ANGLE : -DOOR_OPEN_ANGLE;
                animRot.rotateY(angle);
            }
            Quaternionf rotation = animRot.mul(baseRot);

            Transformation t = door.getDoorModel().getTransformation();
            Vector3f scale = new Vector3f(Math.abs(t.getScale().x), Math.abs(t.getScale().y), Math.abs(t.getScale().z));

            float pivotX = (float) (pivotCfg.x + c.getPivotDeltaX());
            float pivotY = (float) (pivotCfg.y + c.getPivotDeltaY());
            float pivotZ = (float) (pivotCfg.z + c.getPivotDeltaZ());
            Vector3f pivot = new Vector3f(pivotX, pivotY, pivotZ);
            Vector3f translation = VehicleTransformUtil.computePivotTranslation(pivot, rotation, scale);

            door.getDoorModel().setTransformation(new Transformation(
                    translation, rotation, scale, t.getRightRotation()
            ));
        }
    }
}
