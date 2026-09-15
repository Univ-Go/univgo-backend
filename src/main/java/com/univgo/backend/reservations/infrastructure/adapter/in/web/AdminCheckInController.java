package com.univgo.backend.reservations.infrastructure.adapter.in.web;

import com.univgo.backend.reservations.application.port.in.CheckInReservationUseCase;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.CheckInScanRequest;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.CheckInScanResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Every scan returns 200 with a verdict body — never a 4xx/5xx. "Cada escaneo
 * tiene que dar una respuesta inequívoca" is a UX contract for the admin at
 * the door, not an error-handling one.
 */
@RestController
@RequestMapping("/admin/checkin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCheckInController {

    private final CheckInReservationUseCase checkInReservationUseCase;

    public AdminCheckInController(CheckInReservationUseCase checkInReservationUseCase) {
        this.checkInReservationUseCase = checkInReservationUseCase;
    }

    @PostMapping("/scan")
    public CheckInScanResponse scan(@Valid @RequestBody CheckInScanRequest request) {
        return CheckInScanResponse.from(checkInReservationUseCase.execute(request.toCommand()));
    }
}
