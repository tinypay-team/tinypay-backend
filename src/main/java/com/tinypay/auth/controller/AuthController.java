package com.tinypay.auth.controller;

import com.tinypay.auth.dto.request.LoginRequest;
import com.tinypay.auth.dto.response.LoginResponse;
import com.tinypay.auth.dto.response.TokenRefreshResponse;
import com.tinypay.auth.service.AuthService;
import com.tinypay.global.response.ApiResponse;
import com.tinypay.global.response.SuccessType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/google")
    public ApiResponse<LoginResponse> googleLogin(@RequestBody LoginRequest request) {
        LoginResponse response = authService.googleLogin(request);
        return ApiResponse.success(SuccessType.LOGIN_SUCCESS, response);
    }

    @DeleteMapping("/auth")
    public ApiResponse<?> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(authorization);
        return ApiResponse.success(SuccessType.LOGOUT_SUCCESS);
    }

    @PostMapping("/auth/refresh")
    public ApiResponse<TokenRefreshResponse> reissueToken(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        TokenRefreshResponse response = authService.reissueToken(authorization);
        return ApiResponse.success(SuccessType.TOKEN_REISSUE_SUCCESS, response);
    }
}