package com.univgo.backend.auth.application.usecase;

import com.univgo.backend.auth.application.port.in.LoginUseCase;
import com.univgo.backend.auth.application.port.out.TokenProviderPort;
import com.univgo.backend.auth.domain.InvalidCredentialsException;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import com.univgo.backend.users.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements LoginUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final TokenProviderPort tokenProviderPort;

    public LoginService(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoder passwordEncoder,
            TokenProviderPort tokenProviderPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.tokenProviderPort = tokenProviderPort;
    }

    @Override
    public LoginResult execute(LoginCommand command) {
        User user = userRepositoryPort.findByIdentification(command.identification())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = tokenProviderPort.generateToken(user);
        return new LoginResult(token, tokenProviderPort.getExpirationSeconds());
    }
}
