package com.jvmd.uniqueVehiclePlugin.util;

import org.bukkit.Location;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class VehicleTransformUtil {

    private VehicleTransformUtil() {
    }

    public static Location computePartLocation(Location base, double ox, double oy, double oz) {
        double rad = Math.toRadians(base.getYaw());
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        return base.clone().add(ox * cos - oz * sin, oy, ox * sin + oz * cos);
    }

    public static Vector3f computePivotTranslation(Vector3f pivot, Quaternionf rotation, Vector3f scale) {
        Vector3f transformedPivot = new Vector3f(pivot).mul(scale);
        rotation.transform(transformedPivot);
        return new Vector3f(pivot).sub(transformedPivot);
    }
}
