package com.univgo.backend.users.infrastructure.adapter.in.web.dto;

import com.univgo.backend.users.domain.User;
import java.util.Set;
import java.util.UUID;

public record UserResponse(UUID id, String identification, String firstName, String lastName, Set<String> roles) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getIdentification(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles());
    }
}
