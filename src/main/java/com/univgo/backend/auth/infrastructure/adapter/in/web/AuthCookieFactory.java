package com.univgo.backend.auth.infrastructure.adapter.in.web;

import com.univgo.backend.auth.application.port.out.TokenProviderPort;
import com.univgo.backend.shared.config.AuthCookieProperties;
import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * Builds the two cookies that carry the session. Both are {@code HttpOnly}: script running in the
 * page can neither read nor forge them, which is the whole reason the tokens left the response body.
 */
@Component
public class AuthCookieFactory {

    /**
     * The refresh cookie is only ever presented to {@code /auth/refresh} and {@code /auth/logout},
     * so it does not ride along on every catalogue or reservation request.
     */
    private static final String REFRESH_PATH = "/auth";

    private final AuthCookieProperties properties;
    private final TokenProviderPort tokenProvider;

    public AuthCookieFactory(AuthCookieProperties properties, TokenProviderPort tokenProvider) {
        this.properties = properties;
        this.tokenProvider = tokenProvider;
    }

    public String sessionName() {
        return properties.sessionName();
    }

    public String refreshName() {
        return properties.refreshName();
    }

    public ResponseCookie session(String accessToken) {
        return base(properties.sessionName(), accessToken, "/")
                .maxAge(Duration.ofSeconds(tokenProvider.getAccessExpirationSeconds()))
                .build();
    }

    public ResponseCookie refresh(String refreshToken) {
        return base(properties.refreshName(), refreshToken, REFRESH_PATH)
                .maxAge(Duration.ofSeconds(tokenProvider.getRefreshExpirationSeconds()))
                .build();
    }

    public ResponseCookie clearedSession() {
        return base(properties.sessionName(), "", "/").maxAge(Duration.ZERO).build();
    }

    public ResponseCookie clearedRefresh() {
        return base(properties.refreshName(), "", REFRESH_PATH).maxAge(Duration.ZERO).build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String name, String value, String path) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(properties.secure())
                .sameSite(properties.sameSite())
                .path(path);
        // An empty domain would be sent literally and match nothing; omitting it lets the browser
        // scope the cookie to the host that set it, which is what a single-host deployment wants.
        if (properties.domain() != null && !properties.domain().isBlank()) {
            builder.domain(properties.domain());
        }
        return builder;
    }
}
