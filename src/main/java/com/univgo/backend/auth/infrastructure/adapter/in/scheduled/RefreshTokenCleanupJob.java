package com.univgo.backend.auth.infrastructure.adapter.in.scheduled;

import com.univgo.backend.auth.application.port.in.PurgeStaleRefreshTokensUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenCleanupJob.class);

    private final PurgeStaleRefreshTokensUseCase purgeStaleRefreshTokensUseCase;

    public RefreshTokenCleanupJob(PurgeStaleRefreshTokensUseCase purgeStaleRefreshTokensUseCase) {
        this.purgeStaleRefreshTokensUseCase = purgeStaleRefreshTokensUseCase;
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void purgeStaleRefreshTokens() {
        int deleted = purgeStaleRefreshTokensUseCase.execute();
        if (deleted > 0) {
            log.info("Purged {} stale refresh_tokens rows", deleted);
        }
    }
}
