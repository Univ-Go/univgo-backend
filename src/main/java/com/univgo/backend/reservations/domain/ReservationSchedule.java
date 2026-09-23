package com.univgo.backend.reservations.domain;

import com.univgo.backend.spaces.domain.TimeBlock;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** The day and block a reservation is for. {@link TimeBlock} already guarantees end after start. */
public record ReservationSchedule(LocalDate date, TimeBlock block) {

    public LocalDateTime startDateTime() {
        return LocalDateTime.of(date, block.start());
    }

    public LocalDateTime endDateTime() {
        return LocalDateTime.of(date, block.end());
    }
}
