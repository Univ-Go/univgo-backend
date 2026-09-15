package com.univgo.backend.reservations.application.port.in;

import com.univgo.backend.reservations.domain.BlockAvailability;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetSpaceAvailabilityUseCase {

    List<BlockAvailability> execute(UUID spaceId, LocalDate date, UUID requestingUserId);
}
