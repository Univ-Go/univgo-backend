package com.univgo.backend.reservations.application.port.in;

import com.univgo.backend.reservations.domain.InstitutionConfig;

public interface GetInstitutionConfigUseCase {

    InstitutionConfig execute();
}
