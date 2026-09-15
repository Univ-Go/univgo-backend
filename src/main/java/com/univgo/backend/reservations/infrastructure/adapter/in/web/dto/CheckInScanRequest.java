package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.application.port.in.CheckInReservationUseCase.CheckInCommand;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalTime;

public record CheckInScanRequest(@NotBlank String code, LocalTime expectedBlockStart, LocalTime expectedBlockEnd) {

    public CheckInCommand toCommand() {
        return new CheckInCommand(code, expectedBlockStart, expectedBlockEnd);
    }
}
