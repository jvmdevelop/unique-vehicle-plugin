package com.jvmd.uniqueVehiclePlugin;

import com.jvmd.uniqueVehiclePlugin.assembler.VehicleAssembler;
import com.jvmd.uniqueVehiclePlugin.command.VehicleCommand;
import com.jvmd.uniqueVehiclePlugin.config.VehicleConfig;
import com.jvmd.uniqueVehiclePlugin.config.VehicleConfigParser;
import com.jvmd.uniqueVehiclePlugin.listener.VehicleListener;
import com.jvmd.uniqueVehiclePlugin.manager.impl.SeatPacketManager;
import com.jvmd.uniqueVehiclePlugin.manager.impl.VehicleBodyManager;
import com.jvmd.uniqueVehiclePlugin.manager.impl.VehicleMovingManager;
import com.jvmd.uniqueVehiclePlugin.processor.impl.*;
import com.jvmd.uniqueVehiclePlugin.registry.VehicleRegistry;
import com.jvmd.uniqueVehiclePlugin.task.VehicleTickTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public final class UniqueVehiclePlugin extends JavaPlugin {

    private VehicleRegistry registry;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        VehicleConfigParser parser = new VehicleConfigParser();
        Map<String, VehicleConfig> vehicleConfigs = parser.parseAll(getConfig());

        registry = new VehicleRegistry();
        VehicleAssembler assembler = new VehicleAssembler();

        List<com.jvmd.uniqueVehiclePlugin.entity.Vehicle> vehicles = registry.getVehicles();

        VehicleDrivingProcessor drivingProcessor = new VehicleDrivingProcessor(vehicles);
        VehicleBrakeProcessor brakeProcessor = new VehicleBrakeProcessor(vehicles);
        VehicleDriftProcessor driftProcessor = new VehicleDriftProcessor(vehicles);
        VehiclePhysicsProcessor physicsProcessor = new VehiclePhysicsProcessor(vehicles);
        VehicleSoundProcessor soundProcessor = new VehicleSoundProcessor(vehicles);
        VehicleSpeedometerProcessor speedometerProcessor = new VehicleSpeedometerProcessor(vehicles);

        VehicleFrameProcessor frameProcessor = new VehicleFrameProcessor(vehicles);
        VehicleWheelsProcessor wheelsProcessor = new VehicleWheelsProcessor(vehicles);
        VehicleSteeringWheelProcessor steeringWheelProcessor = new VehicleSteeringWheelProcessor(vehicles);
        SeatPacketManager seatPacketController = new SeatPacketManager();

        VehicleMovingManager movingManager = new VehicleMovingManager(
                drivingProcessor, driftProcessor, brakeProcessor, physicsProcessor,
                soundProcessor, speedometerProcessor, vehicles
        );
        VehicleBodyManager bodyManager = new VehicleBodyManager(frameProcessor, wheelsProcessor, steeringWheelProcessor, vehicles);

        VehicleListener listener = new VehicleListener(registry, physicsProcessor, seatPacketController);
        registry.setOnRemoveCallback(listener::onVehicleRemoved);

        VehicleTickTask tickTask = new VehicleTickTask(movingManager, bodyManager, registry, seatPacketController);
        tickTask.runTaskTimer(this, 0L, 1L);

        VehicleCommand command = new VehicleCommand(vehicleConfigs, assembler, registry);
        getCommand("vehicle").setExecutor(command);
        getCommand("vehicle").setTabCompleter(command);

        getServer().getPluginManager().registerEvents(listener, this);
        getLogger().info("UniqueVehiclePlugin enabled. Loaded " + vehicleConfigs.size() + " vehicle config(s).");
    }

    @Override
    public void onDisable() {
        if (registry != null) {
            registry.unregisterAll();
        }
        getLogger().info("UniqueVehiclePlugin disabled. All vehicles removed.");
    }
}
