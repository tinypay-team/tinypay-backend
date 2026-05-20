package com.tinypay.auth.controller;

import com.tinypay.auth.dto.request.LoginRequest;
import com.tinypay.auth.dto.response.LoginResponse;
import com.tinypay.auth.service.AuthService;
import com.tinypay.global.response.ApiResponse;
import com.tinypay.global.response.SuccessType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}