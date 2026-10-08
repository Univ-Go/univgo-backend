package com.univgo.backend.reservations.infrastructure.adapter.in.web;

import com.univgo.backend.reservations.application.port.in.CancelSpaceReservationsUseCase;
import com.univgo.backend.reservations.application.port.in.GetSpaceBlockDetailUseCase;
import com.univgo.backend.reservations.application.port.in.GetSpaceDayBlocksUseCase;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.CancelSpaceReservationsResponse;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.CreateSpaceClosureRequest;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.SpaceBlockDetailResponse;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.SpaceBlockSummaryResponse;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.SpaceClosureResponse;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.SpaceMaintenanceRequest;
import com.univgo.backend.spaces.application.port.in.CloseSpaceUseCase;
import com.univgo.backend.spaces.application.port.in.CloseSpaceUseCase.CloseSpaceCommand;
import com.univgo.backend.spaces.application.port.in.GetSpaceClosuresUseCase;
import com.univgo.backend.spaces.application.port.in.RevertSpaceClosureUseCase;
import com.univgo.backend.spaces.application.port.in.SetSpaceMaintenanceUseCase;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/spaces")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSpacesController {

    private final GetSpaceDayBlocksUseCase getSpaceDayBlocksUseCase;
    private final GetSpaceBlockDetailUseCase getSpaceBlockDetailUseCase;
    private final SetSpaceMaintenanceUseCase setSpaceMaintenanceUseCase;
    private final CancelSpaceReservationsUseCase cancelSpaceReservationsUseCase;
    private final CloseSpaceUseCase closeSpaceUseCase;
    private final RevertSpaceClosureUseCase revertSpaceClosureUseCase;
    private final GetSpaceClosuresUseCase getSpaceClosuresUseCase;

    public AdminSpacesController(
            GetSpaceDayBlocksUseCase getSpaceDayBlocksUseCase,
            GetSpaceBlockDetailUseCase getSpaceBlockDetailUseCase,
            SetSpaceMaintenanceUseCase setSpaceMaintenanceUseCase,
            CancelSpaceReservationsUseCase cancelSpaceReservationsUseCase,
            CloseSpaceUseCase closeSpaceUseCase,
            RevertSpaceClosureUseCase revertSpaceClosureUseCase,
            GetSpaceClosuresUseCase getSpaceClosuresUseCase) {
        this.getSpaceDayBlocksUseCase = getSpaceDayBlocksUseCase;
        this.getSpaceBlockDetailUseCase = getSpaceBlockDetailUseCase;
        this.setSpaceMaintenanceUseCase = setSpaceMaintenanceUseCase;
        this.cancelSpaceReservationsUseCase = cancelSpaceReservationsUseCase;
        this.closeSpaceUseCase = closeSpaceUseCase;
        this.revertSpaceClosureUseCase = revertSpaceClosureUseCase;
        this.getSpaceClosuresUseCase = getSpaceClosuresUseCase;
    }

    @GetMapping("/{spaceId}/blocks")
    public List<SpaceBlockSummaryResponse> blocks(
            @PathVariable UUID spaceId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return getSpaceDayBlocksUseCase.execute(spaceId, date).stream().map(SpaceBlockSummaryResponse::from).toList();
    }

    @GetMapping("/{spaceId}/blocks/{blockStart}")
    public SpaceBlockDetailResponse blockDetail(
            @PathVariable UUID spaceId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime blockStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return SpaceBlockDetailResponse.from(getSpaceBlockDetailUseCase.execute(spaceId, date, blockStart));
    }

    /** The switch is a closure with no end date, so it is recorded against whoever flipped it. */
    @PutMapping("/{spaceId}/maintenance")
    public ResponseEntity<Void> setMaintenance(
            @PathVariable UUID spaceId,
            @Valid @RequestBody SpaceMaintenanceRequest request,
            Authentication authentication) {
        setSpaceMaintenanceUseCase.execute(spaceId, request.underMaintenance(), currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    /** The whole history, reverted closures included: that a space was shut and reopened is a fact. */
    @GetMapping("/{spaceId}/closures")
    public List<SpaceClosureResponse> closures(@PathVariable UUID spaceId) {
        return getSpaceClosuresUseCase.execute(spaceId).stream()
                .map(SpaceClosureResponse::from)
                .toList();
    }

    @PostMapping("/{spaceId}/closures")
    public SpaceClosureResponse close(
            @PathVariable UUID spaceId,
            @Valid @RequestBody CreateSpaceClosureRequest request,
            Authentication authentication) {
        return SpaceClosureResponse.from(closeSpaceUseCase.execute(new CloseSpaceCommand(
                spaceId,
                request.startsAt(),
                request.endsAt(),
                request.reason(),
                request.details(),
                currentUserId(authentication))));
    }

    /**
     * Reopens the space. Nothing is written back to the reservations it suspended: they were never
     * touched, so they read as reserved again the moment the closure stops being in force.
     */
    @PostMapping("/{spaceId}/closures/{closureId}/revert")
    public SpaceClosureResponse revertClosure(
            @PathVariable UUID spaceId, @PathVariable UUID closureId, Authentication authentication) {
        return SpaceClosureResponse.from(revertSpaceClosureUseCase.execute(closureId, currentUserId(authentication)));
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }

    @PostMapping("/{spaceId}/reservations/cancel-all")
    public CancelSpaceReservationsResponse cancelAll(@PathVariable UUID spaceId) {
        return new CancelSpaceReservationsResponse(cancelSpaceReservationsUseCase.execute(spaceId));
    }
}
