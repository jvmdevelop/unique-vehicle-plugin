package com.jvmd.uniqueVehiclePlugin.config;

public record PhysicsConfig(
        double maxSpeed,
        double acceleration,
        double friction,
        float turnRate,
        double brakeForce,
        double reverseMaxSpeed,
        double reverseAcceleration,
        double driftBrakeForce,
        float driftTurnMultiplier,
        double vehicleWidth,
        double vehicleLength
) {

    public static PhysicsConfig defaults() {
        return new PhysicsConfig(
                1.2,    // maxSpeed
                0.05,   // acceleration
                0.02,   // friction
                3.5f,   // turnRate
                0.08,   // brakeForce
                0.4,    // reverseMaxSpeed
                0.03,   // reverseAcceleration
                0.04,   // driftBrakeForce
                1.8f,   // driftTurnMultiplier
                0.8,    // vehicleWidth
                1.2     // vehicleLength
        );
    }
}
