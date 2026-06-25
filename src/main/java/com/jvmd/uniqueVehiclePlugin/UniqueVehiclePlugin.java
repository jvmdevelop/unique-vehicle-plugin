package com.jvmd.uniqueVehiclePlugin;

import com.jvmd.uniqueVehiclePlugin.assembler.VehicleAssembler;
import com.jvmd.uniqueVehiclePlugin.command.VehicleCommand;
import com.jvmd.uniqueVehiclePlugin.config.VehicleConfig;
import com.jvmd.uniqueVehiclePlugin.config.VehicleConfigParser;
import com.jvmd.uniqueVehiclePlugin.customization.VehicleDatabase;
import com.jvmd.uniqueVehiclePlugin.listener.VehicleListener;
import com.jvmd.uniqueVehiclePlugin.protocol.VehicleSeatPacketController;
import com.jvmd.uniqueVehiclePlugin.resourcepack.ResourcePackListener;
import com.jvmd.uniqueVehiclePlugin.resourcepack.ResourcePackServer;
import com.jvmd.uniqueVehiclePlugin.manager.impl.VehicleBodyManager;
import com.jvmd.uniqueVehiclePlugin.manager.impl.VehicleMovingManager;
import com.jvmd.uniqueVehiclePlugin.processor.impl.*;
import com.jvmd.uniqueVehiclePlugin.registry.VehicleRegistry;
import com.jvmd.uniqueVehiclePlugin.task.VehicleTickTask;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public final class UniqueVehiclePlugin extends JavaPlugin {

    private VehicleRegistry registry;
    private VehicleDatabase database;
    private ResourcePackServer resourcePackServer;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        VehicleConfigParser parser = new VehicleConfigParser();
        Map<String, VehicleConfig> vehicleConfigs = parser.parseAll(getConfig());

        database = new VehicleDatabase();
        try {
            database.init(this);
        } catch (SQLException e) {
            getLogger().severe("Failed to initialize SQLite database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

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
        VehicleDoorsProcessor doorsProcessor = new VehicleDoorsProcessor(vehicles);
        VehicleWheelsProcessor wheelsProcessor = new VehicleWheelsProcessor(vehicles);
        VehicleSteeringWheelProcessor steeringWheelProcessor = new VehicleSteeringWheelProcessor(vehicles);
        VehicleSeatPacketController seatPacketController = new VehicleSeatPacketController();

        VehicleMovingManager movingManager = new VehicleMovingManager(
                drivingProcessor, driftProcessor, brakeProcessor, physicsProcessor,
                soundProcessor, speedometerProcessor, vehicles
        );
        VehicleBodyManager bodyManager = new VehicleBodyManager(frameProcessor, doorsProcessor, wheelsProcessor, steeringWheelProcessor, vehicles);

        VehicleListener listener = new VehicleListener(registry, physicsProcessor, seatPacketController);
        registry.setOnRemoveCallback(listener::onVehicleRemoved);

        VehicleTickTask tickTask = new VehicleTickTask(movingManager, bodyManager, registry, seatPacketController);
        tickTask.runTaskTimer(this, 0L, 1L);

        VehicleCommand command = new VehicleCommand(vehicleConfigs, assembler, registry, database);
        getCommand("vehicle").setExecutor(command);
        getCommand("vehicle").setTabCompleter(command);

        getServer().getPluginManager().registerEvents(listener, this);

        // Start resource pack HTTP server
        File zipFile = new File(getServer().getWorldContainer(), "unique-vehicle-resourcepack.zip");
        if (zipFile.exists()) {
            int port = getConfig().getInt("resource-pack-port", 8181);
            try {
                resourcePackServer = new ResourcePackServer(this, zipFile, port);
                getServer().getPluginManager().registerEvents(
                        new ResourcePackListener(resourcePackServer.getUrl(), resourcePackServer.getSha1()), this);
            } catch (IOException e) {
                getLogger().warning("Could not start resource pack server: " + e.getMessage());
            }
        } else {
            getLogger().warning("Resource pack not found at: " + zipFile.getPath());
        }

        getLogger().info("UniqueVehiclePlugin enabled. Loaded " + vehicleConfigs.size() + " vehicle config(s).");
    }

    @Override
    public void onDisable() {
        if (registry != null) {
            registry.unregisterAll();
        }
        if (database != null) {
            database.close();
        }
        if (resourcePackServer != null) {
            resourcePackServer.stop();
        }
        getLogger().info("UniqueVehiclePlugin disabled. All vehicles removed.");
    }
}
