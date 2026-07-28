package com.univgo.backend.users.application.port.in;

import com.univgo.backend.users.domain.User;
import java.util.UUID;

public interface UpdateUserUseCase {

    User execute(UUID id, UpdateUserCommand command);

    record UpdateUserCommand(String firstName, String lastName) {
    }
}
