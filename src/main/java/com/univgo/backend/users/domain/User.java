package com.univgo.backend.users.domain;

import java.util.Set;
import java.util.UUID;

public class User {

    private final UUID id;
    private final String identification;
    private final String email;
    private String firstName;
    private String lastName;
    private final String password;
    private final Set<String> roles;
    private final String school;

    public User(
            UUID id,
            String identification,
            String email,
            String firstName,
            String lastName,
            String password,
            Set<String> roles,
            String school) {
        this.id = id;
        this.identification = identification;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.roles = roles;
        this.school = school;
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

    public String getEmail() {
        return email;
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

    public String getSchool() {
        return school;
    }
}
