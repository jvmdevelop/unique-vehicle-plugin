package com.jvmd.uniqueVehiclePlugin.customization;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.*;
import java.util.Map;

public class VehicleDatabase {

    private Connection connection;

    public void init(Plugin plugin) throws SQLException {
        plugin.getDataFolder().mkdirs();
        File dbFile = new File(plugin.getDataFolder(), "vehicles.db");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS vehicle_customizations (
                    vehicle_id TEXT NOT NULL,
                    part_key TEXT NOT NULL,
                    delta_x REAL NOT NULL DEFAULT 0,
                    delta_y REAL NOT NULL DEFAULT 0,
                    delta_z REAL NOT NULL DEFAULT 0,
                    scale REAL NOT NULL DEFAULT 1,
                    PRIMARY KEY (vehicle_id, part_key)
                )
            """);
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS vehicle_global (
                    vehicle_id TEXT PRIMARY KEY,
                    global_scale REAL NOT NULL DEFAULT 1
                )
            """);
        }
    }

    public VehicleCustomization load(String vehicleId) {
        VehicleCustomization custom = new VehicleCustomization(vehicleId);

        try {
            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT global_scale FROM vehicle_global WHERE vehicle_id = ?")) {
                ps.setString(1, vehicleId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    custom.setGlobalScale(rs.getFloat("global_scale"));
                }
            }

            try (PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM vehicle_customizations WHERE vehicle_id = ?")) {
                ps.setString(1, vehicleId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String key = rs.getString("part_key");
                    PartCustomization part = custom.getPart(key);
                    part.setDeltaX(rs.getDouble("delta_x"));
                    part.setDeltaY(rs.getDouble("delta_y"));
                    part.setDeltaZ(rs.getDouble("delta_z"));
                    part.setScale(rs.getFloat("scale"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return custom;
    }

    public void save(VehicleCustomization custom) {
        try {
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT OR REPLACE INTO vehicle_global (vehicle_id, global_scale) VALUES (?, ?)")) {
                ps.setString(1, custom.getVehicleId());
                ps.setFloat(2, custom.getGlobalScale());
                ps.executeUpdate();
            }

            for (Map.Entry<String, PartCustomization> entry : custom.getAllParts().entrySet()) {
                PartCustomization part = entry.getValue();
                try (PreparedStatement ps = connection.prepareStatement("""
                    INSERT OR REPLACE INTO vehicle_customizations
                    (vehicle_id, part_key, delta_x, delta_y, delta_z, scale)
                    VALUES (?, ?, ?, ?, ?, ?)
                """)) {
                    ps.setString(1, custom.getVehicleId());
                    ps.setString(2, entry.getKey());
                    ps.setDouble(3, part.getDeltaX());
                    ps.setDouble(4, part.getDeltaY());
                    ps.setDouble(5, part.getDeltaZ());
                    ps.setFloat(6, part.getScale());
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
