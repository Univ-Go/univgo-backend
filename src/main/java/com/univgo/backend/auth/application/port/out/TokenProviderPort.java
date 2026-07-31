package com.univgo.backend.auth.application.port.out;

import com.univgo.backend.users.domain.User;

public interface TokenProviderPort {

    String generateToken(User user);

    long getExpirationSeconds();
}
