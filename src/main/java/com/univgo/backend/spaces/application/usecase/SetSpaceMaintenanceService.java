package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.spaces.application.port.in.SetSpaceMaintenanceUseCase;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SetSpaceMaintenanceService implements SetSpaceMaintenanceUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;

    public SetSpaceMaintenanceService(SpaceRepositoryPort spaceRepositoryPort) {
        this.spaceRepositoryPort = spaceRepositoryPort;
    }

    @Override
    public void execute(UUID spaceId, boolean underMaintenance) {
        if (!spaceRepositoryPort.existsById(spaceId)) {
            throw new SpaceNotFoundException(spaceId);
        }
        spaceRepositoryPort.updateMaintenance(spaceId, underMaintenance);
    }
}
