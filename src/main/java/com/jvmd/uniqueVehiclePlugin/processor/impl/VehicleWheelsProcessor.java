package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.config.PartConfig;
import com.jvmd.uniqueVehiclePlugin.customization.PartCustomization;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.entity.Wheel;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import com.jvmd.uniqueVehiclePlugin.util.VehicleTransformUtil;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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
        Player driver = vehicle.getDriver();
        if (driver != null) {
            Input input = driver.getCurrentInput();
            if (input.isLeft()) steerAngle = STEER_ANGLE;
            if (input.isRight()) steerAngle = -STEER_ANGLE;
        }

        Wheel[] wheels = vehicle.getWheels();
        for (int i = 0; i < wheels.length; i++) {
            Wheel wheel = wheels[i];
            PartCustomization c = vehicle.getCustomization().getPart("wheel_" + i);
            PartConfig cfg = vehicle.getConfig().wheels().get(i).part();

            Location loc = VehicleTransformUtil.computePartLocation(vehicle.getLocation(),
                    wheel.getOffset().x + c.getDeltaX(),
                    wheel.getOffset().y + c.getDeltaY(),
                    wheel.getOffset().z + c.getDeltaZ());
            loc.setYaw(vehicle.getLocation().getYaw());

            wheel.getModel().teleport(loc);

            float spin = wheel.getSpinAngle() + (float) vehicle.getSpeed() * SPIN_RATE;
            spin = spin % ((float) Math.PI * 2);
            wheel.setSpinAngle(spin);

            Quaternionf baseRot = new Quaternionf().rotateY((float) Math.toRadians(c.getRotationYaw()));
            Quaternionf spinQ = new Quaternionf().rotateX(spin);
            Quaternionf steerQ = new Quaternionf();
            if (wheel.isFront() && steerAngle != 0) {
                steerQ.rotateY(steerAngle);
            }
            // Apply wheel-local motion before vehicle yaw so the axle stays fixed.
            Quaternionf rotation = new Quaternionf(baseRot).mul(steerQ).mul(spinQ);

            Transformation t = wheel.getModel().getTransformation();
            Vector3f scale = new Vector3f(Math.abs(t.getScale().x), Math.abs(t.getScale().y), Math.abs(t.getScale().z));
            Vector3f translation = new Vector3f(0, 0, 0);

            wheel.getModel().setTransformation(new Transformation(
                    translation,
                    rotation,
                    scale,
                    t.getRightRotation()
            ));

            if (log) {
                Vector3f angles = rotation.getEulerAnglesXYZ(new Vector3f());
                LOGGER.info(String.format(
                        "[Wheels] wheel_%d (%s %s) | spin=%.3f steer=%.3f rot=%.1f | rotation(XYZ)=(%.3f, %.3f, %.3f) | translation=(%.4f,%.4f,%.4f)",
                        i,
                        wheel.isFront() ? "front" : "rear",
                        wheel.getWheelType(),
                        spin, steerAngle, c.getRotationYaw(),
                        angles.x, angles.y, angles.z,
                        translation.x, translation.y, translation.z
                ));
            }
        }
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        processVehicle(vehicle, false);
    }
}
