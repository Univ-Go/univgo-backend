package com.univgo.backend.reservations.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * All the clock math from the functional spec, in one place, as pure
 * functions of an explicit "now" — nothing here reads the system clock or
 * touches a database. Expirada and Finalizada are never written to a row;
 * they fall out of comparing "now" against these formulas.
 */
public final class ReservationTimingCalculator {

    private ReservationTimingCalculator() {
    }

    /** Último instante en que un bloque se puede reservar: fin - uso_mínimo - tolerancia. */
    public static LocalTime lastBookableInstant(LocalTime blockEnd, Duration minUsage, Duration tolerance) {
        return blockEnd.minus(minUsage).minus(tolerance);
    }

    public static boolean isBlockStillBookable(
            LocalDate blockDate, LocalTime blockEnd, LocalDateTime now, Duration minUsage, Duration tolerance) {
        LocalDateTime cutoff = LocalDateTime.of(blockDate, lastBookableInstant(blockEnd, minUsage, tolerance));
        return !now.isAfter(cutoff);
    }

    /** El check-in abre: max(inicio del bloque - tolerancia, hora de creación). */
    public static LocalDateTime checkInOpensAt(LocalDateTime blockStart, LocalDateTime createdAt, Duration tolerance) {
        LocalDateTime toleranceBeforeStart = blockStart.minus(tolerance);
        return toleranceBeforeStart.isAfter(createdAt) ? toleranceBeforeStart : createdAt;
    }

    /** El check-in cierra: min(max(inicio del bloque, hora de creación) + tolerancia, fin del bloque - uso_mínimo). */
    public static LocalDateTime checkInClosesAt(
            LocalDateTime blockStart,
            LocalDateTime blockEnd,
            LocalDateTime createdAt,
            Duration tolerance,
            Duration minUsage) {
        LocalDateTime startOrCreated = blockStart.isAfter(createdAt) ? blockStart : createdAt;
        LocalDateTime toleranceDeadline = startOrCreated.plus(tolerance);
        LocalDateTime minUsageDeadline = blockEnd.minus(minUsage);
        return toleranceDeadline.isBefore(minUsageDeadline) ? toleranceDeadline : minUsageDeadline;
    }

    public static ReservationState stateAt(
            LocalDateTime now,
            LocalDateTime blockStart,
            LocalDateTime blockEnd,
            LocalDateTime createdAt,
            LocalDateTime checkedInAt,
            LocalDateTime cancelledAt,
            Duration tolerance,
            Duration minUsage) {
        if (cancelledAt != null) {
            return ReservationState.CANCELLED;
        }
        if (checkedInAt != null) {
            return now.isBefore(blockEnd) ? ReservationState.IN_PROGRESS : ReservationState.FINISHED;
        }
        LocalDateTime closesAt = checkInClosesAt(blockStart, blockEnd, createdAt, tolerance, minUsage);
        return now.isAfter(closesAt) ? ReservationState.EXPIRED : ReservationState.RESERVED;
    }
}
