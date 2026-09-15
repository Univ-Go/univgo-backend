package com.univgo.backend.reservations.application.port.in;

import com.univgo.backend.reservations.domain.SpaceCatalogItem;
import java.util.List;

public interface GetSpaceCatalogUseCase {

    List<SpaceCatalogItem> execute();
}
