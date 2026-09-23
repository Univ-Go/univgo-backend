package com.univgo.backend.reservations.domain;

import com.univgo.backend.spaces.domain.SpaceClosures;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Everything {@link BlockAvailabilityPolicy} needs to judge one block, gathered once per request. */
public record AvailabilityContext(
        LocalDate date,
        LocalDateTime now,
        InstitutionConfig config,
        StudentDay studentDay,
        BlockReservations reservations,
        SpaceClosures closures) {}
