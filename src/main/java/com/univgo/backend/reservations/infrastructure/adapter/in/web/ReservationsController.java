package com.univgo.backend.reservations.infrastructure.adapter.in.web;

import com.univgo.backend.reservations.application.port.in.CancelReservationUseCase;
import com.univgo.backend.reservations.application.port.in.CreateReservationUseCase;
import com.univgo.backend.reservations.application.port.in.CreateReservationUseCase.CreateReservationCommand;
import com.univgo.backend.reservations.application.port.in.GetInstitutionConfigUseCase;
import com.univgo.backend.reservations.application.port.in.GetReservationByIdUseCase;
import com.univgo.backend.reservations.application.port.in.GetReservationsByUserUseCase;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationNotFoundException;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.CreateReservationRequest;
import com.univgo.backend.spaces.application.port.in.GetSpaceClosuresUseCase;
import com.univgo.backend.spaces.domain.SpaceClosures;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.ReservationResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
public class ReservationsController {

    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    private final CreateReservationUseCase createReservationUseCase;
    private final GetReservationsByUserUseCase getReservationsByUserUseCase;
    private final GetReservationByIdUseCase getReservationByIdUseCase;
    private final CancelReservationUseCase cancelReservationUseCase;
    private final GetInstitutionConfigUseCase getInstitutionConfigUseCase;
    private final GetSpaceClosuresUseCase getSpaceClosuresUseCase;

    public ReservationsController(
            CreateReservationUseCase createReservationUseCase,
            GetReservationsByUserUseCase getReservationsByUserUseCase,
            GetReservationByIdUseCase getReservationByIdUseCase,
            CancelReservationUseCase cancelReservationUseCase,
            GetInstitutionConfigUseCase getInstitutionConfigUseCase,
            GetSpaceClosuresUseCase getSpaceClosuresUseCase) {
        this.createReservationUseCase = createReservationUseCase;
        this.getReservationsByUserUseCase = getReservationsByUserUseCase;
        this.getReservationByIdUseCase = getReservationByIdUseCase;
        this.cancelReservationUseCase = cancelReservationUseCase;
        this.getInstitutionConfigUseCase = getInstitutionConfigUseCase;
        this.getSpaceClosuresUseCase = getSpaceClosuresUseCase;
    }

    @PostMapping
    public ReservationResponse create(@Valid @RequestBody CreateReservationRequest request, Authentication authentication) {
        var command = new CreateReservationCommand(
                currentUserId(authentication), request.spaceId(), request.reservationDate(), request.startTime());
        return toResponse(createReservationUseCase.execute(command));
    }

    /** One read of the closures for the whole list: a student's reservations span several spaces. */
    @GetMapping("/me")
    public List<ReservationResponse> findMine(Authentication authentication) {
        InstitutionConfig config = getInstitutionConfigUseCase.execute();
        SpaceClosures closures = SpaceClosures.of(getSpaceClosuresUseCase.inForce());
        return getReservationsByUserUseCase.execute(currentUserId(authentication)).stream()
                .map(reservation -> ReservationResponse.from(reservation, config, closures))
                .toList();
    }

    @GetMapping("/{id}")
    public ReservationResponse findById(@PathVariable UUID id, Authentication authentication) {
        Reservation reservation = getReservationByIdUseCase.execute(id);
        if (!isAdmin(authentication) && !reservation.getUserId().equals(currentUserId(authentication))) {
            throw new ReservationNotFoundException(id);
        }
        return toResponse(reservation);
    }

    @PostMapping("/{id}/cancel")
    public ReservationResponse cancel(@PathVariable UUID id, Authentication authentication) {
        boolean isAdmin = isAdmin(authentication);
        Reservation reservation = cancelReservationUseCase.execute(id, currentUserId(authentication), isAdmin);
        return toResponse(reservation);
    }

    private ReservationResponse toResponse(Reservation reservation) {
        return ReservationResponse.from(
                reservation,
                getInstitutionConfigUseCase.execute(),
                SpaceClosures.of(getSpaceClosuresUseCase.inForce()));
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(ADMIN_AUTHORITY::equals);
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
