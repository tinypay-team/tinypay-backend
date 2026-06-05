package com.tinypay.auth.controller;

import com.tinypay.auth.dto.request.LoginRequest;
import com.tinypay.auth.dto.request.SendVerificationCodeRequest;
import com.tinypay.auth.dto.request.VerifyCodeRequest;
import com.tinypay.auth.dto.response.LoginResponse;
import com.tinypay.auth.dto.response.TokenRefreshResponse;
import com.tinypay.auth.service.AuthService;
import com.tinypay.global.response.ApiResponse;
import com.tinypay.global.response.SuccessType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ApiResponse<LoginResponse> googleLogin(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.googleLogin(request);
        return ApiResponse.success(SuccessType.LOGIN_SUCCESS, response);
    }

    @PostMapping("/verification-code")
    public ApiResponse<?> sendVerificationCode(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody SendVerificationCodeRequest request) {
        authService.sendVerificationCode(request);
        return ApiResponse.success(SuccessType.SEND_VERIFICATION_CODE_SUCCESS);
    }

    @PostMapping("/verification-code/verify")
    public ApiResponse<?> verifyPhoneNumber(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody VerifyCodeRequest request) {
        authService.verifyPhoneNumber(userId, request);
        return ApiResponse.success(SuccessType.VERIFY_PHONE_NUMBER_SUCCESS);
    }

    @DeleteMapping
    public ApiResponse<?> logout(@RequestAttribute("userId") Long userId) {
        authService.logout(userId);
        return ApiResponse.success(SuccessType.LOGOUT_SUCCESS);
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenRefreshResponse> reissueToken(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        TokenRefreshResponse response = authService.reissueToken(authorization);
        return ApiResponse.success(SuccessType.TOKEN_REISSUE_SUCCESS, response);
    }
}