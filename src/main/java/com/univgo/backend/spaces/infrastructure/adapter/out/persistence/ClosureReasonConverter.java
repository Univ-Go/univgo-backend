package com.univgo.backend.spaces.infrastructure.adapter.out.persistence;

import com.univgo.backend.spaces.domain.ClosureReason;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ClosureReasonConverter implements AttributeConverter<ClosureReason, String> {

    @Override
    public String convertToDatabaseColumn(ClosureReason reason) {
        return reason == null ? null : reason.name().toLowerCase();
    }

    @Override
    public ClosureReason convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : ClosureReason.valueOf(dbValue.toUpperCase());
    }
}
