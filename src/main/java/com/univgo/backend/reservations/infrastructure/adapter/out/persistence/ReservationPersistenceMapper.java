package com.univgo.backend.reservations.infrastructure.adapter.out.persistence;

import com.univgo.backend.reservations.domain.Reservation;
import com.univgo.backend.reservations.domain.ReservationGuest;
import java.util.List;

final class ReservationPersistenceMapper {

    private ReservationPersistenceMapper() {
    }

    static Reservation toDomain(ReservationJpaEntity entity) {
        List<ReservationGuest> guests = entity.getGuests().stream()
                .map(guest -> new ReservationGuest(guest.getId(), guest.getGuestIdentification(), guest.getGuestName()))
                .toList();

        return new Reservation(
                entity.getId(),
                entity.getQrCodeData(),
                entity.getUserId(),
                entity.getSpaceId(),
                entity.getReservationDate(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                guests);
    }

    static ReservationJpaEntity toEntity(Reservation reservation) {
        ReservationJpaEntity entity = new ReservationJpaEntity(
                reservation.getId(),
                reservation.getQrCodeData(),
                reservation.getUserId(),
                reservation.getSpaceId(),
                reservation.getReservationDate(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt());

        reservation.getGuests().forEach(guest -> entity.addGuest(
                new ReservationGuestJpaEntity(guest.getId(), entity, guest.getIdentification(), guest.getName())));

        return entity;
    }
}
