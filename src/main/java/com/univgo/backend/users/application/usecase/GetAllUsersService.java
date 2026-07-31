package com.univgo.backend.users.application.usecase;

import com.univgo.backend.users.application.port.in.GetAllUsersUseCase;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import com.univgo.backend.users.domain.User;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GetAllUsersService implements GetAllUsersUseCase {

    private final UserRepositoryPort userRepositoryPort;

    public GetAllUsersService(UserRepositoryPort userRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
    }

    @Override
    public List<User> execute() {
        return userRepositoryPort.findAll();
    }
}
