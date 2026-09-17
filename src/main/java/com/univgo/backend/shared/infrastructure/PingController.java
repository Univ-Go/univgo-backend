package com.univgo.backend.shared.infrastructure;

import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The free hosting plan suspends the instance after a stretch without traffic, and waking it again
 * costs the first visitor most of a minute. An external scheduler calls this endpoint to keep the
 * instance awake, so the answer deliberately touches neither the database nor the session: the call
 * must stay cheap enough to run every few minutes.
 */
@RestController
public class PingController {

    /**
     * @param status always {@code ok}; reaching the method at all is the actual signal
     * @param time   server clock, which makes a stale cache in front of the API visible
     */
    public record PingResponse(String status, Instant time) {
    }

    @GetMapping("/ping")
    public PingResponse ping() {
        return new PingResponse("ok", Instant.now());
    }
}
