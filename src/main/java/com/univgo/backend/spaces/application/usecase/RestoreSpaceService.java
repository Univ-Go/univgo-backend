package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.spaces.application.port.in.RestoreSpaceUseCase;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Puts a space back in the catalogue without reopening it. The indefinite closure archiving created
 * stays in force until somebody reverts it deliberately, which is how the maintenance switch already
 * behaves: coming back into the list and being open for business are two decisions.
 */
@Service
public class RestoreSpaceService implements RestoreSpaceUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;

    public RestoreSpaceService(SpaceRepositoryPort spaceRepositoryPort) {
        this.spaceRepositoryPort = spaceRepositoryPort;
    }

    @Override
    public void execute(UUID spaceId) {
        if (spaceRepositoryPort.findById(spaceId).isEmpty()) {
            throw new SpaceNotFoundException(spaceId);
        }
        spaceRepositoryPort.restore(spaceId);
    }
}
