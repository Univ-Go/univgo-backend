package com.univgo.backend.auth.infrastructure.adapter.in.web;

import com.univgo.backend.auth.application.port.in.LoginUseCase;
import com.univgo.backend.auth.application.port.in.LoginUseCase.LoginCommand;
import com.univgo.backend.auth.application.port.in.LogoutUseCase;
import com.univgo.backend.auth.application.port.in.LogoutUseCase.LogoutCommand;
import com.univgo.backend.auth.application.port.in.RefreshTokenUseCase;
import com.univgo.backend.auth.application.port.in.RefreshTokenUseCase.RefreshCommand;
import com.univgo.backend.auth.infrastructure.adapter.in.web.dto.LoginRequest;
import com.univgo.backend.auth.infrastructure.adapter.in.web.dto.LoginResponse;
import com.univgo.backend.auth.infrastructure.adapter.in.web.dto.RefreshTokenRequest;
import com.univgo.backend.auth.infrastructure.adapter.in.web.dto.RefreshTokenResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;

    public AuthController(
            LoginUseCase loginUseCase, RefreshTokenUseCase refreshTokenUseCase, LogoutUseCase logoutUseCase) {
        this.loginUseCase = loginUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.logoutUseCase = logoutUseCase;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var result = loginUseCase.execute(new LoginCommand(request.identification(), request.password()));
        return LoginResponse.from(result);
    }

    @PostMapping("/refresh")
    public RefreshTokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        var result = refreshTokenUseCase.execute(new RefreshCommand(request.refreshToken()));
        return RefreshTokenResponse.from(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        logoutUseCase.execute(new LogoutCommand(request.refreshToken()));
        return ResponseEntity.noContent().build();
    }
}
