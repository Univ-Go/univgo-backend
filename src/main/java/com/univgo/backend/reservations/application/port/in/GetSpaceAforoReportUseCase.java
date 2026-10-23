package com.univgo.backend.reservations.application.port.in;

import com.univgo.backend.reservations.domain.BlockAforoRow;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetSpaceAforoReportUseCase {

    AforoReport execute(UUID spaceId, LocalDate date);

    record AforoReport(String spaceName, LocalDate date, List<BlockAforoRow> rows) {
    }
}
