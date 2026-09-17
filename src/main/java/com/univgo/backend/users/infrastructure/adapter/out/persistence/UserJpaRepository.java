package com.univgo.backend.users.infrastructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {

    /**
     * Sign-in accepts the ID number or the institutional email in a single field, so both are
     * resolved in one query rather than probing twice and leaking which of the two matched.
     */
    @Query("SELECT u FROM UserJpaEntity u WHERE u.identification = :value OR LOWER(u.email) = LOWER(:value)")
    Optional<UserJpaEntity> findByLoginIdentifier(@Param("value") String value);
}
