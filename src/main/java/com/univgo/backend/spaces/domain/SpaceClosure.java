package com.univgo.backend.spaces.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * A stretch of time a space is not operating, per the spec's §12.
 *
 * <p>A closure <em>suspends</em>; it never cancels. The reservations inside it keep their place and
 * come back if it is reverted — which is the whole reason it works this way: once a student has
 * been told their place is gone, restoring it would hand them two bookings for the same hour.
 *
 * <p>{@code endsAt} is exclusive, and null means "until somebody reverts it" — which is what the
 * panel's maintenance switch creates. {@code revertedAt} is what ends a closure without erasing it:
 * that a space was closed three times this month and reopened twice is part of what the panel is
 * for.
 */
public class SpaceClosure {

    private final UUID id;
    private final UUID spaceId;
    private final ClosurePeriod period;
    private final ClosureCause cause;
    private final UUID createdBy;
    private final LocalDateTime createdAt;
    private ClosureReversion reversion;

    public SpaceClosure(
            UUID id,
            UUID spaceId,
            ClosurePeriod period,
            ClosureCause cause,
            UUID createdBy,
            LocalDateTime createdAt,
            ClosureReversion reversion) {
        this.id = id;
        this.spaceId = spaceId;
        this.period = period;
        this.cause = cause;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.reversion = reversion;
    }

    /** In force until reverted: a closure whose window has passed is history, not a fact about now. */
    public boolean isInForce() {
        return !reversion.isReverted();
    }

    /** Open-ended closures are what the maintenance switch creates, and what it reverts. */
    public boolean isIndefinite() {
        return period.endsAt() == null;
    }

    public boolean coversInstant(LocalDateTime instant) {
        return isInForce()
                && !instant.isBefore(period.startsAt())
                && (period.endsAt() == null || instant.isBefore(period.endsAt()));
    }

    /**
     * Whether the closure eats into a block at all. Any overlap counts: half a block is not a block
     * somebody can use, and offering it would be promising a room that shuts halfway through.
     */
    public boolean coversBlock(LocalDate date, LocalTime blockStart, LocalTime blockEnd) {
        if (!isInForce()) {
            return false;
        }
        LocalDateTime start = LocalDateTime.of(date, blockStart);
        LocalDateTime end = LocalDateTime.of(date, blockEnd);
        LocalDateTime endsAt = period.endsAt();
        return start.isBefore(endsAt == null ? end : endsAt) && period.startsAt().isBefore(end);
    }

    public void revert(UUID actor, LocalDateTime now) {
        if (!isInForce()) {
            throw new IllegalStateException("Closure " + id + " was already reverted");
        }
        this.reversion = new ClosureReversion(now, actor);
    }

    public UUID getId() {
        return id;
    }

    public UUID getSpaceId() {
        return spaceId;
    }

    public LocalDateTime getStartsAt() {
        return period.startsAt();
    }

    public LocalDateTime getEndsAt() {
        return period.endsAt();
    }

    public ClosureReason getReason() {
        return cause.reason();
    }

    public String getDetails() {
        return cause.details();
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getRevertedAt() {
        return reversion.revertedAt();
    }

    public UUID getRevertedBy() {
        return reversion.revertedBy();
    }
}
