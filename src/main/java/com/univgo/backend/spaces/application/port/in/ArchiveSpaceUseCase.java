package com.univgo.backend.spaces.application.port.in;

import java.util.UUID;

public interface ArchiveSpaceUseCase {

    /**
     * Retires a space and reports how many reservations the archiving suspends, so the panel can
     * say so before and after. It cancels nothing: per spec §12 a closure suspends.
     */
    int execute(UUID spaceId, UUID actor);
}
