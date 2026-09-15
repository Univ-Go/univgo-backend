package com.univgo.backend.reservations.application.usecase;

import com.univgo.backend.reservations.application.port.in.GetInstitutionConfigUseCase;
import com.univgo.backend.reservations.application.port.out.InstitutionConfigRepositoryPort;
import com.univgo.backend.reservations.domain.InstitutionConfig;
import org.springframework.stereotype.Service;

@Service
public class GetInstitutionConfigService implements GetInstitutionConfigUseCase {

    private final InstitutionConfigRepositoryPort institutionConfigRepositoryPort;

    public GetInstitutionConfigService(InstitutionConfigRepositoryPort institutionConfigRepositoryPort) {
        this.institutionConfigRepositoryPort = institutionConfigRepositoryPort;
    }

    @Override
    public InstitutionConfig execute() {
        return institutionConfigRepositoryPort.getCurrent();
    }
}
