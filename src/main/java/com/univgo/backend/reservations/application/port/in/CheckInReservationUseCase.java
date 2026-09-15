package com.univgo.backend.reservations.application.port.in;

import com.univgo.backend.reservations.domain.CheckInResult;
import java.time.LocalTime;

public interface CheckInReservationUseCase {

    CheckInResult execute(CheckInCommand command);

    /**
     * {@code expectedBlockStart}/{@code expectedBlockEnd} are optional — when the admin's scan
     * screen is scoped to a specific block, a mismatch is reported as OTHER_BLOCK instead of
     * silently checking the student into the wrong session's roster.
     */
    record CheckInCommand(String code, LocalTime expectedBlockStart, LocalTime expectedBlockEnd) {
    }
}
