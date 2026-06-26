package com.jvmd.uniqueVehiclePlugin.manager.impl;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.manager.Manager;

import java.util.List;

public abstract class VehicleManager extends Manager {

    protected final List<Vehicle> vehicles;

    protected VehicleManager(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    public abstract void manage();
}
