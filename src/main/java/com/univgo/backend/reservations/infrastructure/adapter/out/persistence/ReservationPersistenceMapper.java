package com.univgo.backend.reservations.infrastructure.adapter.out.persistence;

import com.univgo.backend.reservations.domain.Reservation;

final class ReservationPersistenceMapper {

    private ReservationPersistenceMapper() {
    }

    static Reservation toDomain(ReservationJpaEntity entity) {
        return Reservation.builder()
                .id(entity.getId())
                .qrCodeData(entity.getQrCodeData())
                .confirmationCode(entity.getConfirmationCode())
                .userId(entity.getUserId())
                .spaceId(entity.getSpaceId())
                .reservationDate(entity.getReservationDate())
                .blockStart(entity.getStartTime())
                .blockEnd(entity.getEndTime())
                .createdAt(entity.getCreatedAt())
                .checkedInAt(entity.getCheckedInAt())
                .cancelledAt(entity.getCancelledAt())
                .cancelledBy(entity.getCancelledBy())
                .build();
    }

    static ReservationJpaEntity toEntity(Reservation reservation) {
        return ReservationJpaEntity.builder()
                .id(reservation.getId())
                .qrCodeData(reservation.getQrCodeData())
                .confirmationCode(reservation.getConfirmationCode())
                .userId(reservation.getUserId())
                .spaceId(reservation.getSpaceId())
                .reservationDate(reservation.getReservationDate())
                .startTime(reservation.getBlockStart())
                .endTime(reservation.getBlockEnd())
                .createdAt(reservation.getCreatedAt())
                .checkedInAt(reservation.getCheckedInAt())
                .cancelledAt(reservation.getCancelledAt())
                .cancelledBy(reservation.getCancelledBy())
                .build();
    }
}
