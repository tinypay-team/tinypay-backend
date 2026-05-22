package com.tinypay.user.controller;

import com.tinypay.global.response.ApiResponse;
import com.tinypay.global.response.SuccessType;
import com.tinypay.user.dto.response.GetMyInfoResponse;
import com.tinypay.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<GetMyInfoResponse> getMyInfo(@RequestAttribute("userId") Long userId) {
        return ApiResponse.success(SuccessType.GET_MY_INFO_SUCCESS, userService.getMyInfo(userId));
    }
}