package com.univgo.backend.users.application.port.in;

import java.util.UUID;

public interface DeleteUserUseCase {

    void execute(UUID id);
}
