package com.univgo.backend.users.infrastructure.adapter.out.persistence;

import com.univgo.backend.shared.domain.Role;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Postgres guarda role_enum en minúscula ('student', 'admin'); Java usa
 * mayúscula por convención. Requiere ?stringtype=unspecified en la JDBC URL
 * para que pgjdbc permita bindear un String plano contra la columna enum nativa.
 */
@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {
        return role == null ? null : role.name().toLowerCase();
    }

    @Override
    public Role convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : Role.valueOf(dbValue.toUpperCase());
    }
}
