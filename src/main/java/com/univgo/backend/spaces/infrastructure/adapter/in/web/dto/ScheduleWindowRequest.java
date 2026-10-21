package com.univgo.backend.spaces.infrastructure.adapter.in.web.dto;

import com.univgo.backend.spaces.application.port.in.CreateSpaceUseCase.ScheduleWindow;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

/** @param dayOfWeek 1 is Monday and 7 is Sunday, matching {@code space_schedules.day_of_week}. */
public record ScheduleWindowRequest(
        @Min(1) @Max(7) int dayOfWeek, @NotNull LocalTime startTime, @NotNull LocalTime endTime) {

    public ScheduleWindow toCommand() {
        return new ScheduleWindow(dayOfWeek, startTime, endTime);
    }
}
