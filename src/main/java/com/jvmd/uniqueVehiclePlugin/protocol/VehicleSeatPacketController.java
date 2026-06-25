package com.jvmd.uniqueVehiclePlugin.protocol;

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

public class VehicleSeatPacketController {

    private final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private final Map<java.util.UUID, SeatState> states = new HashMap<>();

    public void enter(Vehicle vehicle, Player player) {
        if (vehicle == null || player == null || vehicle.getSeat() == null) return;

        states.put(player.getUniqueId(), new SeatState(player.getWalkSpeed(), player.getFlySpeed()));

        lockMovement(player);
        mount(vehicle, player);
    }

    public void exit(Vehicle vehicle, Player player) {
        if (vehicle == null || player == null) return;

        unmount(vehicle, player);
        restore(player);
    }

    public void sync(Vehicle vehicle) {
        if (vehicle == null || vehicle.getSeat() == null) return;
        Player driver = vehicle.getDriver();
        if (driver == null) return;

        refreshSeatEntity(vehicle, driver);
        mount(vehicle, driver);
        sendMountPacket(vehicle, driver);
    }

    private void restore(Player player) {
        SeatState state = states.remove(player.getUniqueId());
        if (state == null) return;

        player.setWalkSpeed(state.walkSpeed());
        player.setFlySpeed(state.flySpeed());
    }

    private void mount(Vehicle vehicle, Player player) {
        Entity seat = vehicle.getSeat();
        if (!seat.getPassengers().contains(player)) {
            seat.addPassenger(player);
        }
        player.setFallDistance(0);
        player.setVelocity(player.getVelocity().zero());
    }

    private void lockMovement(Player player) {
        player.setWalkSpeed(0.0f);
        player.setFlySpeed(0.0f);
    }

    private void unmount(Vehicle vehicle, Player player) {
        Entity seat = vehicle.getSeat();
        if (seat.getPassengers().contains(player)) {
            seat.removePassenger(player);
        }
        sendMountPacket(vehicle, null);
    }

    private void refreshSeatEntity(Vehicle vehicle, Player driver) {
        Entity seat = vehicle.getSeat();
        List<Player> viewers = new ArrayList<>(seat.getTrackedBy());
        if (!viewers.contains(driver)) {
            viewers.add(driver);
        }

        if (!viewers.isEmpty()) {
            protocolManager.updateEntity(seat, viewers);
        }
    }

    private void sendMountPacket(Vehicle vehicle, Player driver) {
        Entity seat = vehicle.getSeat();
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.MOUNT);
        packet.getIntegers().write(0, seat.getEntityId());
        packet.getIntegerArrays().write(0, driver == null ? new int[0] : new int[] { driver.getEntityId() });

        List<Player> viewers = new ArrayList<>(seat.getTrackedBy());
        if (driver != null && !viewers.contains(driver)) {
            viewers.add(driver);
        }

        for (Player viewer : viewers) {
            protocolManager.sendServerPacket(viewer, packet.deepClone());
        }
    }

    private record SeatState(float walkSpeed, float flySpeed) {
    }
}
