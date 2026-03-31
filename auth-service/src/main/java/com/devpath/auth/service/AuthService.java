package com.devpath.auth.service;

import com.devpath.auth.dto.request.LoginRequest;
import com.devpath.auth.dto.request.RegisterRequest;
import com.devpath.auth.dto.request.UpdateProfileRequest;
import com.devpath.auth.dto.response.AuthResponse;
import com.devpath.auth.dto.response.UserResponse;
import com.devpath.auth.exception.InvalidCredentialsException;
import com.devpath.auth.exception.TokenExpiredException;
import com.devpath.auth.exception.UserAlreadyExistsException;
import com.devpath.auth.model.RefreshToken;
import com.devpath.auth.model.User;
import com.devpath.auth.repository.RefreshTokenRepository;
import com.devpath.auth.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .build();

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(rawRefreshToken)
                .orElseThrow(() -> new TokenExpiredException("Invalid refresh token"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException("Refresh token expired or revoked");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return buildAuthResponse(stored.getUser());
    }

    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        refreshTokenRepository.revokeAllByUser(user);
    }

    public UserResponse getMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return toUserResponse(user);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return toUserResponse(user);
    }

    @Transactional
    public UserResponse updateMe(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already in use: " + request.getEmail());
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        userRepository.save(user);

        return toUserResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(
                user.getEmail(),
                Map.of("role", user.getRole().name(), "userId", user.getId())
        );

        String rawRefreshToken = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(rawRefreshToken)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .user(toUserResponse(user))
                .build();
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
