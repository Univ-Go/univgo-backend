package com.univgo.backend.reservations.infrastructure.adapter.in.web;

import com.univgo.backend.reservations.application.port.in.GetInstitutionConfigUseCase;
import com.univgo.backend.reservations.application.port.in.UpdateInstitutionConfigUseCase;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.InstitutionConfigRequest;
import com.univgo.backend.reservations.infrastructure.adapter.in.web.dto.InstitutionConfigResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/institution-config")
@PreAuthorize("hasRole('ADMIN')")
public class AdminInstitutionConfigController {

    private final GetInstitutionConfigUseCase getInstitutionConfigUseCase;
    private final UpdateInstitutionConfigUseCase updateInstitutionConfigUseCase;

    public AdminInstitutionConfigController(
            GetInstitutionConfigUseCase getInstitutionConfigUseCase,
            UpdateInstitutionConfigUseCase updateInstitutionConfigUseCase) {
        this.getInstitutionConfigUseCase = getInstitutionConfigUseCase;
        this.updateInstitutionConfigUseCase = updateInstitutionConfigUseCase;
    }

    @GetMapping
    public InstitutionConfigResponse get() {
        return InstitutionConfigResponse.from(getInstitutionConfigUseCase.execute());
    }

    @PutMapping
    public InstitutionConfigResponse update(@Valid @RequestBody InstitutionConfigRequest request) {
        return InstitutionConfigResponse.from(updateInstitutionConfigUseCase.execute(request.toDomain()));
    }
}
