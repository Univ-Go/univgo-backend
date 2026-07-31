package com.univgo.backend.auth.infrastructure.adapter.in.web;

import com.univgo.backend.auth.application.port.in.LoginUseCase;
import com.univgo.backend.auth.application.port.in.LoginUseCase.LoginCommand;
import com.univgo.backend.auth.infrastructure.adapter.in.web.dto.LoginRequest;
import com.univgo.backend.auth.infrastructure.adapter.in.web.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginUseCase loginUseCase;

    public AuthController(LoginUseCase loginUseCase) {
        this.loginUseCase = loginUseCase;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        var result = loginUseCase.execute(new LoginCommand(request.identification(), request.password()));
        return LoginResponse.from(result);
    }
}
