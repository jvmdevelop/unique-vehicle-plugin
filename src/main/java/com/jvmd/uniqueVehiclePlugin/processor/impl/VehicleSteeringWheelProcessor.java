package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import org.bukkit.Input;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;

import java.util.List;

public class VehicleSteeringWheelProcessor extends Processor {

    private static final float STEER_ANGLE = (float) Math.toRadians(30);

    public VehicleSteeringWheelProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        Location loc = computePartLocation(vehicle);
        loc.setYaw(vehicle.getLocation().getYaw());

        vehicle.getSteeringWheel().teleport(loc);

        float angle = 0;
        Player driver = vehicle.getDriver();
        if (driver != null) {
            Input input = driver.getCurrentInput();
            if (input.isLeft()) angle = STEER_ANGLE;
            if (input.isRight()) angle = -STEER_ANGLE;
        }

        Transformation t = vehicle.getSteeringWheel().getTransformation();
        vehicle.getSteeringWheel().setTransformation(new Transformation(
                t.getTranslation(),
                new Quaternionf().rotateZ(angle),
                t.getScale(),
                t.getRightRotation()
        ));
    }

    private Location computePartLocation(Vehicle vehicle) {
        // steeringWheel не имеет отдельного offset в текущей модели, позиционируется напрямую
        return vehicle.getLocation().clone();
    }
}
