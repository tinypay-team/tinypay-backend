package com.tinypay.auth.service;

import com.tinypay.auth.domain.RefreshToken;
import com.tinypay.auth.dto.request.LoginRequest;
import com.tinypay.auth.dto.request.SendVerificationCodeRequest;
import com.tinypay.auth.dto.response.LoginResponse;
import com.tinypay.auth.dto.response.TokenRefreshResponse;
import com.tinypay.auth.google.GoogleIdTokenVerifier;
import com.tinypay.auth.google.GoogleUserInfo;
import com.tinypay.auth.jwt.JwtTokenProvider;
import com.tinypay.auth.repository.RefreshTokenRepository;
import com.tinypay.finance.domain.BudgetPolicy;
import com.tinypay.finance.repository.BudgetPolicyRepository;
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
    private final BudgetPolicyRepository budgetPolicyRepository;
    private final SmsService smsService;

    @Transactional
    public LoginResponse googleLogin(LoginRequest request) {

        GoogleUserInfo userInfo = googleIdTokenVerifier.verify(request.getIdToken());

        User user = userRepository.findByProviderId(userInfo.getSub())
                .orElseGet(() -> {
                    User newUser = userRepository.save(
                            User.builder()
                                    .providerId(userInfo.getSub())
                                    .email(userInfo.getEmail())
                                    .nickname(userInfo.getName())
                                    .profileImageUrl(userInfo.getPicture())
                                    .build()
                    );
                    budgetPolicyRepository.save(BudgetPolicy.builder()
                            .user(newUser)
                            .build());
                    return newUser;
                });

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

    public void sendVerificationCode(SendVerificationCodeRequest request) {
        smsService.sendVerificationCode(request.getPhoneNumber());
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    @Transactional
    public TokenRefreshResponse reissueToken(String bearerToken) {
        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer ")) {
            throw new CustomException(ErrorType.MISSING_REFRESH_TOKEN);
        }

        String refreshTokenValue = bearerToken.substring(7);

        if (!StringUtils.hasText(refreshTokenValue)) {
            throw new CustomException(ErrorType.MISSING_REFRESH_TOKEN);
        }

        jwtTokenProvider.validateRefreshTokenOrThrow(refreshTokenValue);

        RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(refreshTokenValue)
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_REFRESH_TOKEN));

        if (refreshToken.isRevoked()) {
            throw new CustomException(ErrorType.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtTokenProvider.extractUserId(refreshTokenValue);
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId);
        String newRefreshTokenValue = jwtTokenProvider.generateRefreshToken(userId);

        refreshToken.updateToken(
                newRefreshTokenValue,
                LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshExpiration() / 1000)
        );

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshTokenValue)
                .build();
    }
}