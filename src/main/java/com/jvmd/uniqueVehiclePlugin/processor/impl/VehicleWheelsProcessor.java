package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.entity.Wheel;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import org.bukkit.Location;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;

import java.util.List;

public class VehicleWheelsProcessor extends Processor {

    private static final float SPIN_RATE = 0.5f;

    public VehicleWheelsProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        for (Wheel wheel : vehicle.getWheels()) {
            Location loc = computePartLocation(vehicle, wheel.getOffset().x, wheel.getOffset().y, wheel.getOffset().z);
            loc.setYaw(vehicle.getLocation().getYaw());

            wheel.getModel().teleport(loc);

            float spin = wheel.getSpinAngle() + (float) vehicle.getSpeed() * SPIN_RATE;
            wheel.setSpinAngle(spin);

            Transformation t = wheel.getModel().getTransformation();
            wheel.getModel().setTransformation(new Transformation(
                    t.getTranslation(),
                    new Quaternionf().rotateX(spin),
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
