package com.jvmd.uniqueVehiclePlugin.config;

import com.jvmd.uniqueVehiclePlugin.entity.WheelType;
import org.joml.Vector3d;

import java.util.List;

public record VehicleConfig(
        String id,
        PartConfig frame,
        PartConfig steeringWheel,
        List<WheelPartConfig> wheels,
        Vector3d seatOffset,
        PhysicsConfig physics,
        List<Vector3d> passengerSeatsOffset,
        int passengerSeatCount
) {

    public record WheelPartConfig(WheelType wheelType, boolean front, PartConfig part) {
    }
}
