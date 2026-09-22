package com.univgo.backend.auth.infrastructure.adapter.in.web;

import com.univgo.backend.auth.application.port.in.LoginUseCase;
import com.univgo.backend.auth.application.port.in.LoginUseCase.LoginCommand;
import com.univgo.backend.auth.application.port.in.LogoutUseCase;
import com.univgo.backend.auth.application.port.in.LogoutUseCase.LogoutCommand;
import com.univgo.backend.auth.application.port.in.RefreshTokenUseCase;
import com.univgo.backend.auth.application.port.in.RefreshTokenUseCase.RefreshCommand;
import com.univgo.backend.auth.domain.InvalidRefreshTokenException;
import com.univgo.backend.auth.infrastructure.adapter.in.web.dto.LoginRequest;
import com.univgo.backend.users.application.port.in.GetUserByIdUseCase;
import com.univgo.backend.users.infrastructure.adapter.in.web.dto.UserResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

/**
 * Tokens never appear in a response body: they are set as HttpOnly cookies, so the browser stores
 * something no script in the page can read, and every response describes the session instead.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final AuthCookieFactory cookies;

    public AuthController(
            LoginUseCase loginUseCase,
            RefreshTokenUseCase refreshTokenUseCase,
            LogoutUseCase logoutUseCase,
            GetUserByIdUseCase getUserByIdUseCase,
            AuthCookieFactory cookies) {
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.cookies = cookies;
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = loginUseCase.execute(new LoginCommand(request.identifier(), request.password()));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookies.session(result.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookies.refresh(result.refreshToken()).toString())
                .body(UserResponse.from(result.user()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<UserResponse> refresh(HttpServletRequest request) {
        String refreshToken = readCookie(request, cookies.refreshName())
                .orElseThrow(InvalidRefreshTokenException::new);
        var result = refreshTokenUseCase.execute(new RefreshCommand(refreshToken));

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookies.session(result.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookies.refresh(result.refreshToken()).toString())
                .body(UserResponse.from(result.user()));
    }

    /**
     * Rehydrates a session the browser still holds but the application has forgotten — a reload, a
     * new tab, a deep link. The cookie is the only thing that survives; this turns it back into a
     * user and a role.
     */
    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        return UserResponse.from(getUserByIdUseCase.execute(UUID.fromString(authentication.getName())));
    }

    /**
     * Clears the cookies whether or not the token was still valid: signing out must always leave the
     * browser in the signed-out state, and a stale token is exactly when that matters most.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        readCookie(request, cookies.refreshName())
                .ifPresent(token -> logoutUseCase.execute(new LogoutCommand(token)));

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookies.clearedSession().toString())
                .header(HttpHeaders.SET_COOKIE, cookies.clearedRefresh().toString())
                .build();
    }

    private static Optional<String> readCookie(HttpServletRequest request, String name) {
        Cookie cookie = WebUtils.getCookie(request, name);
        return cookie == null || cookie.getValue().isBlank()
                ? Optional.empty()
                : Optional.of(cookie.getValue());
    }
}
