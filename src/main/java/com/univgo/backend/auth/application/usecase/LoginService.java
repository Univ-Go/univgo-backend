package com.univgo.backend.auth.application.usecase;

import com.univgo.backend.auth.application.port.in.LoginUseCase;
import com.univgo.backend.auth.application.port.out.RefreshTokenRepositoryPort;
import com.univgo.backend.auth.application.port.out.TokenProviderPort;
import com.univgo.backend.auth.application.util.TokenHasher;
import com.univgo.backend.auth.domain.InvalidCredentialsException;
import com.univgo.backend.auth.domain.RefreshToken;
import com.univgo.backend.users.application.port.out.UserRepositoryPort;
import com.univgo.backend.users.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements LoginUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final TokenProviderPort tokenProviderPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;

    public LoginService(
            UserRepositoryPort userRepositoryPort,
            PasswordEncoder passwordEncoder,
            TokenProviderPort tokenProviderPort,
            RefreshTokenRepositoryPort refreshTokenRepositoryPort) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.tokenProviderPort = tokenProviderPort;
        this.refreshTokenRepositoryPort = refreshTokenRepositoryPort;
    }

    @Override
    public LoginResult execute(LoginCommand command) {
        User user = userRepositoryPort.findByLoginIdentifier(command.identifier())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String accessToken = tokenProviderPort.generateAccessToken(user);
        TokenProviderPort.IssuedToken issuedRefreshToken = tokenProviderPort.generateRefreshToken(user);

        refreshTokenRepositoryPort.save(RefreshToken.issue(
                user.getId(), TokenHasher.sha256(issuedRefreshToken.token()), issuedRefreshToken.expiresAt()));

        return new LoginResult(
                accessToken, issuedRefreshToken.token(), tokenProviderPort.getAccessExpirationSeconds(), user);
    }
}
