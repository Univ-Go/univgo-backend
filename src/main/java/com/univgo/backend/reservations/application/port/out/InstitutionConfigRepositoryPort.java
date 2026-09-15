package com.univgo.backend.reservations.application.port.out;

import com.univgo.backend.reservations.domain.InstitutionConfig;

public interface InstitutionConfigRepositoryPort {

    InstitutionConfig getCurrent();

    InstitutionConfig update(InstitutionConfig config);
}
