package com.univgo.backend.spaces.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record UpdateSpaceUsageRequest(
        @NotBlank @Size(max = 2000) String description,
        @Size(max = 20) List<@NotBlank @Size(max = 300) String> rules) {

    public List<String> safeRules() {
        return rules == null ? List.of() : rules;
    }
}
