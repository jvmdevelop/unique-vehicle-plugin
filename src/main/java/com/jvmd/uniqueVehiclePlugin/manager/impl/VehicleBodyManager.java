package com.jvmd.uniqueVehiclePlugin.manager.impl;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.manager.Manager;
import com.jvmd.uniqueVehiclePlugin.processor.impl.VehicleDoorsProcessor;
import com.jvmd.uniqueVehiclePlugin.processor.impl.VehicleFrameProcessor;
import com.jvmd.uniqueVehiclePlugin.processor.impl.VehicleSteeringWheelProcessor;
import com.jvmd.uniqueVehiclePlugin.processor.impl.VehicleWheelsProcessor;

import java.util.List;

public class VehicleBodyManager extends VehicleManager {

    private final VehicleFrameProcessor vehicleFrameProcessor;
    private final VehicleDoorsProcessor vehicleDoorsProcessor;
    private final VehicleWheelsProcessor vehicleWheelsProcessor;
    private final VehicleSteeringWheelProcessor vehicleSteeringWheelProcessor;


    public VehicleBodyManager(VehicleFrameProcessor vehicleFrameProcessor, VehicleDoorsProcessor vehicleDoorsProcessor, VehicleWheelsProcessor vehicleWheelsProcessor, VehicleSteeringWheelProcessor vehicleSteeringWheelProcessor, List<Vehicle> vehicles) {
        super(vehicles);
        this.vehicleFrameProcessor = vehicleFrameProcessor;
        this.vehicleDoorsProcessor = vehicleDoorsProcessor;
        this.vehicleWheelsProcessor = vehicleWheelsProcessor;
        this.vehicleSteeringWheelProcessor = vehicleSteeringWheelProcessor;
    }


    @Override
    public void manage() {
        vehicleDoorsProcessor.process();
        vehicleFrameProcessor.process();
        vehicleWheelsProcessor.process();
        vehicleSteeringWheelProcessor.process();
    }
}
