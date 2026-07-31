package com.univgo.backend.users.infrastructure.adapter.out.persistence;

import com.univgo.backend.users.domain.User;

final class UserPersistenceMapper {

    private UserPersistenceMapper() {
    }

    static User toDomain(UserJpaEntity entity) {
        return new User(
                entity.getId(),
                entity.getIdentification(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPassword(),
                entity.getRole());
    }

    static UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.getId(),
                user.getIdentification(),
                user.getFirstName(),
                user.getLastName(),
                user.getPassword(),
                user.getRole());
    }
}
