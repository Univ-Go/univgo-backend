package com.univgo.backend.reservations.infrastructure.adapter.in.web;

import com.univgo.backend.reservations.application.port.in.GetAllReservationsUseCase;
import com.univgo.backend.reservations.application.port.in.GetInstitutionConfigUseCase;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.ReservationResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/reservations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReservationsController {

    private final GetAllReservationsUseCase getAllReservationsUseCase;
    private final GetInstitutionConfigUseCase getInstitutionConfigUseCase;

    public AdminReservationsController(
            GetAllReservationsUseCase getAllReservationsUseCase, GetInstitutionConfigUseCase getInstitutionConfigUseCase) {
        this.getAllReservationsUseCase = getAllReservationsUseCase;
        this.getInstitutionConfigUseCase = getInstitutionConfigUseCase;
    }

    @GetMapping
    public List<ReservationResponse> findAll() {
        InstitutionConfig config = getInstitutionConfigUseCase.execute();
        return getAllReservationsUseCase.execute().stream().map(r -> ReservationResponse.from(r, config)).toList();
    }
}
