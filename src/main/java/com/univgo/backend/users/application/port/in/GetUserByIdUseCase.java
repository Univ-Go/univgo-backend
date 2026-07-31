package com.univgo.backend.users.application.port.in;

import com.univgo.backend.users.domain.User;
import java.util.UUID;

public interface GetUserByIdUseCase {

    User execute(UUID id);
}
