package com.univgo.backend.shared.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The server's default zone, which is what every bare {@code LocalDateTime.now()} in the app already
 * reads. Naming it as a bean makes that choice explicit and lets code read the same clock tests can
 * replace.
 */
@Configuration
public class ClockConfig {

    // The default zone is the institution's (`univgo.timezone`), set in Application before any bean.
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
