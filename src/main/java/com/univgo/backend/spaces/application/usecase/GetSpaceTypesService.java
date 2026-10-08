package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.spaces.application.port.in.GetSpaceTypesUseCase;
import com.univgo.backend.spaces.application.port.out.SpaceTypeRepositoryPort;
import com.univgo.backend.spaces.domain.SpaceType;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GetSpaceTypesService implements GetSpaceTypesUseCase {

    private final SpaceTypeRepositoryPort spaceTypeRepositoryPort;

    public GetSpaceTypesService(SpaceTypeRepositoryPort spaceTypeRepositoryPort) {
        this.spaceTypeRepositoryPort = spaceTypeRepositoryPort;
    }

    @Override
    public List<SpaceType> execute() {
        return spaceTypeRepositoryPort.findAll();
    }
}
