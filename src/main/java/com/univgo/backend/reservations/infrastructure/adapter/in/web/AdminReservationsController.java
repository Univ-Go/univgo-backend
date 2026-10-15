package com.univgo.backend.reservations.infrastructure.adapter.in.web;

import com.univgo.backend.reservations.application.port.in.CreateReservationUseCase;
import com.univgo.backend.reservations.application.port.in.CreateReservationUseCase.CreateReservationCommand;
import com.univgo.backend.reservations.application.port.in.GetAllReservationsUseCase;
import com.univgo.backend.reservations.application.port.in.GetInstitutionConfigUseCase;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.AdminCreateReservationRequest;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.ReservationResponse;
import com.univgo.backend.spaces.application.port.in.GetSpaceClosuresUseCase;
import com.univgo.backend.spaces.domain.SpaceClosures;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import com.univgo.backend.users.domain.User;
import com.univgo.backend.users.domain.UserNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/reservations")
@PreAuthorize("hasRole('ADMIN')")
public class AdminReservationsController {

    private final GetAllReservationsUseCase getAllReservationsUseCase;
    private final GetInstitutionConfigUseCase getInstitutionConfigUseCase;
    private final GetSpaceClosuresUseCase getSpaceClosuresUseCase;
    private final CreateReservationUseCase createReservationUseCase;
    private final UserRepositoryPort userRepositoryPort;

    public AdminReservationsController(
            GetAllReservationsUseCase getAllReservationsUseCase,
            GetInstitutionConfigUseCase getInstitutionConfigUseCase,
            GetSpaceClosuresUseCase getSpaceClosuresUseCase,
            CreateReservationUseCase createReservationUseCase,
            UserRepositoryPort userRepositoryPort) {
        this.getAllReservationsUseCase = getAllReservationsUseCase;
        this.getInstitutionConfigUseCase = getInstitutionConfigUseCase;
        this.getSpaceClosuresUseCase = getSpaceClosuresUseCase;
        this.createReservationUseCase = createReservationUseCase;
        this.userRepositoryPort = userRepositoryPort;
    }

    @GetMapping
    public List<ReservationResponse> findAll() {
        InstitutionConfig config = getInstitutionConfigUseCase.execute();
        SpaceClosures closures = SpaceClosures.of(getSpaceClosuresUseCase.inForce());
        return getAllReservationsUseCase.execute().stream()
                .map(r -> ReservationResponse.from(r, config, closures))
                .toList();
    }

    /** For a student at the door without the app yet — identified by CC or institutional email. */
    @PreAuthorize("hasRole('ADMIN') or hasRole('GYM_ADMIN')")
    @PostMapping
    public ReservationResponse createForStudent(
            @Valid @RequestBody AdminCreateReservationRequest request, Authentication authentication) {
        if (!GymSpaceAccessGuard.permits(authentication, request.spaceId())) {
            throw new AccessDeniedException("GYM_ADMIN cannot book outside the gym space");
        }
        User student = userRepositoryPort
                .findByLoginIdentifier(request.studentIdentifier())
                .orElseThrow(() -> new UserNotFoundException(request.studentIdentifier()));
        var command = new CreateReservationCommand(
                student.getId(), request.spaceId(), request.reservationDate(), request.startTime());
        return toResponse(createReservationUseCase.execute(command));
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return ReservationResponse.from(
                reservation,
                getInstitutionConfigUseCase.execute(),
                SpaceClosures.of(getSpaceClosuresUseCase.inForce()));
    }
}
