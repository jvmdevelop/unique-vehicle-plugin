package com.jvmd.uniqueVehiclePlugin.manager;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;

import java.util.List;

public abstract class Manager {

    protected final List<Vehicle> vehicles;

    protected Manager(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    // manage every tick
    public abstract void manage();

}
