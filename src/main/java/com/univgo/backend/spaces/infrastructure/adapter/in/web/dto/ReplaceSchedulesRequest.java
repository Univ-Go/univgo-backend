package com.univgo.backend.spaces.infrastructure.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ReplaceSchedulesRequest(
        @NotEmpty @Size(max = 56) List<@Valid ScheduleWindowRequest> schedules) {
}
