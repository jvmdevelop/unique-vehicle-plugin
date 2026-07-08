package com.jvmd.uniqueVehiclePlugin.manager.impl;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatPacketManager {

    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private final Map<java.util.UUID, SeatState> states = new HashMap<>();

    public void enter(Vehicle vehicle, Player player) {
        if (vehicle == null || player == null || vehicle.getSeat() == null) return;
        if (!mount(vehicle, player)) return;

        states.put(player.getUniqueId(), new SeatState(player.getWalkSpeed(), player.getFlySpeed()));
        lockMovement(player);
    }

    public void exit(Vehicle vehicle, Player player) {
        if (vehicle == null || player == null) return;

        unmount(vehicle, player);
        restore(player);
    }

    public void sync(Vehicle vehicle) {
        if (vehicle == null || vehicle.getSeat() == null) return;
        Player driver = vehicle.getDriver();
        if (driver != null) {
            refreshSeatEntity(vehicle.getSeat(), driver);
            mount(vehicle, driver);
        }
        sendMountPacket(vehicle.getSeat());
        for (Entity passengerSeat : vehicle.getPassengerSeat()) {
            sendMountPacket(passengerSeat);
        }
    }

    private void restore(Player player) {
        SeatState state = states.remove(player.getUniqueId());
        if (state == null) return;

        player.setWalkSpeed(state.walkSpeed());
        player.setFlySpeed(state.flySpeed());
    }

    private boolean mount(Vehicle vehicle, Player player) {
        boolean mounted;
        if (vehicle.isDriver(player)) {
            mounted = vehicle.getSeat().getPassengers().contains(player) || vehicle.getSeat().addPassenger(player);
        } else {
            mounted = vehicle.addPassenger(player);
        }
        if (!mounted) return false;

        player.setFallDistance(0);
        player.setVelocity(player.getVelocity().zero());
        return true;
    }

    private void lockMovement(Player player) {
        player.setWalkSpeed(0.0f);
        player.setFlySpeed(0.0f);
    }

    private void unmount(Vehicle vehicle, Player player) {
        Entity seat = vehicle.getSeat();
        if (seat.getPassengers().contains(player)) {
            seat.removePassenger(player);
            sendMountPacket(seat);
            return;
        }

        Entity passengerSeat = vehicle.unmountPassenger(player.getUniqueId());
        if (passengerSeat != null) {
            sendMountPacket(passengerSeat);
        }
    }

    private void refreshSeatEntity(Entity seat, Player driver) {
        List<Player> viewers = new ArrayList<>(seat.getTrackedBy());
        if (!viewers.contains(driver)) {
            viewers.add(driver);
        }

        if (!viewers.isEmpty()) {
            protocolManager.updateEntity(seat, viewers);
        }
    }

    private void sendMountPacket(Entity seat) {
        if (seat == null) return;

        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.MOUNT);
        packet.getIntegers().write(0, seat.getEntityId());
        int[] passengerIds = seat.getPassengers().stream()
                .mapToInt(Entity::getEntityId)
                .toArray();
        packet.getIntegerArrays().write(0, passengerIds);

        List<Player> viewers = new ArrayList<>(seat.getTrackedBy());
        for (Entity passenger : seat.getPassengers()) {
            if (passenger instanceof Player player && !viewers.contains(player)) {
                viewers.add(player);
            }
        }

        for (Player viewer : viewers) {
            protocolManager.sendServerPacket(viewer, packet.deepClone());
        }
    }

    private record SeatState(float walkSpeed, float flySpeed) {
    }
}
