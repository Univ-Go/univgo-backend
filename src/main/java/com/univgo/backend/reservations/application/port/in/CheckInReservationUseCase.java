package com.univgo.backend.reservations.application.port.in;

import com.univgo.backend.reservations.domain.CheckInResult;
import java.time.LocalTime;
import java.util.UUID;

public interface CheckInReservationUseCase {

    CheckInResult execute(CheckInCommand command);

    /**
     * {@code spaceId} is the space the admin's scan screen is physically at — a reservation for a
     * different space must never validate there, so a mismatch is reported as OTHER_BLOCK instead
     * of checking the student into a roster for a room they aren't in.
     * {@code expectedBlockStart}/{@code expectedBlockEnd} are optional — when the admin's scan
     * screen is also scoped to a specific block, a mismatch is reported as OTHER_BLOCK instead of
     * silently checking the student into the wrong session's roster.
     */
    record CheckInCommand(String code, UUID spaceId, LocalTime expectedBlockStart, LocalTime expectedBlockEnd) {
    }
}
