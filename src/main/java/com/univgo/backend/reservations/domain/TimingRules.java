package com.univgo.backend.reservations.domain;

import java.time.Duration;

/** The two institution-wide durations {@link ReservationTimingCalculator} needs to place a reservation in time. */
public record TimingRules(Duration tolerance, Duration minUsage) {}
