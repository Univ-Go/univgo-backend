package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.spaces.application.port.in.UpdateSpaceIdentityUseCase;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceTypeRepositoryPort;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import com.univgo.backend.spaces.domain.SpaceType;
import com.univgo.backend.spaces.domain.SpaceTypeNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UpdateSpaceIdentityService implements UpdateSpaceIdentityUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;
    private final SpaceTypeRepositoryPort spaceTypeRepositoryPort;

    public UpdateSpaceIdentityService(
            SpaceRepositoryPort spaceRepositoryPort, SpaceTypeRepositoryPort spaceTypeRepositoryPort) {
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.spaceTypeRepositoryPort = spaceTypeRepositoryPort;
    }

    @Override
    public void execute(UpdateSpaceIdentityCommand command) {
        Space existing = spaceRepositoryPort
                .findById(command.spaceId())
                .orElseThrow(() -> new SpaceNotFoundException(command.spaceId()));
        SpaceType type = spaceTypeRepositoryPort
                .findById(command.spaceTypeId())
                .orElseThrow(() -> new SpaceTypeNotFoundException(command.spaceTypeId()));

        spaceRepositoryPort.save(new Space(
                existing.getId(),
                command.name(),
                command.location(),
                command.capacity(),
                type.id(),
                type.category(),
                existing.getDescription(),
                existing.getRules()));
    }
}
