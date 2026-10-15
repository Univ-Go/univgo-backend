package com.univgo.backend.users.application.port.out;

import java.util.UUID;

/**
 * Ends every open session of a user. Declared here and implemented by the auth module: {@code auth}
 * already depends on {@code users}, so the consumer owns the port rather than closing a cycle.
 */
public interface UserSessionRevokerPort {

    void revokeAllSessions(UUID userId);
}
