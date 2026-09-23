package com.univgo.backend.reservations.infrastructure.adapter.in.web;

import com.univgo.backend.reservations.application.port.in.GetAllReservationsUseCase;
import com.univgo.backend.reservations.application.port.in.GetInstitutionConfigUseCase;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.ReservationResponse;
import com.univgo.backend.spaces.application.port.in.GetSpaceClosuresUseCase;
import com.univgo.backend.spaces.domain.SpaceClosures;
import java.time.Clock;
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
    private final GetSpaceClosuresUseCase getSpaceClosuresUseCase;
    private final Clock clock;

    public AdminReservationsController(
            GetAllReservationsUseCase getAllReservationsUseCase,
            GetInstitutionConfigUseCase getInstitutionConfigUseCase,
            GetSpaceClosuresUseCase getSpaceClosuresUseCase,
            Clock clock) {
        this.getAllReservationsUseCase = getAllReservationsUseCase;
        this.getInstitutionConfigUseCase = getInstitutionConfigUseCase;
        this.getSpaceClosuresUseCase = getSpaceClosuresUseCase;
        this.clock = clock;
    }

    @GetMapping
    public List<ReservationResponse> findAll() {
        InstitutionConfig config = getInstitutionConfigUseCase.execute();
        SpaceClosures closures = SpaceClosures.of(getSpaceClosuresUseCase.inForce());
        return getAllReservationsUseCase.execute().stream()
                .map(r -> ReservationResponse.from(r, config, closures, clock))
                .toList();
    }
}
