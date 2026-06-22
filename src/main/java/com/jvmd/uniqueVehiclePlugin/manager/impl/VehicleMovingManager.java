package com.jvmd.uniqueVehiclePlugin.manager.impl;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.manager.Manager;
import com.jvmd.uniqueVehiclePlugin.processor.impl.VehicleBrakeProcessor;
import com.jvmd.uniqueVehiclePlugin.processor.impl.VehicleDriftProcessor;
import com.jvmd.uniqueVehiclePlugin.processor.impl.VehicleDrivingProcessor;
import com.jvmd.uniqueVehiclePlugin.processor.impl.VehiclePhysicsProcessor;

import java.util.List;

public class VehicleMovingManager extends Manager {

    private final VehicleDrivingProcessor vehicleMovingProcessor;
    private final VehicleDriftProcessor vehicleDriftProcessor;
    private final VehicleBrakeProcessor vehicleBrakeProcessor;
    private final VehiclePhysicsProcessor vehiclePhysicsProcessor;


    public VehicleMovingManager(VehicleDrivingProcessor vehicleMovingProcessor, VehicleDriftProcessor vehicleDriftProcessor, VehicleBrakeProcessor vehicleBrakeProcessor, VehiclePhysicsProcessor vehiclePhysicsProcessor, List<Vehicle> vehicles) {
        super(vehicles);
        this.vehicleMovingProcessor = vehicleMovingProcessor;
        this.vehicleDriftProcessor = vehicleDriftProcessor;
        this.vehicleBrakeProcessor = vehicleBrakeProcessor;
        this.vehiclePhysicsProcessor = vehiclePhysicsProcessor;
    }

    @Override
    public void manage() {
        vehicleMovingProcessor.process();
        vehicleBrakeProcessor.process();
        vehicleDriftProcessor.process();
        vehiclePhysicsProcessor.process();
    }

}
