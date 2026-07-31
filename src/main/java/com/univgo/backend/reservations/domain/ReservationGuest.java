package com.univgo.backend.reservations.domain;

import java.util.UUID;

public class ReservationGuest {

    private final UUID id;
    private final String identification;
    private final String name;

    public ReservationGuest(UUID id, String identification, String name) {
        this.id = id;
        this.identification = identification;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getIdentification() {
        return identification;
    }

    public String getName() {
        return name;
    }
}
