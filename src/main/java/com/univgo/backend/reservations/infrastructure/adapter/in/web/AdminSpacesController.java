package com.univgo.backend.reservations.infrastructure.adapter.in.web;

import com.univgo.backend.reservations.application.port.in.CancelSpaceReservationsUseCase;
import com.univgo.backend.reservations.application.port.in.GetSpaceBlockDetailUseCase;
import com.univgo.backend.reservations.application.port.in.GetSpaceDayBlocksUseCase;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.CancelSpaceReservationsResponse;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.SpaceBlockDetailResponse;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.SpaceBlockSummaryResponse;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.SpaceMaintenanceRequest;
import com.univgo.backend.spaces.application.port.in.SetSpaceMaintenanceUseCase;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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

    public AdminSpacesController(
            GetSpaceDayBlocksUseCase getSpaceDayBlocksUseCase,
            GetSpaceBlockDetailUseCase getSpaceBlockDetailUseCase,
            SetSpaceMaintenanceUseCase setSpaceMaintenanceUseCase,
            CancelSpaceReservationsUseCase cancelSpaceReservationsUseCase) {
        this.getSpaceDayBlocksUseCase = getSpaceDayBlocksUseCase;
        this.getSpaceBlockDetailUseCase = getSpaceBlockDetailUseCase;
        this.setSpaceMaintenanceUseCase = setSpaceMaintenanceUseCase;
        this.cancelSpaceReservationsUseCase = cancelSpaceReservationsUseCase;
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

    @PutMapping("/{spaceId}/maintenance")
    public ResponseEntity<Void> setMaintenance(@PathVariable UUID spaceId, @Valid @RequestBody SpaceMaintenanceRequest request) {
        setSpaceMaintenanceUseCase.execute(spaceId, request.underMaintenance());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{spaceId}/reservations/cancel-all")
    public CancelSpaceReservationsResponse cancelAll(@PathVariable UUID spaceId) {
        return new CancelSpaceReservationsResponse(cancelSpaceReservationsUseCase.execute(spaceId));
    }
}
