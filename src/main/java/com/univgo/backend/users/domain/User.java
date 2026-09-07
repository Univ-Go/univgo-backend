package com.univgo.backend.users.domain;

import java.util.Set;
import java.util.UUID;

public class User {

    private final UUID id;
    private final String identification;
    private String firstName;
    private String lastName;
    private final String password;
    private final Set<String> roles;

    public User(UUID id, String identification, String firstName, String lastName, String password, Set<String> roles) {
        this.id = id;
        this.identification = identification;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.roles = roles;
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

    public Set<String> getRoles() {
        return roles;
    }
}
