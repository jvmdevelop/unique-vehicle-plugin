package com.jvmd.uniqueVehiclePlugin.entity;


import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Vehicle {

    private final VehicleFrame frame;
    private final ItemDisplay steeringWheel;
    private final VehicleDoor[] doors;
    private final Wheel[] wheels;
    private final org.bukkit.entity.Vehicle seat;
    private Location location;
    private double speed;
    private boolean drifting;

    public Vehicle(VehicleFrame frame, ItemDisplay steeringWheel, VehicleDoor[] doors, Wheel[] wheels, org.bukkit.entity.Vehicle seat, Location location) {
        this.frame = frame;
        this.steeringWheel = steeringWheel;
        this.doors = doors;
        this.wheels = wheels;
        this.seat = seat;
        this.location = location;
    }

    public Player getDriver() {
        if (seat == null || seat.getPassengers().isEmpty()) return null;
        return seat.getPassengers().getFirst() instanceof Player p ? p : null;
    }

    public boolean hasEntity(Entity entity) {
        UUID id = entity.getUniqueId();
        if (seat != null && seat.getUniqueId().equals(id)) return true;
        if (steeringWheel != null && steeringWheel.getUniqueId().equals(id)) return true;
        if (frame != null && frame.getItemDisplay().getUniqueId().equals(id)) return true;
        for (VehicleDoor door : doors) {
            if (door.getDoorModel().getUniqueId().equals(id)) return true;
        }
        for (Wheel wheel : wheels) {
            if (wheel.getModel().getUniqueId().equals(id)) return true;
        }
        return false;
    }

    public void teleportSeat() {
        if (seat != null) {
            seat.teleport(location);
        }
    }

    public void despawn() {
        if (frame != null) frame.getItemDisplay().remove();
        if (steeringWheel != null) steeringWheel.remove();
        for (VehicleDoor door : doors) {
            door.getDoorModel().remove();
        }
        for (Wheel wheel : wheels) {
            wheel.getModel().remove();
        }
        if (seat != null) {
            seat.eject();
            seat.remove();
        }
    }

    public VehicleFrame getFrame() {
        return frame;
    }

    public ItemDisplay getSteeringWheel() {
        return steeringWheel;
    }

    public VehicleDoor[] getDoors() {
        return doors;
    }

    public Wheel[] getWheels() {
        return wheels;
    }

    public org.bukkit.entity.Vehicle getSeat() {
        return seat;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public boolean isDrifting() {
        return drifting;
    }

    public void setDrifting(boolean drifting) {
        this.drifting = drifting;
    }
}
