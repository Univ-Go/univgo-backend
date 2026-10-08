package com.univgo.backend.spaces.infrastructure.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/**
 * The JSON part of the multipart creation. The photographs travel as the other parts, so "at least
 * one photograph" is not expressible here and is checked by the use case instead: a part count is
 * not a field.
 *
 * @param rules     may be empty — a space whose rules nobody has written yet is a real state.
 * @param schedules 56 is seven days of eight windows; a ceiling, not a product rule.
 */
public record CreateSpaceRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 150) String location,
        @NotNull UUID spaceTypeId,
        @Min(1) @Max(1000) int capacity,
        @NotBlank @Size(max = 2000) String description,
        @Size(max = 20) List<@NotBlank @Size(max = 300) String> rules,
        @NotEmpty @Size(max = 56) List<@Valid ScheduleWindowRequest> schedules) {

    public List<String> safeRules() {
        return rules == null ? List.of() : rules;
    }
}
