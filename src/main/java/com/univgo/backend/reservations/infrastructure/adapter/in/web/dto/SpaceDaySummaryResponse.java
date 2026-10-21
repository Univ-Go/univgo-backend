package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.domain.SpaceDaySummary;
import java.time.LocalDate;

public record SpaceDaySummaryResponse(LocalDate date, int totalCapacity, int totalFree) {

    public static SpaceDaySummaryResponse from(SpaceDaySummary summary) {
        return new SpaceDaySummaryResponse(summary.date(), summary.totalCapacity(), summary.totalFree());
    }
}
