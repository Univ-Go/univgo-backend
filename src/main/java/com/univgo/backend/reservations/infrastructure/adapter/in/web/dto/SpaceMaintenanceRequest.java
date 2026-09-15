package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

public record SpaceMaintenanceRequest(@NotNull Boolean underMaintenance) {
}
