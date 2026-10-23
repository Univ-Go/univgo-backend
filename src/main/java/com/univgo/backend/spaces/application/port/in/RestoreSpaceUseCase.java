package com.univgo.backend.spaces.application.port.in;

import java.util.UUID;

public interface RestoreSpaceUseCase {

    /**
     * Puts a space back in the catalogue. It does not reopen it: the indefinite closure archiving
     * created stays in force until somebody reverts it, exactly as the maintenance switch behaves.
     */
    void execute(UUID spaceId);
}
