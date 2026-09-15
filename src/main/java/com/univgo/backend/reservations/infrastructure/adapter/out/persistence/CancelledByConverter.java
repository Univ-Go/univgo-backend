package com.univgo.backend.reservations.infrastructure.adapter.out.persistence;

import com.univgo.backend.reservations.domain.CancelledBy;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CancelledByConverter implements AttributeConverter<CancelledBy, String> {

    @Override
    public String convertToDatabaseColumn(CancelledBy cancelledBy) {
        return cancelledBy == null ? null : cancelledBy.name().toLowerCase();
    }

    @Override
    public CancelledBy convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : CancelledBy.valueOf(dbValue.toUpperCase());
    }
}
