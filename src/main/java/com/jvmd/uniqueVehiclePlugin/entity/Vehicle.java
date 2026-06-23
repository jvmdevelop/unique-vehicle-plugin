package com.jvmd.uniqueVehiclePlugin.entity;


import com.jvmd.uniqueVehiclePlugin.config.PhysicsConfig;
import com.jvmd.uniqueVehiclePlugin.config.VehicleConfig;
import com.jvmd.uniqueVehiclePlugin.customization.VehicleCustomization;
import io.papermc.paper.entity.TeleportFlag;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;

import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.UUID;

public class Vehicle {

    private final VehicleConfig config;
    private final VehicleFrame frame;
    private final ItemDisplay steeringWheel;
    private final VehicleDoor[] doors;
    private final Wheel[] wheels;
    private final Entity seat;
    private final Interaction hitbox;
    private final VehicleCustomization customization;
    private Location location;
    private double speed;
    private boolean drifting;

    public Vehicle(VehicleConfig config, VehicleFrame frame, ItemDisplay steeringWheel, VehicleDoor[] doors, Wheel[] wheels, Entity seat, Interaction hitbox, Location location, VehicleCustomization customization) {
        this.config = config;
        this.frame = frame;
        this.steeringWheel = steeringWheel;
        this.doors = doors;
        this.wheels = wheels;
        this.seat = seat;
        this.hitbox = hitbox;
        this.location = location;
        this.customization = customization;
    }

    public Player getDriver() {
        if (seat == null || seat.getPassengers().isEmpty()) return null;
        return seat.getPassengers().getFirst() instanceof Player p ? p : null;
    }

    public boolean hasEntity(Entity entity) {
        UUID id = entity.getUniqueId();
        if (seat != null && seat.getUniqueId().equals(id)) return true;
        if (hitbox != null && hitbox.getUniqueId().equals(id)) return true;
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
        if (seat == null) return;

        Vector3d seatOffset = config.seatOffset();
        var c = customization.getPart("seat");
        Location seatLoc = computePartLocation(
                seatOffset.x + c.getDeltaX(),
                seatOffset.y + c.getDeltaY(),
                seatOffset.z + c.getDeltaZ());
        seatLoc.setYaw(location.getYaw());

        seat.teleport(seatLoc, TeleportFlag.EntityState.RETAIN_PASSENGERS);

        if (hitbox != null) {
            hitbox.teleport(location);
        }
    }

    public void applyGlobalScale(float scale) {
        customization.setGlobalScale(scale);
        setDisplayScale(frame.getItemDisplay(), scale);
        setDisplayScale(steeringWheel, scale);
        for (VehicleDoor door : doors) setDisplayScale(door.getDoorModel(), scale);
        for (Wheel wheel : wheels) setDisplayScale(wheel.getModel(), scale);
    }

    private void setDisplayScale(ItemDisplay display, float scale) {
        Transformation t = display.getTransformation();
        display.setTransformation(new Transformation(
                t.getTranslation(), t.getLeftRotation(),
                new Vector3f(scale, scale, scale),
                t.getRightRotation()));
    }

    private Location computePartLocation(double ox, double oy, double oz) {
        double rad = Math.toRadians(location.getYaw());
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        return location.clone().add(ox * cos - oz * sin, oy, ox * sin + oz * cos);
    }

    public void toggleDoors() {
        for (VehicleDoor door : doors) {
            door.toggleDoor();
        }
    }

    public boolean hasOpenDoor() {
        for (VehicleDoor door : doors) {
            if (door.isOpen()) return true;
        }
        return false;
    }

    public void closeAllDoors() {
        for (VehicleDoor door : doors) {
            if (door.isOpen()) door.toggleDoor();
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
        if (hitbox != null) {
            hitbox.remove();
        }
    }

    public VehicleCustomization getCustomization() { return customization; }

    public PhysicsConfig getPhysics() {
        return config.physics();
    }

    public VehicleConfig getConfig() {
        return config;
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

    public Entity getSeat() {
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
