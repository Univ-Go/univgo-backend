package com.univgo.backend.spaces.application.port.in;

import com.univgo.backend.spaces.domain.ClosureReason;
import com.univgo.backend.spaces.domain.SpaceClosure;
import java.time.LocalDateTime;
import java.util.UUID;

public interface CloseSpaceUseCase {

    SpaceClosure execute(CloseSpaceCommand command);

    /**
     * {@code endsAt} null closes the space until somebody reverts it, which is what the panel's
     * maintenance switch asks for. Closing does not cancel anything: the reservations inside the
     * window are suspended and come back if the closure is reverted (spec §12).
     */
    record CloseSpaceCommand(
            UUID spaceId,
            LocalDateTime startsAt,
            LocalDateTime endsAt,
            ClosureReason reason,
            String details,
            UUID actor) {
    }
}
