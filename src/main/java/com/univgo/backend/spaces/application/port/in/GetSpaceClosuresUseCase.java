package com.univgo.backend.spaces.application.port.in;

import com.univgo.backend.spaces.domain.SpaceClosure;
import java.util.List;
import java.util.UUID;

public interface GetSpaceClosuresUseCase {

    /** Every closure of a space, reverted ones included: the panel's history. */
    List<SpaceClosure> execute(UUID spaceId);

    /**
     * Every closure in force, whichever space it belongs to. One read for a list of reservations
     * that can span several spaces, which is what the student's own list is.
     */
    List<SpaceClosure> inForce();
}
