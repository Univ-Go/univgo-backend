package com.univgo.backend.users.domain;

import com.univgo.backend.shared.domain.Role;
import java.util.UUID;

public class User {

    private final UUID id;
    private final String identification;
    private String firstName;
    private String lastName;
    private final String password;
    private final Role role;

    public User(UUID id, String identification, String firstName, String lastName, String password, Role role) {
        this.id = id;
        this.identification = identification;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.role = role;
    }

    public void rename(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getId() {
        return id;
    }

    public String getIdentification() {
        return identification;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}
