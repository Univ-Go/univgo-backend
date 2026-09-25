package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.shared.util.Uuidv7Generator;
import com.univgo.backend.spaces.application.port.in.CloseSpaceUseCase;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.application.port.out.SpaceRepositoryPort;
import com.univgo.backend.spaces.domain.ClosureCause;
import com.univgo.backend.spaces.domain.ClosurePeriod;
import com.univgo.backend.spaces.domain.ClosureReversion;
import com.univgo.backend.spaces.domain.SpaceClosure;
import com.univgo.backend.spaces.domain.SpaceNotFoundException;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class CloseSpaceService implements CloseSpaceUseCase {

    private final SpaceRepositoryPort spaceRepositoryPort;
    private final SpaceClosureRepositoryPort spaceClosureRepositoryPort;

    public CloseSpaceService(
            SpaceRepositoryPort spaceRepositoryPort, SpaceClosureRepositoryPort spaceClosureRepositoryPort) {
        this.spaceRepositoryPort = spaceRepositoryPort;
        this.spaceClosureRepositoryPort = spaceClosureRepositoryPort;
    }

    @Override
    public SpaceClosure execute(CloseSpaceCommand command) {
        if (!spaceRepositoryPort.existsById(command.spaceId())) {
            throw new SpaceNotFoundException(command.spaceId());
        }

        LocalDateTime now = LocalDateTime.now();

        return spaceClosureRepositoryPort.save(new SpaceClosure(
                Uuidv7Generator.generate(),
                command.spaceId(),
                new ClosurePeriod(command.startsAt() == null ? now : command.startsAt(), command.endsAt()),
                new ClosureCause(command.reason(), command.details()),
                command.actor(),
                now,
                ClosureReversion.none()));
    }
}
