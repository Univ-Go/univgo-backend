package com.univgo.backend.reservations.infrastructure.adapter.in.web;

import com.univgo.backend.reservations.application.port.in.CancelSpaceReservationsUseCase;
import com.univgo.backend.reservations.application.port.in.GetSpaceBlockDetailUseCase;
import com.univgo.backend.reservations.application.port.in.GetSpaceBlocksRangeSummaryUseCase;
import com.univgo.backend.reservations.application.port.in.GetSpaceDayBlocksUseCase;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.CancelSpaceReservationsResponse;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.SpaceBlockDetailResponse;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.SpaceBlockSummaryResponse;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.SpaceDaySummaryResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/spaces")
@PreAuthorize("hasRole('ADMIN') or hasRole('GYM_ADMIN')")
public class AdminSpacesController {

    private final GetSpaceDayBlocksUseCase getSpaceDayBlocksUseCase;
    private final GetSpaceBlocksRangeSummaryUseCase getSpaceBlocksRangeSummaryUseCase;
    private final GetSpaceBlockDetailUseCase getSpaceBlockDetailUseCase;
    private final CancelSpaceReservationsUseCase cancelSpaceReservationsUseCase;

    public AdminSpacesController(
            GetSpaceDayBlocksUseCase getSpaceDayBlocksUseCase,
            GetSpaceBlocksRangeSummaryUseCase getSpaceBlocksRangeSummaryUseCase,
            GetSpaceBlockDetailUseCase getSpaceBlockDetailUseCase,
            CancelSpaceReservationsUseCase cancelSpaceReservationsUseCase) {
        this.getSpaceDayBlocksUseCase = getSpaceDayBlocksUseCase;
        this.getSpaceBlocksRangeSummaryUseCase = getSpaceBlocksRangeSummaryUseCase;
        this.getSpaceBlockDetailUseCase = getSpaceBlockDetailUseCase;
        this.cancelSpaceReservationsUseCase = cancelSpaceReservationsUseCase;
    }

    @GetMapping("/{spaceId}/blocks")
    public List<SpaceBlockSummaryResponse> blocks(
            @PathVariable UUID spaceId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return getSpaceDayBlocksUseCase.execute(spaceId, date).stream().map(SpaceBlockSummaryResponse::from).toList();
    }

    /** Up to 14 days in one call — replaces fetching one day at a time for a week view. */
    @GetMapping("/{spaceId}/blocks/summary")
    public List<SpaceDaySummaryResponse> blocksSummary(
            @PathVariable UUID spaceId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) {
        if (!GymSpaceAccessGuard.permits(authentication, spaceId)) {
            throw new AccessDeniedException("GYM_ADMIN cannot read another space's roster");
        }
        return getSpaceBlocksRangeSummaryUseCase.execute(spaceId, from, to).stream()
                .map(SpaceDaySummaryResponse::from)
                .toList();
    }

    @GetMapping("/{spaceId}/blocks/{blockStart}")
    public SpaceBlockDetailResponse blockDetail(
            @PathVariable UUID spaceId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime blockStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        if (!GymSpaceAccessGuard.permits(authentication, spaceId)) {
            throw new AccessDeniedException("GYM_ADMIN cannot read another space's roster");
        }
        return SpaceBlockDetailResponse.from(getSpaceBlockDetailUseCase.execute(spaceId, date, blockStart));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{spaceId}/reservations/cancel-all")
    public CancelSpaceReservationsResponse cancelAll(@PathVariable UUID spaceId) {
        return new CancelSpaceReservationsResponse(cancelSpaceReservationsUseCase.execute(spaceId));
    }
}
