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

    // ponytail: system zone, matching the rest of the app; switch to a fixed ZoneId (e.g.
    // America/Bogota) here once the institution's timezone is decided app-wide.
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
