package com.univgo.backend.reservations.domain;

import com.univgo.backend.spaces.domain.TimeBlock;
import java.util.List;

/**
 * One block of the aforo report: its figures, and who booked it — cancelled reservations included,
 * since the report counts them too.
 */
public record BlockAforoRow(
        TimeBlock block,
        int capacity,
        int created,
        int checkedIn,
        int expired,
        int cancelled,
        int free,
        List<OccupantView> roster) {
}
