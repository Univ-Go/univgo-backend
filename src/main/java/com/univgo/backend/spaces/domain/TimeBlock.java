package com.univgo.backend.spaces.domain;

import java.time.LocalTime;

/**
 * A fixed-duration reservation slot within a space's opening hours, e.g.
 * 14:00-16:00. Students pick one of these; they never choose a custom start
 * time.
 */
public record TimeBlock(LocalTime start, LocalTime end) {

    public TimeBlock {
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("end must be after start");
        }
    }
}
