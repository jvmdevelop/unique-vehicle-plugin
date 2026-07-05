package com.jvmd.uniqueVehiclePlugin.entity;


import com.jvmd.uniqueVehiclePlugin.config.PhysicsConfig;
import com.jvmd.uniqueVehiclePlugin.config.VehicleConfig;
import com.jvmd.uniqueVehiclePlugin.util.VehicleTransformUtil;
import io.papermc.paper.entity.TeleportFlag;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.UUID;

public class Vehicle {

    private final VehicleConfig config;
    private final VehicleFrame frame;
    private final ItemDisplay steeringWheel;
    private final Wheel[] wheels;
    private final Entity seat;
    private final Entity[] passengerSeat;
    private final HashMap<UUID, Pair<Player, Integer>> passenger;
    private int availableSeatsCount;
    private final boolean[] availableSeats;
    private final Interaction hitbox;
    private Location location;
    private double speed;
    private boolean drifting;
    private UUID driverId;
    private float steeringAngle;

    public Vehicle(VehicleConfig config, VehicleFrame frame, ItemDisplay steeringWheel, Wheel[] wheels, Entity seat, Entity[] passengerSeat, Interaction hitbox, Location location, int availableSeatsCount, boolean[] avaliableSeats) {
        this.config = config;
        this.frame = frame;
        this.steeringWheel = steeringWheel;
        this.wheels = wheels;
        this.seat = seat;
        this.passengerSeat = passengerSeat;
        this.availableSeatsCount = availableSeatsCount;
        this.passenger = new HashMap<>();
        this.hitbox = hitbox;
        this.location = location;
        this.availableSeats = avaliableSeats;
    }


    public Player getDriver() {
        if (driverId == null) return null;
        return Bukkit.getPlayer(driverId);
    }

    public boolean isDriver(Player player) {
        return player != null && driverId != null && driverId.equals(player.getUniqueId());
    }

    public void setDriver(Player player) {
        driverId = player == null ? null : player.getUniqueId();
    }

    public void clearDriver(Player player) {
        if (player == null) return;
        if (driverId != null && driverId.equals(player.getUniqueId())) {
            driverId = null;
        }
    }

    public boolean hasEntity(Entity entity) {
        UUID id = entity.getUniqueId();
        if (seat != null && seat.getUniqueId().equals(id)) return true;
        if (hitbox != null && hitbox.getUniqueId().equals(id)) return true;
        if (steeringWheel != null && steeringWheel.getUniqueId().equals(id)) return true;
        if (frame != null && frame.getItemDisplay().getUniqueId().equals(id)) return true;
        for (Wheel wheel : wheels) {
            if (wheel.getModel().getUniqueId().equals(id)) return true;
        }
        return false;
    }

    public void teleportSeat() {
        if (seat == null) return;

        Location seatLoc = computeSeatLocation();
        seat.teleport(seatLoc, TeleportFlag.EntityState.RETAIN_PASSENGERS);

        if (hitbox != null) {
            hitbox.teleport(location);
        }
    }

    private Location computeSeatLocation() {
        Vector3d seatOffset = config.seatOffset();

        Location seatLoc = VehicleTransformUtil.computePartLocation(location, seatOffset.x, seatOffset.y, seatOffset.z);
        seatLoc.setYaw(location.getYaw());
        return seatLoc;
    }

    public void despawn() {
        if (frame != null) frame.getItemDisplay().remove();
        if (steeringWheel != null) steeringWheel.remove();

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

    public float getSteeringAngle() {
        return steeringAngle;
    }

    public void setSteeringAngle(float steeringAngle) {
        this.steeringAngle = steeringAngle;
    }


    private int getAvailableSeatIndex() {
        if (availableSeatsCount>0) {
            for (int index = 0; index < availableSeats.length; index++) {
                if (availableSeats[index]) {
                    return index;
                }
            }
        }
        return -1;
    }

    public void addPassenger(Player player) {
        if (availableSeatsCount <= 0) return;
        int availableIndex = getAvailableSeatIndex();
        passenger.put(player.getUniqueId(), Pair.of(player, availableIndex));
        availableSeats[availableIndex] = true;
        availableSeatsCount--;
    }

    public Entity unmountPassenger(UUID playerUUID) {
        Pair<Player, Integer> playerWithAvailableCount = passenger.remove(playerUUID);
        if (playerWithAvailableCount == null) return null;
        Integer index = playerWithAvailableCount.right();
        availableSeats[index] = false;
        availableSeatsCount++;

        return passengerSeat[index];
    }

    public Entity[] getPassengerSeat() {
        return passengerSeat;
    }

    public HashMap<UUID, Pair<Player, Integer>> getPassenger() {
        return passenger;
    }
}
