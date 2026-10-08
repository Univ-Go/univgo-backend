package com.univgo.backend.spaces.application.port.out;

import java.util.UUID;

/**
 * How many reservations retiring a space would suspend. Declared here and implemented by the
 * reservations module: {@code spaces} must not depend on {@code reservations} — that is the
 * direction every other import already runs — so the consumer owns the port and the module that has
 * the data fulfils it.
 */
public interface SpaceReservationCounterPort {

    /** Reservations still cancellable, which are exactly the ones a closure suspends. */
    int countSuspendable(UUID spaceId);
}
