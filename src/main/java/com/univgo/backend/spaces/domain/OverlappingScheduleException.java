package com.univgo.backend.spaces.domain;

import java.time.LocalTime;

public class OverlappingScheduleException extends RuntimeException {

    public OverlappingScheduleException(
            int dayOfWeek, LocalTime firstStart, LocalTime firstEnd, LocalTime secondStart, LocalTime secondEnd) {
        super("Overlapping opening windows on day %d: %s-%s and %s-%s"
                .formatted(dayOfWeek, firstStart, firstEnd, secondStart, secondEnd));
    }
}
