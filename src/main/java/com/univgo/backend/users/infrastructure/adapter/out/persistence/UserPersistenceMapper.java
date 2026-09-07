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
        return new User(
                entity.getId(),
                entity.getIdentification(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPassword(),
                roles);
    }
}
