package com.jvmd.uniqueVehiclePlugin.manager.impl;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.processor.impl.VehicleFrameProcessor;
import com.jvmd.uniqueVehiclePlugin.processor.impl.VehicleSteeringWheelProcessor;
import com.jvmd.uniqueVehiclePlugin.processor.impl.VehicleWheelsProcessor;

import java.util.List;

public class VehicleBodyManager extends VehicleManager {

    private final VehicleFrameProcessor vehicleFrameProcessor;
    private final VehicleWheelsProcessor vehicleWheelsProcessor;
    private final VehicleSteeringWheelProcessor vehicleSteeringWheelProcessor;


    public VehicleBodyManager(VehicleFrameProcessor vehicleFrameProcessor, VehicleWheelsProcessor vehicleWheelsProcessor, VehicleSteeringWheelProcessor vehicleSteeringWheelProcessor, List<Vehicle> vehicles) {
        super(vehicles);
        this.vehicleFrameProcessor = vehicleFrameProcessor;
        this.vehicleWheelsProcessor = vehicleWheelsProcessor;
        this.vehicleSteeringWheelProcessor = vehicleSteeringWheelProcessor;
    }


    @Override
    public void manage() {
        vehicleFrameProcessor.process();
        vehicleWheelsProcessor.process();
        vehicleSteeringWheelProcessor.process();
    }
}
