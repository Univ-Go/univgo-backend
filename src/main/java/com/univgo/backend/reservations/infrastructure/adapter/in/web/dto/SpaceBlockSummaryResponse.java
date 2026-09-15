package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.domain.SpaceBlockSummary;
import java.time.LocalTime;

public record SpaceBlockSummaryResponse(LocalTime start, LocalTime end, int capacity, int occupied, int free) {

    public static SpaceBlockSummaryResponse from(SpaceBlockSummary summary) {
        return new SpaceBlockSummaryResponse(
                summary.block().start(), summary.block().end(), summary.capacity(), summary.occupied(), summary.free());
    }
}
