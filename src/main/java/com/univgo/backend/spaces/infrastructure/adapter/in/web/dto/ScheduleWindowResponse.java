package com.univgo.backend.spaces.infrastructure.adapter.in.web.dto;

import com.univgo.backend.spaces.domain.SpaceSchedule;
import java.time.LocalTime;
import java.util.UUID;

/** @param dayOfWeek 1 is Monday and 7 is Sunday, as {@code space_schedules} stores it. */
public record ScheduleWindowResponse(UUID id, int dayOfWeek, LocalTime startTime, LocalTime endTime) {

    public static ScheduleWindowResponse from(SpaceSchedule schedule) {
        return new ScheduleWindowResponse(
                schedule.getId(), schedule.getDayOfWeek(), schedule.getStartTime(), schedule.getEndTime());
    }
}
