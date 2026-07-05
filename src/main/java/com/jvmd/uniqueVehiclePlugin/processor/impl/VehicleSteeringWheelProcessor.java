package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import com.jvmd.uniqueVehiclePlugin.util.VehicleTransformUtil;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class VehicleSteeringWheelProcessor extends Processor {

    private static final float MAX_STEER_ANGLE = (float) Math.toRadians(30);
    private static final float STEER_RATE = 0.08f;

    public VehicleSteeringWheelProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        processVehicle(vehicle, false);
    }

    @Override
    protected void processVehicle(Vehicle vehicle, boolean log) {
        var offset = vehicle.getConfig().steeringWheel().offset();

        Location loc = VehicleTransformUtil.computePartLocation(vehicle.getLocation(),
                offset.x ,
                offset.y ,
                offset.z);
        loc.setYaw(vehicle.getLocation().getYaw());

        vehicle.getSteeringWheel().teleport(loc);

        float target = 0;
        Player driver = vehicle.getDriver();
        if (driver != null) {
            Input input = driver.getCurrentInput();
            if (input.isLeft()) target = -MAX_STEER_ANGLE;
            if (input.isRight()) target = MAX_STEER_ANGLE;
        }

        float current = vehicle.getSteeringAngle();
        float diff = target - current;
        if (Math.abs(diff) < STEER_RATE) {
            current = target;
        } else {
            current += Math.signum(diff) * STEER_RATE;
        }
        vehicle.setSteeringAngle(current);

        Quaternionf rotation = new Quaternionf().rotateZ(current);

        Transformation t = vehicle.getSteeringWheel().getTransformation();
        Vector3f scale = new Vector3f(Math.abs(t.getScale().x), Math.abs(t.getScale().y), Math.abs(t.getScale().z));

        vehicle.getSteeringWheel().setTransformation(new Transformation(
                new Vector3f(0, 0, 0), rotation, scale, t.getRightRotation()
        ));
    }
}
