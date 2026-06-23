package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.customization.PartCustomization;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class VehicleSteeringWheelProcessor extends Processor {

    private static final float STEER_ANGLE = (float) Math.toRadians(30);

    public VehicleSteeringWheelProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        PartCustomization c = vehicle.getCustomization().getPart("steering_wheel");
        var offset = vehicle.getConfig().steeringWheel().offset();
        var pivotCfg = vehicle.getConfig().steeringWheel().pivot();

        Location loc = computePartLocation(vehicle,
                offset.x + c.getDeltaX(),
                offset.y + c.getDeltaY(),
                offset.z + c.getDeltaZ());
        loc.setYaw(vehicle.getLocation().getYaw());

        vehicle.getSteeringWheel().teleport(loc);

        float angle = 0;
        Player driver = vehicle.getDriver();
        if (driver != null) {
            Input input = driver.getCurrentInput();
            if (input.isLeft()) angle = STEER_ANGLE;
            if (input.isRight()) angle = -STEER_ANGLE;
        }

        Quaternionf rotation = new Quaternionf().rotateZ(angle);

        Vector3f pivot = new Vector3f((float) pivotCfg.x, (float) pivotCfg.y, (float) pivotCfg.z);
        Vector3f translation = new Vector3f(pivot).sub(rotation.transform(new Vector3f(pivot)));

        Transformation t = vehicle.getSteeringWheel().getTransformation();
        vehicle.getSteeringWheel().setTransformation(new Transformation(
                translation, rotation, t.getScale(), t.getRightRotation()
        ));
    }

    private Location computePartLocation(Vehicle vehicle, double ox, double oy, double oz) {
        Location base = vehicle.getLocation();
        double rad = Math.toRadians(base.getYaw());
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);
        return base.clone().add(ox * cos - oz * sin, oy, ox * sin + oz * cos);
    }
}
