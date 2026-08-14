package com.univgo.backend.reservations.infrastructure.adapter.in.web.dto;

import com.univgo.backend.reservations.domain.ReservationGuest;

public record GuestResponse(String identification, String name) {

    public static GuestResponse from(ReservationGuest guest) {
        return new GuestResponse(guest.getIdentification(), guest.getName());
    }
}
