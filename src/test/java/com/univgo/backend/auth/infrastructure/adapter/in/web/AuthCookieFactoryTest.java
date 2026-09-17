package com.univgo.backend.auth.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.univgo.backend.auth.application.port.out.TokenProviderPort;
import com.univgo.backend.shared.config.AuthCookieProperties;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseCookie;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuthCookieFactoryTest {

    private static final AuthCookieProperties PROPERTIES =
            new AuthCookieProperties("univgo_session", "univgo_refresh", true, "None", null);

    @Mock
    private TokenProviderPort tokenProvider;

    private AuthCookieFactory factory() {
        when(tokenProvider.getAccessExpirationSeconds()).thenReturn(900L);
        when(tokenProvider.getRefreshExpirationSeconds()).thenReturn(1800L);
        return new AuthCookieFactory(PROPERTIES, tokenProvider);
    }

    @Test
    void sessionCookieIsHttpOnlyAndLastsAsLongAsTheAccessToken() {
        ResponseCookie cookie = factory().session("access-token");

        assertThat(cookie.getName()).isEqualTo("univgo_session");
        assertThat(cookie.getValue()).isEqualTo("access-token");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("None");
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofSeconds(900));
    }

    @Test
    void refreshCookieIsScopedToTheAuthEndpointsThatConsumeIt() {
        ResponseCookie cookie = factory().refresh("refresh-token");

        assertThat(cookie.getPath()).isEqualTo("/auth");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofSeconds(1800));
    }

    @Test
    void clearedCookiesExpireImmediatelyOnTheSamePathTheyWereSetOn() {
        AuthCookieFactory factory = factory();

        assertThat(factory.clearedSession().getMaxAge()).isZero();
        assertThat(factory.clearedSession().getPath()).isEqualTo("/");
        assertThat(factory.clearedRefresh().getMaxAge()).isZero();
        assertThat(factory.clearedRefresh().getPath()).isEqualTo("/auth");
    }

    @Test
    void blankDomainIsOmittedSoTheBrowserScopesTheCookieToTheHost() {
        AuthCookieProperties blankDomain =
                new AuthCookieProperties("univgo_session", "univgo_refresh", false, "Lax", "  ");
        when(tokenProvider.getAccessExpirationSeconds()).thenReturn(900L);

        assertThat(new AuthCookieFactory(blankDomain, tokenProvider).session("t").getDomain()).isNull();
    }
}
