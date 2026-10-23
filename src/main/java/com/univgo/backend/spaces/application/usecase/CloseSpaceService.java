package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.shared.util.Uuidv7Generator;
import com.univgo.backend.spaces.application.port.in.CloseSpaceUseCase;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.domain.SpaceClosure;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import java.time.Clock;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class CloseSpaceService implements CloseSpaceUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;
    private final SpaceClosureRepositoryPort spaceClosureRepositoryPort;
    private final Clock clock;

    public CloseSpaceService(
            SpaceRepositoryPort spaceRepositoryPort,
            SpaceClosureRepositoryPort spaceClosureRepositoryPort,
            Clock clock) {
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.spaceClosureRepositoryPort = spaceClosureRepositoryPort;
        this.clock = clock;
    }

    @Override
    public SpaceClosure execute(CloseSpaceCommand command) {
        if (!spaceRepositoryPort.existsActiveById(command.spaceId())) {
            throw new SpaceNotFoundException(command.spaceId());
        }

        LocalDateTime now = LocalDateTime.now(clock);

        return spaceClosureRepositoryPort.save(SpaceClosure.builder()
                .id(Uuidv7Generator.generate())
                .spaceId(command.spaceId())
                .startsAt(command.startsAt() == null ? now : command.startsAt())
                .endsAt(command.endsAt())
                .reason(command.reason())
                .details(command.details())
                .createdBy(command.actor())
                .createdAt(now)
                .build());
    }
}
