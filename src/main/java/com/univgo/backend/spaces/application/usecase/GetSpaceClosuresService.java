package com.univgo.backend.spaces.application.usecase;

import com.univgo.backend.spaces.application.port.in.GetSpaceClosuresUseCase;
import com.univgo.backend.spaces.application.port.out.SpaceClosureRepositoryPort;
import com.univgo.backend.spaces.domain.SpaceClosure;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class GetSpaceClosuresService implements GetSpaceClosuresUseCase {

    private final SpaceClosureRepositoryPort spaceClosureRepositoryPort;

    public GetSpaceClosuresService(SpaceClosureRepositoryPort spaceClosureRepositoryPort) {
        this.spaceClosureRepositoryPort = spaceClosureRepositoryPort;
    }

    @Override
    public List<SpaceClosure> execute(UUID spaceId) {
        return spaceClosureRepositoryPort.findBySpaceId(spaceId);
    }

    @Override
    public List<SpaceClosure> inForce() {
        return spaceClosureRepositoryPort.findAllInForce();
    }
}
