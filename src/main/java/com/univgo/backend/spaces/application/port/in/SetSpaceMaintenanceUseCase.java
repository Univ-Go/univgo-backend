package com.univgo.backend.spaces.application.port.in;

import java.util.UUID;

public interface SetSpaceMaintenanceUseCase {

    /** {@code actor} is the administrator the closure is recorded against, either way. */
    void execute(UUID spaceId, boolean underMaintenance, UUID actor);
}
