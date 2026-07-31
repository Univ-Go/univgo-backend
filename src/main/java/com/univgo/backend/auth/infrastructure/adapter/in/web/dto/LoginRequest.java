package com.univgo.backend.auth.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String identification,
        @NotBlank String password) {
}
