package com.univgo.backend.spaces.application.port.in;

import com.univgo.backend.spaces.domain.SpaceClosure;
import java.util.UUID;

public interface RevertSpaceClosureUseCase {

    /** Reopens the space and gives its suspended reservations back, exactly as they were. */
    SpaceClosure execute(UUID closureId, UUID actor);
}
