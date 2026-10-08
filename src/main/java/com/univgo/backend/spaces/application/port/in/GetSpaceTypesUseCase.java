package com.univgo.backend.spaces.application.port.in;

import com.univgo.backend.spaces.domain.SpaceType;
import java.util.List;

public interface GetSpaceTypesUseCase {

    List<SpaceType> execute();
}
