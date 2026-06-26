package com.jvmd.uniqueVehiclePlugin.manager.impl;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;

import com.jvmd.uniqueVehiclePlugin.processor.impl.*;

import java.util.List;

public class VehicleMovingManager extends VehicleManager {

    private final VehicleDrivingProcessor vehicleMovingProcessor;
    private final VehicleDriftProcessor vehicleDriftProcessor;
    private final VehicleBrakeProcessor vehicleBrakeProcessor;
    private final VehiclePhysicsProcessor vehiclePhysicsProcessor;
    private final VehicleSoundProcessor vehicleSoundProcessor;
    private final VehicleSpeedometerProcessor vehicleSpeedometerProcessor;

    public VehicleMovingManager(
            VehicleDrivingProcessor vehicleMovingProcessor,
            VehicleDriftProcessor vehicleDriftProcessor,
            VehicleBrakeProcessor vehicleBrakeProcessor,
            VehiclePhysicsProcessor vehiclePhysicsProcessor,
            VehicleSoundProcessor vehicleSoundProcessor,
            VehicleSpeedometerProcessor vehicleSpeedometerProcessor,
            List<Vehicle> vehicles
    ) {
        super(vehicles);
        this.vehicleMovingProcessor = vehicleMovingProcessor;
        this.vehicleDriftProcessor = vehicleDriftProcessor;
        this.vehicleBrakeProcessor = vehicleBrakeProcessor;
        this.vehiclePhysicsProcessor = vehiclePhysicsProcessor;
        this.vehicleSoundProcessor = vehicleSoundProcessor;
        this.vehicleSpeedometerProcessor = vehicleSpeedometerProcessor;
    }

    @Override
    public void manage() {
        vehicleMovingProcessor.process();
        vehicleBrakeProcessor.process();
        vehicleDriftProcessor.process();
        vehiclePhysicsProcessor.process();
        vehicleSoundProcessor.process();
        vehicleSpeedometerProcessor.process();
    }
}
