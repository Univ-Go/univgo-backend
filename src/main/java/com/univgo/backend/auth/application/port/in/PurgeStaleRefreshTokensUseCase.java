package com.univgo.backend.auth.application.port.in;

public interface PurgeStaleRefreshTokensUseCase {

    /** Deletes expired/stale-revoked refresh token rows. Returns rows deleted. */
    int execute();
}
