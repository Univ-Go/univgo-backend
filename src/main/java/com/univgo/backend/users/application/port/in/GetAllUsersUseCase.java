package com.univgo.backend.users.application.port.in;

import com.univgo.backend.users.domain.User;
import java.util.List;

public interface GetAllUsersUseCase {

    List<User> execute();
}
