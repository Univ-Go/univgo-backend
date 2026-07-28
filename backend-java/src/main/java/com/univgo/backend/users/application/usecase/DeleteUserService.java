package com.univgo.backend.users.application.usecase;

import com.univgo.backend.users.application.port.in.DeleteUserUseCase;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import com.univgo.backend.users.domain.UserNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class DeleteUserService implements DeleteUserUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public DeleteUserService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public void execute(UUID id) {
        if (!userRepositoryPort.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepositoryPort.deleteById(id);
    }
}
