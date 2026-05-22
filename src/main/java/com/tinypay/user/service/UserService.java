package com.tinypay.user.service;

import com.tinypay.global.exception.CustomException;
import com.tinypay.global.exception.ErrorType;
import com.tinypay.user.domain.User;
import com.tinypay.user.dto.request.UpdateMyInfoRequest;
import com.tinypay.user.dto.response.GetMyInfoResponse;
import com.tinypay.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Pattern S3_PRESIGNED_URL_PATTERN = Pattern.compile(
            "^https://[a-zA-Z0-9\\-]+\\.s3\\.[a-zA-Z0-9\\-]+\\.amazonaws\\.com/.+"
    );

    private final UserRepository userRepository;

    @Transactional
    public void updateMyInfo(Long userId, UpdateMyInfoRequest request) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new CustomException(ErrorType.USER_NOT_FOUND));

        String nickname = request.getNickname();
        String profileImage = request.getProfileImage();

        if (StringUtils.hasText(nickname)) {
            validateNickname(nickname, userId);
        }

        if (StringUtils.hasText(profileImage)) {
            validateUrl(profileImage);
        }

        user.updateProfile(
                StringUtils.hasText(nickname) ? nickname : user.getNickname(),
                StringUtils.hasText(profileImage) ? profileImage : user.getProfileImageUrl()
        );
    }

    private void validateNickname(String nickname, Long userId) {
        if (nickname.length() < 2 || nickname.length() > 10) {
            throw new CustomException(ErrorType.INVALID_NICKNAME_LENGTH);
        }
        if (!nickname.matches("^[가-힣a-zA-Z0-9]+$")) {
            throw new CustomException(ErrorType.INVALID_NICKNAME_FORMAT);
        }
        if (userRepository.existsByNicknameAndIdNot(nickname, userId)) {
            throw new CustomException(ErrorType.DUPLICATE_NICKNAME);
        }
    }

    private void validateUrl(String url) {
        if (!S3_PRESIGNED_URL_PATTERN.matcher(url).matches()) {
            throw new CustomException(ErrorType.INVALID_URL_FORMAT);
        }
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