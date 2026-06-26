package com.jvmd.uniqueVehiclePlugin.processor.impl;

import com.jvmd.uniqueVehiclePlugin.entity.Vehicle;
import com.jvmd.uniqueVehiclePlugin.processor.Processor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

import java.util.List;

public class VehicleSpeedometerProcessor extends Processor {

    private static final double BLOCKS_PER_TICK_TO_KMH = 72.0;
    private int tickCounter = 0;

    public VehicleSpeedometerProcessor(List<Vehicle> vehicles) {
        super(vehicles);
    }

    @Override
    public void process() {
        tickCounter++;
        if (tickCounter % 2 != 0) return;
        super.process();
    }

    @Override
    protected void processVehicle(Vehicle vehicle) {
        Player driver = vehicle.getDriver();
        if (driver == null) return;

        double speed = vehicle.getSpeed();
        double maxSpeed = vehicle.getPhysics().maxSpeed();
        int kmh = (int) Math.round(Math.abs(speed) * BLOCKS_PER_TICK_TO_KMH);

        double ratio = Math.abs(speed) / maxSpeed;
        TextColor color;
        if (ratio < 0.5) {
            color = NamedTextColor.GREEN;
        } else if (ratio < 0.8) {
            color = NamedTextColor.YELLOW;
        } else {
            color = NamedTextColor.RED;
        }

        String gear = speed < 0 ? "R" : "D";
        String drift = vehicle.isDrifting() ? " [DRIFT]" : "";

        Component speedometer = Component.text(gear + " ", NamedTextColor.GRAY)
                .append(Component.text(kmh + " km/h", color))
                .append(Component.text(drift, NamedTextColor.GOLD))
                .append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                .append(buildSpeedBar(ratio));

        driver.sendActionBar(speedometer);
    }

    private Component buildSpeedBar(double ratio) {
        int filled = (int) (ratio * 10);
        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            bar.append(i < filled ? '|' : '.');
        }
        TextColor barColor = ratio < 0.5 ? NamedTextColor.GREEN : ratio < 0.8 ? NamedTextColor.YELLOW : NamedTextColor.RED;
        return Component.text(bar.toString(), barColor);
    }
}
