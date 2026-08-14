package com.univgo.backend.users.application.port.out;

import com.univgo.backend.users.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {

    List<User> findAll();

    Optional<User> findById(UUID id);

    Optional<User> findByIdentification(String identification);

    User save(User user);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
