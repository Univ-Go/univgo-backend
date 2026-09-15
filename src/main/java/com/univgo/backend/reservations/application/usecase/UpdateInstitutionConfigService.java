package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.UpdateInstitutionConfigUseCase;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import org.springframework.stereotype.Service;

@Service
public class UpdateInstitutionConfigService implements UpdateInstitutionConfigUseCase {

    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;

    public UpdateInstitutionConfigService(InstitutionConfigRepositoryPort institutionConfigRepositoryPort) {
        this.institutionConfigRepositoryPort = institutionConfigRepositoryPort;
    }

    @Override
    public InstitutionConfig execute(InstitutionConfig config) {
        return institutionConfigRepositoryPort.update(config);
    }
}
