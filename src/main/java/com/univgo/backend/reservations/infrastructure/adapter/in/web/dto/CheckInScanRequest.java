package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.application.port.in.CheckInReservationUseCase.CheckInCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.UUID;

public record CheckInScanRequest(
        @NotBlank String code,
        @NotNull UUID spaceId,
        LocalTime expectedBlockStart,
        LocalTime expectedBlockEnd) {

    public CheckInCommand toCommand() {
        return new CheckInCommand(code, spaceId, expectedBlockStart, expectedBlockEnd);
    }
}
