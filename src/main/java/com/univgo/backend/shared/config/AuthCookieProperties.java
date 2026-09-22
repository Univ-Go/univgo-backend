package com.univgo.backend.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cookie attributes differ per environment and must not be compiled in: development runs over plain
 * HTTP on the same site, production runs cross-site over TLS and therefore needs
 * {@code SameSite=None; Secure}.
 *
 * @param sessionName cookie holding the short-lived access token
 * @param refreshName cookie holding the refresh token, scoped to {@code /auth}
 * @param secure      whether the browser may only send the cookies over TLS
 * @param sameSite    {@code Lax} when the browser and the API share a site, {@code None} otherwise
 * @param domain      explicit cookie domain, or {@code null} to let the browser use the host
 */
@ConfigurationProperties("auth.cookie")
public record AuthCookieProperties(
        String sessionName,
        String refreshName,
        boolean secure,
        String sameSite,
        String domain) {
}
