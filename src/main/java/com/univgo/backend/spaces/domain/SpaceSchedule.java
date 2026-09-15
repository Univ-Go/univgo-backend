package com.univgo.backend.spaces.domain;

import java.time.LocalTime;
import java.util.UUID;

/**
 * One recurring weekly opening window for a space, e.g. "Monday 06:00-22:00".
 * A space can have several of these per day of week, though today's seed data
 * only ever needs one per day.
 */
public class SpaceSchedule {

    private final UUID id;
    private final UUID spaceId;
    private final int dayOfWeek;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public SpaceSchedule(UUID id, UUID spaceId, int dayOfWeek, LocalTime startTime, LocalTime endTime) {
        if (dayOfWeek < 1 || dayOfWeek > 7) {
            throw new IllegalArgumentException("dayOfWeek must be between 1 (Monday) and 7 (Sunday)");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
        this.id = id;
        this.spaceId = spaceId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSpaceId() {
        return spaceId;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }
}
