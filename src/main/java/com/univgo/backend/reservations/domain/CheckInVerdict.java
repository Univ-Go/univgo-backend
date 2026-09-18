package com.univgo.backend.reservations.domain;

/** The unambiguous answers the admin's scan screen must give, per the spec. */
public enum CheckInVerdict {
    VALID,
    /** The space is not operating at this hour: the door is shut, so nobody is let in. */
    SPACE_CLOSED,
    TOO_EARLY,
    EXPIRED_ALREADY,
    ALREADY_USED,
    OTHER_BLOCK,
    NOT_EXISTS
}
