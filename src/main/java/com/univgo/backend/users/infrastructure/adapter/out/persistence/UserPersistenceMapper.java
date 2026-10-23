package com.univgo.backend.users.infrastructure.adapter.out.persistence;

import com.univgo.backend.users.domain.User;
import java.util.Set;
import java.util.stream.Collectors;

final class UserPersistenceMapper {

    private UserPersistenceMapper() {
    }

    static User toDomain(UserJpaEntity entity) {
        Set<String> roles = entity.getRoles().stream()
                .map(RoleJpaEntity::getName)
                .collect(Collectors.toSet());
        return User.builder()
                .id(entity.getId())
                .identification(entity.getIdentification())
                .email(entity.getEmail())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .password(entity.getPassword())
                .roles(roles)
                .school(entity.getSchool())
                .build();
    }
}
