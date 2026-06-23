package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.config.PartConfig;
import com.jvmd.uniqueVehiclePlugin.customization.PartCustomization;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.entity.Wheel;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class VehicleWheelsProcessor extends Processor {

    private static final float SPIN_RATE = 0.5f;
    private static final float STEER_ANGLE = (float) Math.toRadians(25);

    public VehicleWheelsProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        float steerAngle = 0;
        Player driver = vehicle.getDriver();
        if (driver != null) {
            Input input = driver.getCurrentInput();
            if (input.isLeft()) steerAngle = -STEER_ANGLE;
            if (input.isRight()) steerAngle = STEER_ANGLE;
        }

        Wheel[] wheels = vehicle.getWheels();
        for (int i = 0; i < wheels.length; i++) {
            Wheel wheel = wheels[i];
            PartCustomization c = vehicle.getCustomization().getPart("wheel_" + i);
            PartConfig cfg = vehicle.getConfig().wheels().get(i).part();

            Location loc = computePartLocation(vehicle,
                    wheel.getOffset().x + c.getDeltaX(),
                    wheel.getOffset().y + c.getDeltaY(),
                    wheel.getOffset().z + c.getDeltaZ());
            loc.setYaw(vehicle.getLocation().getYaw());

            wheel.getModel().teleport(loc);

            float spin = wheel.getSpinAngle() + (float) vehicle.getSpeed() * SPIN_RATE;
            spin = spin % ((float) Math.PI * 2);
            wheel.setSpinAngle(spin);

            // Steer (Y axis) then spin (X axis = wheel axle)
            Quaternionf rotation = new Quaternionf();
            if (wheel.isFront() && steerAngle != 0) {
                rotation.rotateY(steerAngle);
            }
            rotation.rotateX(spin);

            Vector3f pivot = new Vector3f(
                    (float) cfg.pivot().x,
                    (float) cfg.pivot().y,
                    (float) cfg.pivot().z);
            Vector3f translation = new Vector3f(pivot).sub(rotation.transform(new Vector3f(pivot)));

            Transformation t = wheel.getModel().getTransformation();
            wheel.getModel().setTransformation(new Transformation(
                    translation,
                    rotation,
                    t.getScale(),
                    t.getRightRotation()
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
