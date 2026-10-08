package com.univgo.backend.spaces.application.port.out;

/**
 * How many photographs a space may hold. A tunable reaches a use case through a port here for the
 * same reason {@code InstitutionConfig} does: the application layer never reads configuration
 * directly, which is what keeps {@code shared.config} an infrastructure-only import.
 */
public interface SpaceImageLimitsPort {

    int maxPerSpace();
}
