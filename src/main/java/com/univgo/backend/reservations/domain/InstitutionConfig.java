package com.univgo.backend.reservations.domain;

import java.time.Duration;

/**
 * The four institution-wide numbers the functional spec calls "configuración
 * de la institución, no reglas escritas en el código" — tunable without a
 * code change or redeploy.
 */
public class InstitutionConfig {

    private final int blockDurationMinutes;
    private final int checkInToleranceMinutes;
    private final int minUsageMinutes;
    private final int reservationsPerSpacePerDay;

    public InstitutionConfig(
            int blockDurationMinutes, int checkInToleranceMinutes, int minUsageMinutes, int reservationsPerSpacePerDay) {
        this.blockDurationMinutes = blockDurationMinutes;
        this.checkInToleranceMinutes = checkInToleranceMinutes;
        this.minUsageMinutes = minUsageMinutes;
        this.reservationsPerSpacePerDay = reservationsPerSpacePerDay;
    }

    public int getBlockDurationMinutes() {
        return blockDurationMinutes;
    }

    public int getCheckInToleranceMinutes() {
        return checkInToleranceMinutes;
    }

    public int getMinUsageMinutes() {
        return minUsageMinutes;
    }

    public int getReservationsPerSpacePerDay() {
        return reservationsPerSpacePerDay;
    }

    public Duration blockDuration() {
        return Duration.ofMinutes(blockDurationMinutes);
    }

    public Duration tolerance() {
        return Duration.ofMinutes(checkInToleranceMinutes);
    }

    public Duration minUsage() {
        return Duration.ofMinutes(minUsageMinutes);
    }
}
