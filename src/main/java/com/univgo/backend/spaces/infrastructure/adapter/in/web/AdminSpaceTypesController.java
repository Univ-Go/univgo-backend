package com.univgo.backend.spaces.infrastructure.adapter.in.web;

import com.univgo.backend.spaces.application.port.in.GetSpaceTypesUseCase;
import com.univgo.backend.spaces.infrastructure.adapter.in.web.dto.SpaceTypeResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only because its only consumer is the space form's type picker. Least privilege costs
 * nothing here: a student is never shown the taxonomy, only the category a space resolved to.
 */
@RestController
@RequestMapping("/admin/space-types")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSpaceTypesController {

    private final GetSpaceTypesUseCase getSpaceTypesUseCase;

    public AdminSpaceTypesController(GetSpaceTypesUseCase getSpaceTypesUseCase) {
        this.getSpaceTypesUseCase = getSpaceTypesUseCase;
    }

    @GetMapping
    public List<SpaceTypeResponse> list() {
        return getSpaceTypesUseCase.execute().stream().map(SpaceTypeResponse::from).toList();
    }
}
