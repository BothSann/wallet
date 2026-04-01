package com.bothsann.wallet.auth.service;

import com.bothsann.wallet.auth.dto.AuthResponse;
import com.bothsann.wallet.auth.dto.LoginRequest;
import com.bothsann.wallet.auth.dto.RefreshTokenRequest;
import com.bothsann.wallet.auth.dto.RegisterRequest;
import com.bothsann.wallet.auth.entity.RefreshToken;
import com.bothsann.wallet.auth.repository.RefreshTokenRepository;
import com.bothsann.wallet.user.entity.User;
import com.bothsann.wallet.user.repository.UserRepository;
import com.bothsann.wallet.auth.security.JwtService;
import com.bothsann.wallet.shared.config.JwtProperties;
import com.bothsann.wallet.shared.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new RuntimeException("Email already registered"); // Phase 5: EmailAlreadyExistsException
        }
        User user = User.builder()
                .fullName(req.fullName())
                .email(req.email())
                .phone(req.phone())
                .password(passwordEncoder.encode(req.password()))
                .role(Role.USER)
                .isActive(true)
                .build();
        user = userRepository.save(user);
        // TODO Phase 3: create and save Wallet(user, ZERO, "USD")
        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new RuntimeException("User not found")); // Phase 5: UserNotFoundException
        if (!user.isActive()) {
            throw new RuntimeException("Account deactivated"); // Phase 5: AccountDeactivatedException
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        return buildAuthResponse(user);
    }

    public AuthResponse refresh(RefreshTokenRequest req) {
        RefreshToken stored = refreshTokenRepository.findByToken(req.refreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid token")); // Phase 5: InvalidTokenException
        if (stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired or revoked");
        }
        String newAccessToken = jwtService.generateAccessToken(stored.getUser());
        return new AuthResponse(newAccessToken, stored.getToken(), "Bearer",
                jwtProperties.getAccessExpiration() / 1000);
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshTokenValue = jwtService.generateRefreshToken(user);
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(refreshTokenValue)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshExpiration() / 1000))
                .build();
        refreshTokenRepository.save(token);
        return new AuthResponse(accessToken, refreshTokenValue, "Bearer",
                jwtProperties.getAccessExpiration() / 1000);
    }
}
