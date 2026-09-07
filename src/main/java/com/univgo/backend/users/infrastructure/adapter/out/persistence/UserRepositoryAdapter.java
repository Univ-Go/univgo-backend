package com.univgo.backend.users.infrastructure.adapter.out.persistence;

import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import com.univgo.backend.users.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream()
                .map(UserPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userJpaRepository.findById(id).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<User> findByIdentification(String identification) {
        return userJpaRepository.findByIdentification(identification).map(UserPersistenceMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserJpaEntity existing = userJpaRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("User not found: " + user.getId()));
        existing.rename(user.getFirstName(), user.getLastName());
        UserJpaEntity saved = userJpaRepository.save(existing);
        return UserPersistenceMapper.toDomain(saved);
    }

    @Override
    public boolean existsById(UUID id) {
        return userJpaRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        userJpaRepository.deleteById(id);
    }
}
