package com.univgo.backend.spaces.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateSpaceIdentityRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 150) String location,
        @NotNull UUID spaceTypeId,
        @Min(1) @Max(1000) int capacity) {
}
