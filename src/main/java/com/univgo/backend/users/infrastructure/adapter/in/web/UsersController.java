package com.univgo.backend.users.infrastructure.adapter.in.web;

import com.univgo.backend.users.application.port.in.DeleteUserUseCase;
import com.univgo.backend.users.application.port.in.GetAllUsersUseCase;
import com.univgo.backend.users.application.port.in.GetUserByIdUseCase;
import com.univgo.backend.users.application.port.in.UpdateUserUseCase;
import com.univgo.backend.users.application.port.in.UpdateUserUseCase.UpdateUserCommand;
import com.univgo.backend.users.infrastructure.adapter.in.web.dto.UpdateUserRequest;
import com.univgo.backend.users.infrastructure.adapter.in.web.dto.UserResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UsersController {

    private final GetAllUsersUseCase getAllUsersUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;

    public UsersController(
            GetAllUsersUseCase getAllUsersUseCase,
            GetUserByIdUseCase getUserByIdUseCase,
            UpdateUserUseCase updateUserUseCase,
            DeleteUserUseCase deleteUserUseCase) {
        this.getAllUsersUseCase = getAllUsersUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.updateUserUseCase = updateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
    }

    @GetMapping
    public List<UserResponse> findAll() {
        return getAllUsersUseCase.execute().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable UUID id) {
        return UserResponse.from(getUserByIdUseCase.execute(id));
    }

    @PatchMapping("/{id}")
    public UserResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        var command = new UpdateUserCommand(request.firstName(), request.lastName());
        return UserResponse.from(updateUserUseCase.execute(id, command));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deleteUserUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
