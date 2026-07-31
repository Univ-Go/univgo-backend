package com.univgo.backend.reservations.infrastructure.adapter.out.persistence;

import com.univgo.backend.reservations.domain.ReservationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ReservationStatusConverter implements AttributeConverter<ReservationStatus, String> {

    @Override
    public String convertToDatabaseColumn(ReservationStatus status) {
        return status == null ? null : status.name().toLowerCase();
    }

    @Override
    public ReservationStatus convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : ReservationStatus.valueOf(dbValue.toUpperCase());
    }
}
