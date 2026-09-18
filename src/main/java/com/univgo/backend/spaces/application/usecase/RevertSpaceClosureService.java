package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.spaces.application.port.in.RevertSpaceClosureUseCase;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.domain.SpaceClosure;
import com.univgo.backend.spaces.domain.SpaceClosureNotFoundException;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RevertSpaceClosureService implements RevertSpaceClosureUseCase {

    private final SpaceClosureRepositoryPort spaceClosureRepositoryPort;

    public RevertSpaceClosureService(SpaceClosureRepositoryPort spaceClosureRepositoryPort) {
        this.spaceClosureRepositoryPort = spaceClosureRepositoryPort;
    }

    /**
     * Nothing is restored by hand: a suspended reservation was never touched, so the moment the
     * closure stops being in force it reads as reserved again, like any other.
     */
    @Override
    public SpaceClosure execute(UUID closureId, UUID actor) {
        SpaceClosure closure =
                spaceClosureRepositoryPort.findById(closureId).orElseThrow(() -> new SpaceClosureNotFoundException(closureId));

        closure.revert(actor, LocalDateTime.now());

        return spaceClosureRepositoryPort.save(closure);
    }
}
