package com.univgo.backend.reservations.domain;

import java.time.LocalDate;

/** totalCapacity 0 means the space has no blocks that day (closed/not operating). */
public record SpaceDaySummary(LocalDate date, int totalCapacity, int totalFree) {
}
