package com.univgo.backend.reservations.application.port.in;

import com.univgo.backend.reservations.domain.SpaceBlockSummary;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetSpaceDayBlocksUseCase {

    List<SpaceBlockSummary> execute(UUID spaceId, LocalDate date);
}
