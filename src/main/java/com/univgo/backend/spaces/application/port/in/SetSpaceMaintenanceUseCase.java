package com.univgo.backend.spaces.application.port.in;

import java.util.UUID;

public interface SetSpaceMaintenanceUseCase {

    void execute(UUID spaceId, boolean underMaintenance);
}
