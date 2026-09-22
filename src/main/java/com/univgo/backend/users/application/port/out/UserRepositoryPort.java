package com.univgo.backend.users.application.port.out;

import com.univgo.backend.users.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    List<User> findAll();

    Optional<User> findById(UUID id);

    /** Resolves a sign-in identifier, which may be the ID number or the institutional email. */
    Optional<User> findByLoginIdentifier(String identifier);

    User save(User user);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
