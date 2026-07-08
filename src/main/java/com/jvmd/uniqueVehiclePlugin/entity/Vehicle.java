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
import java.util.List;
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
    private final boolean[] occupiedPassengerSeats;
    private final Interaction hitbox;
    private Location location;
    private double speed;
    private boolean drifting;
    private UUID driverId;
    private float steeringAngle;

    public Vehicle(VehicleConfig config, VehicleFrame frame, ItemDisplay steeringWheel, Wheel[] wheels, Entity seat, Entity[] passengerSeat, Interaction hitbox, Location location, int availableSeatsCount, boolean[] occupiedPassengerSeats) {
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
        this.occupiedPassengerSeats = occupiedPassengerSeats;
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
        for (Entity passengerSeatEntity : passengerSeat) {
            if (passengerSeatEntity != null && passengerSeatEntity.getUniqueId().equals(id)) return true;
        }
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
        int index = 0;
        for (Location passengerSeatLocation : computePassengersSeatLocations()) {
            passengerSeat[index].teleport(passengerSeatLocation, TeleportFlag.EntityState.RETAIN_PASSENGERS);
            index++;
        }


        if (hitbox != null) {
            hitbox.teleport(location);
        }
    }

    private List<Location> computePassengersSeatLocations() {
        return config.passengerSeatsOffset().stream()
                .map(offset -> VehicleTransformUtil.computePartLocation(location, offset.x, offset.y, offset.z))
                .toList();
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
        for (Entity passengerSeatEntity : passengerSeat) {
            if (passengerSeatEntity != null) {
                passengerSeatEntity.eject();
                passengerSeatEntity.remove();
            }
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
        if (availableSeatsCount > 0) {
            for (int index = 0; index < occupiedPassengerSeats.length; index++) {
                if (!occupiedPassengerSeats[index]) {
                    return index;
                }
            }
        }
        return -1;
    }

    public boolean addPassenger(Player player) {
        if (player == null || isPassenger(player) || availableSeatsCount <= 0) return false;
        int availableIndex = getAvailableSeatIndex();
        if (availableIndex == -1) return false;
        passenger.put(player.getUniqueId(), Pair.of(player, availableIndex));
        if (!passengerSeat[availableIndex].addPassenger(player)) {
            passenger.remove(player.getUniqueId());
            return false;
        }
        occupiedPassengerSeats[availableIndex] = true;
        availableSeatsCount--;
        return true;
    }

    public Entity unmountPassenger(UUID playerUUID) {
        Pair<Player, Integer> playerWithAvailableCount = passenger.remove(playerUUID);
        if (playerWithAvailableCount == null) return null;
        Integer index = playerWithAvailableCount.right();
        Player player = Bukkit.getPlayer(playerUUID);
        if (player != null) {
            passengerSeat[index].removePassenger(player);
        }
        occupiedPassengerSeats[index] = false;
        availableSeatsCount++;

        return passengerSeat[index];
    }

    public Entity[] getPassengerSeat() {
        return passengerSeat;
    }

    public HashMap<UUID, Pair<Player, Integer>> getPassenger() {
        return passenger;
    }

    public boolean isPassenger(Player player) {
        return passenger.containsKey(player.getUniqueId());
    }
}
