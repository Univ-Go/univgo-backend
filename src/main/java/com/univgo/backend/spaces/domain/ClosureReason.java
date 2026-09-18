package com.univgo.backend.spaces.domain;

/** Why a space stopped operating. The panel offers these five and nothing else. */
public enum ClosureReason {
    MAINTENANCE,
    TECHNICAL_INCIDENT,
    INSTITUTIONAL_EVENT,
    EXTERNAL_USE,
    OTHER
}
