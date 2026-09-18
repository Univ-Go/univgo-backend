package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.domain.SpaceBlockSummary;
import com.univgo.backend.spaces.domain.ClosureReason;
import java.time.LocalTime;

public record SpaceBlockSummaryResponse(
        LocalTime start,
        LocalTime end,
        int capacity,
        int occupied,
        int free,
        boolean closed,
        ClosureReason closureReason) {

    public static SpaceBlockSummaryResponse from(SpaceBlockSummary summary) {
        return new SpaceBlockSummaryResponse(
                summary.block().start(),
                summary.block().end(),
                summary.capacity(),
                summary.occupied(),
                summary.free(),
                summary.closed(),
                summary.closureReason());
    }
}
