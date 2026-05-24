package com.tinypay.auth.service;

import com.tinypay.auth.domain.RefreshToken;
import com.tinypay.auth.dto.request.LoginRequest;
import com.tinypay.auth.dto.response.LoginResponse;
import com.tinypay.auth.google.GoogleIdTokenVerifier;
import com.tinypay.auth.google.GoogleUserInfo;
import com.tinypay.auth.jwt.JwtTokenProvider;
import com.tinypay.auth.repository.RefreshTokenRepository;
import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import com.tinypay.user.domain.User;
import com.tinypay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public LoginResponse googleLogin(LoginRequest request) {
        if (!StringUtils.hasText(request.getIdToken())) {
            throw new CustomException(ErrorType.MISSING_ID_TOKEN);
        }

        GoogleUserInfo userInfo = googleIdTokenVerifier.verify(request.getIdToken());

        User user = userRepository.findByProviderId(userInfo.getSub())
                .map(existing -> {
                    existing.updateProfile(userInfo.getName(), userInfo.getPicture());
                    return existing;
                })
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .providerId(userInfo.getSub())
                                .email(userInfo.getEmail())
                                .nickname(userInfo.getName())
                                .profileImageUrl(userInfo.getPicture())
                                .build()
                ));

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken(user.getId());

        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .refreshToken(refreshTokenValue)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshExpiration() / 1000))
                .build());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .user(LoginResponse.UserInfo.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .profileImage(user.getProfileImageUrl())
                        .build())
                .build();
    }
}