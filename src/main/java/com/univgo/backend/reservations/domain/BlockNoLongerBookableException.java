package com.univgo.backend.reservations.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class BlockNoLongerBookableException extends RuntimeException {

    public BlockNoLongerBookableException(UUID spaceId, LocalDate date, LocalTime start, LocalTime end) {
        super("Space " + spaceId + "'s " + start + "-" + end + " block on " + date
                + " no longer leaves enough time to meet the minimum usage guarantee");
    }
}
