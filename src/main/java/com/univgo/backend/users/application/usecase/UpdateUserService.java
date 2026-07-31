package com.univgo.backend.users.application.usecase;

import com.univgo.backend.users.application.port.in.UpdateUserUseCase;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import com.univgo.backend.users.domain.User;
import com.univgo.backend.users.domain.UserNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class UpdateUserService implements UpdateUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public UpdateUserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public User execute(UUID id, UpdateUserCommand command) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        String firstName = command.firstName() != null ? command.firstName() : user.getFirstName();
        String lastName = command.lastName() != null ? command.lastName() : user.getLastName();
        user.rename(firstName, lastName);

        return userRepositoryPort.save(user);
    }
}
