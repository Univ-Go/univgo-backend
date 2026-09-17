package com.univgo.backend.reservations.infrastructure.adapter.in.web;

import com.univgo.backend.reservations.application.port.in.GetSpaceAvailabilityUseCase;
import com.univgo.backend.reservations.application.port.in.GetSpaceCatalogUseCase;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.BlockAvailabilityResponse;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.SpaceCatalogResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spaces")
public class SpacesController {

    private final GetSpaceCatalogUseCase getSpaceCatalogUseCase;
    private final GetSpaceAvailabilityUseCase getSpaceAvailabilityUseCase;

    public SpacesController(GetSpaceCatalogUseCase getSpaceCatalogUseCase, GetSpaceAvailabilityUseCase getSpaceAvailabilityUseCase) {
        this.getSpaceCatalogUseCase = getSpaceCatalogUseCase;
        this.getSpaceAvailabilityUseCase = getSpaceAvailabilityUseCase;
    }

    /** The day defaults to today: browsing the catalog without asking for a date means "now". */
    @GetMapping
    public List<SpaceCatalogResponse> catalog(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate requested = date != null ? date : LocalDate.now();
        return getSpaceCatalogUseCase.execute(requested).stream().map(SpaceCatalogResponse::from).toList();
    }

    @GetMapping("/{id}/availability")
    public List<BlockAvailabilityResponse> availability(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication) {
        UUID requestingUserId = UUID.fromString(authentication.getName());
        return getSpaceAvailabilityUseCase.execute(id, date, requestingUserId).stream()
                .map(BlockAvailabilityResponse::from)
                .toList();
    }
}
