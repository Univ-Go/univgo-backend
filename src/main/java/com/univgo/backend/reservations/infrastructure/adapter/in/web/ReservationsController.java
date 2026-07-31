package com.univgo.backend.reservations.infrastructure.adapter.in.web;

import com.univgo.backend.reservations.application.port.in.CreateReservationUseCase;
import com.univgo.backend.reservations.application.port.in.CreateReservationUseCase.CreateReservationCommand;
import com.univgo.backend.reservations.application.port.in.DeleteReservationUseCase;
import com.univgo.backend.reservations.application.port.in.GetAllReservationsUseCase;
import com.univgo.backend.reservations.application.port.in.GetReservationByIdUseCase;
import com.univgo.backend.reservations.application.port.in.GetReservationsByUserUseCase;
import com.univgo.backend.reservations.application.port.in.UpdateReservationStatusUseCase;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.CreateReservationRequest;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.ReservationResponse;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.UpdateReservationStatusRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservations")
public class ReservationsController {

    private final CreateReservationUseCase createReservationUseCase;
    private final GetAllReservationsUseCase getAllReservationsUseCase;
    private final GetReservationsByUserUseCase getReservationsByUserUseCase;
    private final GetReservationByIdUseCase getReservationByIdUseCase;
    private final UpdateReservationStatusUseCase updateReservationStatusUseCase;
    private final DeleteReservationUseCase deleteReservationUseCase;

    public ReservationsController(
            CreateReservationUseCase createReservationUseCase,
            GetAllReservationsUseCase getAllReservationsUseCase,
            GetReservationsByUserUseCase getReservationsByUserUseCase,
            GetReservationByIdUseCase getReservationByIdUseCase,
            UpdateReservationStatusUseCase updateReservationStatusUseCase,
            DeleteReservationUseCase deleteReservationUseCase) {
        this.createReservationUseCase = createReservationUseCase;
        this.getAllReservationsUseCase = getAllReservationsUseCase;
        this.getReservationsByUserUseCase = getReservationsByUserUseCase;
        this.getReservationByIdUseCase = getReservationByIdUseCase;
        this.updateReservationStatusUseCase = updateReservationStatusUseCase;
        this.deleteReservationUseCase = deleteReservationUseCase;
    }

    @PostMapping
    public ReservationResponse create(
            @Valid @RequestBody CreateReservationRequest request, Authentication authentication) {
        var command = new CreateReservationCommand(
                currentUserId(authentication),
                request.spaceId(),
                request.reservationDate(),
                request.startTime(),
                request.endTime(),
                request.guestIdentifications());
        return ReservationResponse.from(createReservationUseCase.execute(command));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ReservationResponse> findAll() {
        return getAllReservationsUseCase.execute().stream().map(ReservationResponse::from).toList();
    }

    @GetMapping("/me")
    public List<ReservationResponse> findMine(Authentication authentication) {
        return getReservationsByUserUseCase.execute(currentUserId(authentication)).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public ReservationResponse findById(@PathVariable UUID id) {
        return ReservationResponse.from(getReservationByIdUseCase.execute(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ReservationResponse updateStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdateReservationStatusRequest request) {
        return ReservationResponse.from(updateReservationStatusUseCase.execute(id, request.status()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteReservationUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
