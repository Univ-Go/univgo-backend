package com.univgo.backend.reservations.infrastructure.adapter.in.web;

import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.CreateSpaceClosureRequest;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.SpaceClosureResponse;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.SpaceMaintenanceRequest;
import com.univgo.backend.spaces.application.port.in.CloseSpaceUseCase;
import com.univgo.backend.spaces.application.port.in.CloseSpaceUseCase.CloseSpaceCommand;
import com.univgo.backend.spaces.application.port.in.GetSpaceClosuresUseCase;
import com.univgo.backend.spaces.application.port.in.RevertSpaceClosureUseCase;
import com.univgo.backend.spaces.application.port.in.SetSpaceMaintenanceUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Shutting a space and reopening it. Shares {@code /admin/spaces} with {@link AdminSpacesController}. */
@RestController
@RequestMapping("/admin/spaces")
@PreAuthorize("hasRole('ADMIN') or hasRole('GYM_ADMIN')")
public class AdminSpaceClosuresController {

    private final SetSpaceMaintenanceUseCase setSpaceMaintenanceUseCase;
    private final CloseSpaceUseCase closeSpaceUseCase;
    private final RevertSpaceClosureUseCase revertSpaceClosureUseCase;
    private final GetSpaceClosuresUseCase getSpaceClosuresUseCase;

    public AdminSpaceClosuresController(
            SetSpaceMaintenanceUseCase setSpaceMaintenanceUseCase,
            CloseSpaceUseCase closeSpaceUseCase,
            RevertSpaceClosureUseCase revertSpaceClosureUseCase,
            GetSpaceClosuresUseCase getSpaceClosuresUseCase) {
        this.setSpaceMaintenanceUseCase = setSpaceMaintenanceUseCase;
        this.closeSpaceUseCase = closeSpaceUseCase;
        this.revertSpaceClosureUseCase = revertSpaceClosureUseCase;
        this.getSpaceClosuresUseCase = getSpaceClosuresUseCase;
    }

    /** The switch is a closure with no end date, so it is recorded against whoever flipped it. */
    @PreAuthorize("hasRole('ADMIN')")
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
    public List<SpaceClosureResponse> closures(@PathVariable UUID spaceId, Authentication authentication) {
        if (!GymSpaceAccessGuard.permits(authentication, spaceId)) {
            throw new AccessDeniedException("GYM_ADMIN cannot read another space's closure history");
        }
        return getSpaceClosuresUseCase.execute(spaceId).stream()
                .map(SpaceClosureResponse::from)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{spaceId}/closures/{closureId}/revert")
    public SpaceClosureResponse revertClosure(
            @PathVariable UUID spaceId, @PathVariable UUID closureId, Authentication authentication) {
        return SpaceClosureResponse.from(revertSpaceClosureUseCase.execute(closureId, currentUserId(authentication)));
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(authentication.getName());
    }
}
