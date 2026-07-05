package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.entity.Wheel;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import com.jvmd.uniqueVehiclePlugin.util.VehicleTransformUtil;
import org.bukkit.Location;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class VehicleWheelsProcessor extends Processor {

    private static final float SPIN_RATE = 2.27f;
    private static final float STEER_ANGLE = (float) Math.toRadians(25);

    public VehicleWheelsProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    protected void processVehicle(Vehicle vehicle, boolean log) {
        float steerAngle = 0;
        var driver = vehicle.getDriver();
        if (driver != null) {
            var input = driver.getCurrentInput();
            if (input.isLeft()) steerAngle = STEER_ANGLE;
            if (input.isRight()) steerAngle = -STEER_ANGLE;
        }

        var wheels = vehicle.getWheels();
        for (int i = 0; i < wheels.length; i++) {
            Wheel wheel = wheels[i];

            Location loc = VehicleTransformUtil.computePartLocation(vehicle.getLocation(),
                    wheel.getOffset().x ,
                    wheel.getOffset().y ,
                    wheel.getOffset().z);
            loc.setYaw(vehicle.getLocation().getYaw());

            wheel.getModel().teleport(loc);

            float spin = wheel.getSpinAngle() + (float) vehicle.getSpeed() * SPIN_RATE;
            spin = spin % ((float) Math.PI * 2);
            wheel.setSpinAngle(spin);

            Quaternionf spinQ = new Quaternionf().rotateX(spin);
            Quaternionf steerQ = new Quaternionf();
            if (wheel.isFront() && steerAngle != 0) {
                steerQ.rotateY(steerAngle);
            }

            Quaternionf rotation = new Quaternionf().mul(steerQ).mul(spinQ);

            Transformation t = wheel.getModel().getTransformation();
            Vector3f scale = new Vector3f(Math.abs(t.getScale().x), Math.abs(t.getScale().y), Math.abs(t.getScale().z));

            wheel.getModel().setTransformation(new Transformation(
                    new Vector3f(0, 0, 0),
                    rotation,
                    scale,
                    t.getRightRotation()
            ));
        }
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        processVehicle(vehicle, false);
    }
}
