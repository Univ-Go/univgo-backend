package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.spaces.application.port.in.UpdateSpaceUsageUseCase;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.domain.Space;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UpdateSpaceUsageService implements UpdateSpaceUsageUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;

    public UpdateSpaceUsageService(SpaceRepositoryPort spaceRepositoryPort) {
        this.spaceRepositoryPort = spaceRepositoryPort;
    }

    @Override
    public void execute(UpdateSpaceUsageCommand command) {
        Space existing = spaceRepositoryPort
                .findById(command.spaceId())
                .orElseThrow(() -> new SpaceNotFoundException(command.spaceId()));

        spaceRepositoryPort.save(Space.builder()
                .id(existing.getId())
                .name(existing.getName())
                .location(existing.getLocation())
                .capacity(existing.getCapacity())
                .spaceTypeId(existing.getSpaceTypeId())
                .category(existing.getCategory())
                .description(command.description())
                .rules(command.rules())
                .build());
    }
}
