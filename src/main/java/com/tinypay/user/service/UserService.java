package com.tinypay.user.service;

import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import com.tinypay.user.domain.User;
import com.tinypay.user.dto.request.UpdateMyInfoRequest;
import com.tinypay.user.dto.response.GetMyInfoResponse;
import com.tinypay.auth.repository.RefreshTokenRepository;
import com.tinypay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        refreshTokenRepository.deleteByUserId(userId);
        user.softDelete();
    }

    @Transactional
    public void updateMyInfo(Long userId, UpdateMyInfoRequest request) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        String nickname = request.getNickname();
        String profileImage = request.getProfileImage();

        if (StringUtils.hasText(nickname)) {
            if (userRepository.existsByNicknameAndIdNot(nickname, userId)) {
                throw new CustomException(ErrorType.DUPLICATE_NICKNAME);
            }
        }

        user.updateProfile(
                StringUtils.hasText(nickname) ? nickname : user.getNickname(),
                StringUtils.hasText(profileImage) ? profileImage : user.getProfileImageUrl()
        );
    }

    @Transactional(readOnly = true)
    public GetMyInfoResponse getMyInfo(Long userId) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        return GetMyInfoResponse.builder()
                .user(GetMyInfoResponse.UserInfo.builder()
                        .userId(user.getId())
                        .email(user.getEmail())
                        .nickname(user.getNickname())
                        .profileImage(user.getProfileImageUrl())
                        .build())
                .build();
    }
}