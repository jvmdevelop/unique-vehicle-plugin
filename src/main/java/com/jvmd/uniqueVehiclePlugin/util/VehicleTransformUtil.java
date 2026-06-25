package com.jvmd.uniqueVehiclePlugin.util;

import org.bukkit.Location;

public final class VehicleTransformUtil {

    private VehicleTransformUtil() {
    }

    public static Location computePartLocation(Location base, double ox, double oy, double oz) {
        double rad = Math.toRadians(base.getYaw());
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        return base.clone().add(ox * cos - oz * sin, oy, ox * sin + oz * cos);
    }

}
