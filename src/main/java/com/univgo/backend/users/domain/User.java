package com.univgo.backend.users.domain;

import java.util.Set;
import java.util.UUID;

public class User {

    private final UUID id;
    private final String identification;
    private final String email;
    private String firstName;
    private String lastName;
    private String password;
    private final Set<String> roles;
    private final String school;

    private User(Builder builder) {
        this.id = builder.id;
        this.identification = builder.identification;
        this.email = builder.email;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.password = builder.password;
        this.roles = builder.roles;
        this.school = builder.school;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void rename(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /** Takes an already-hashed password; hashing is the application layer's job. */
    public void changePassword(String passwordHash) {
        this.password = passwordHash;
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

    public static final class Builder {
        private UUID id;
        private String identification;
        private String email;
        private String firstName;
        private String lastName;
        private String password;
        private Set<String> roles;
        private String school;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder identification(String identification) {
            this.identification = identification;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder roles(Set<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder school(String school) {
            this.school = school;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
