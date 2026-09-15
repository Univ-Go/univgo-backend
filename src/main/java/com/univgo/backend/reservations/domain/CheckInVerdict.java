package com.univgo.backend.reservations.domain;

/** The six unambiguous answers the admin's scan screen must give, per the spec. */
public enum CheckInVerdict {
    VALID,
    TOO_EARLY,
    EXPIRED_ALREADY,
    ALREADY_USED,
    OTHER_BLOCK,
    NOT_EXISTS
}
